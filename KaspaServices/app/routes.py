from app import app, auth, session
from app.models import User, Sensor
from flask import request, abort, jsonify, g, send_from_directory
from cassandra.query  import SimpleStatement
from flask_httpauth import HTTPBasicAuth
from datetime import datetime
import os, tarfile

@app.route('/')
@app.route('/index')
@auth.login_required
def index():
    return "index"

@app.route('/api/token/v1.0/getauthtoken', methods=['POST'])
@auth.login_required
def getauthtoken():
    token = g.user.generate_auth_token()
    return jsonify({'token': token.decode('ascii')})

@app.route('/api/sensors/v1.0/downloadinstaller', methods=['GET'])
def downloadinstaller():
    filename = 'installer.sh'
    filedir = app.config['BASEDIR'] + '/app/static/'

    return send_from_directory(filedir, filename, as_attachment=True)

@app.route('/api/sensors/v1.0/verifysensorkey', methods=['POST'])
@auth.login_required
def verifysensorkey():
    device_id = request.json.get('device_id')
    sensor_key = request.json.get('sensor_key')
    netint = request.json.get('netint')
    print(device_id)
    print(sensor_key)
    print(netint)
    if device_id is None or sensor_key is None or netint is None:
        abort(400)
    q = Sensor.objects.filter(company = g.user['company'])
    q = q.filter(device_id = device_id)
    sensor = q.first()

    if sensor is None:
        abort(400)

    #create tarball

    buildfile = 'build_snoqtt.sh'
    conffile = 'env-conf.conf'
    removefile = 'remove_snoqtt.sh'
    startfile = 'start_snoqtt.sh'
    stopfile = 'stop_snoqtt.sh'
    
    filedirtemplate = app.config['BASEDIR'] + '/app/static/template/'
    
    if not os.path.exists(app.config['BASEDIR'] + '/app/static/generated/{}/'.format(sensor_key)):
        os.makedirs(app.config['BASEDIR'] + '/app/static/generated/{}/'.format(sensor_key))
    
    filediroutput = app.config['BASEDIR'] + '/app/static/generated/{}/'.format(sensor_key)

    with open(filedirtemplate + buildfile) as build_template:
        templatebuild = build_template.read()
    with open(filediroutput + buildfile, "w") as current:
        current.write(templatebuild.format(protected_subnet=sensor['protected_subnet'],
                                            external_subnet="'{}'".format(sensor['external_subnet']),
                                            oinkcode=sensor['oinkcode']))
    
    with open(filedirtemplate + conffile) as conf_template:
        templateconf = conf_template.read()
    with open(filediroutput + conffile, "w") as current:
        current.write(templateconf.format(global_topic=sensor['topic_global'],
                                            global_server='103.24.56.244',
                                            global_port='1883',
                                            device_id=sensor['device_id'],
                                            oinkcode=sensor['oinkcode'],
                                            protected_subnet=sensor['protected_subnet'],
                                            external_subnet=sensor['external_subnet'],
                                            netint=netint,
                                            company=g.user['company']))
    
    with open(filedirtemplate + removefile) as remove_template:
        templateremove = remove_template.read()
    with open(filediroutput + removefile, "w") as current:
        current.write(templateremove)

    with open(filedirtemplate + startfile) as start_template:
        templatestart = start_template.read()
    with open(filediroutput + startfile, "w") as current:
        current.write(templatestart)

    with open(filedirtemplate + stopfile) as stop_template:
        templatestop = stop_template.read()
    with open(filediroutput + stopfile, "w") as current:
        current.write(templatestop)

    filetarname='snoqtt-{}.tar.gz'.format(sensor_key)
    if os.path.exists(filediroutput + filetarname):
        os.remove(filediroutput + filetarname)

    tar = tarfile.open((filediroutput + filetarname), "w:gz")
    tar.add(filediroutput + buildfile, arcname=buildfile)
    tar.add(filediroutput + conffile, arcname=conffile)
    tar.add(filediroutput + removefile, arcname=removefile)
    tar.add(filediroutput + startfile, arcname=startfile)
    tar.add(filediroutput + stopfile, arcname=stopfile)
    tar.close()

    os.remove(filediroutput + buildfile)
    os.remove(filediroutput + conffile)
    os.remove(filediroutput + removefile)
    os.remove(filediroutput + startfile)
    os.remove(filediroutput + stopfile)

    return send_from_directory(filediroutput, filetarname, as_attachment=True)

@app.route('/api/sensors/v1.0/listsensors', methods=['POST'])
@auth.login_required
def listsensors():
    company = g.user['company']
    if company is None:
        abort(400)
    
    obj={
        "company" : g.user['company'],
        "count" : 0,
        "sensors" : []
    }
    for sensor in Sensor.objects.filter(company=company):
        sensor_obj = {
            "device_id" : sensor['device_id'],
            "device_name" : sensor['device_name'],
            "hostname" : sensor['hostname'],
            "ip_address" : sensor['ip_address'],
            "location" : sensor['location'],
            "protected_subnet" : sensor['protected_subnet'],
            "external_subnet" : sensor['external_subnet'],
            "oinkcode" : sensor['oinkcode'],
            "topic_global" : sensor['topic_global'],
            "topic_cmd" : sensor['topic_cmd'],
            "topic_resp" : sensor['topic_resp'],
            "sensor_key" : sensor['sensor_key'],
            "time_created" : sensor['time_created']
        }
        obj['sensors'].append(sensor_obj)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/sensors/v1.0/getsensordetail', methods=['POST'])
@auth.login_required
def getsensordetail():
    company = g.user['company']
    device_id = request.json.get('device_id')
    if device_id is None or company is None:
        abort(400)
    
    q = Sensor.objects.filter(company=company)
    q = q.filter(device_id=device_id)
    sensor = q.first()

    if sensor is None:
        abort(400)
    
    sensor_obj = {
        "company" : sensor['company'],
        "device_id" : sensor['device_id'],
        "device_name" : sensor['device_name'],
        "hostname" : sensor['hostname'],
        "ip_address" : sensor['ip_address'],
        "location" : sensor['location'],
        "protected_subnet" : sensor['protected_subnet'],
        "external_subnet" : sensor['external_subnet'],
        "oinkcode" : sensor['oinkcode'],
        "topic_global" : sensor['topic_global'],
        "topic_cmd" : sensor['topic_cmd'],
        "topic_resp" : sensor['topic_resp'],
        "sensor_key" : sensor['sensor_key'],
        "time_created" : sensor['time_created']
    }
    
    return jsonify(sensor_obj)

@app.route('/api/sensors/v1.0/createsensor', methods=['POST'])
@auth.login_required
def createsensor():
    device_name = request.json.get('device_name')
    hostname = request.json.get('hostname')
    ip_address = request.json.get('ip_address')
    location = request.json.get('location')
    protected_subnet = request.json.get('protected_subnet')
    external_subnet = request.json.get('external_subnet')
    oinkcode = request.json.get('oinkcode')
    company = g.user['company']
    sensor = Sensor(company=company, device_name=device_name,
        hostname=hostname, ip_address=ip_address, location=location,
        protected_subnet=protected_subnet)
    
    if external_subnet:
        sensor.set_oinkcode(oinkcode)
    if external_subnet:
        sensor.set_external_subnet(external_subnet)

    sensor.create_dev_id(device_name)
    sensor.create_topic_cmd()
    sensor.create_topic_resp()

    Sensor.create(company=sensor['company'],
        device_id=sensor['device_id'],
        device_name=sensor['device_name'],
        hostname=sensor['hostname'],
        ip_address=sensor['ip_address'],
        location=sensor['location'],
        protected_subnet=sensor['protected_subnet'],
        external_subnet=sensor['external_subnet'],
        oinkcode=sensor['oinkcode'],
        topic_global=sensor['topic_global'],
        topic_cmd=sensor['topic_cmd'],
        topic_resp=sensor['topic_resp'],
        sensor_key=sensor['sensor_key'],
        time_created=sensor['time_created']
    )

    session.execute(
        """
        INSERT INTO sensor_status (device_id, status, ts) 
        VALUES (%s, %s, %s)
        """,
        (sensor['device_id'], "STOPPED", datetime.now())
    )

    return jsonify({
        'device_id' : sensor['device_id'],
        'device_name' : sensor['device_name'],
        'sensor_key' : sensor['sensor_key'],
    })

@app.route('/api/users/v1.0/getuserdetail/<username>', methods=['POST'])
@auth.login_required
def getuserdetail(username):
    q = User.objects.filter(username=username).first()
    if User.objects.filter(username = username).first() is None:
        abort(400)
    username = q['username']
    first_name = q['first_name']
    last_name = q['last_name']
    email = q['email']
    company = q['company']

    return jsonify({
        'username' : username,
        'first_name' : first_name,
        'last_name' : last_name,
        'email' : email,
        'company' : company
    })

@app.route('/api/users/v1.0/createuser', methods=['POST'])
def createuser():
    username = request.json.get('username')
    password = request.json.get('password')
    first_name = request.json.get('first_name')
    last_name = request.json.get('last_name')
    email = request.json.get('email')
    company = request.json.get('company')
    if username is None or password is None:
        abort(400)
    if User.objects.filter(username = username).first() is not None:
        abort(400)
    user = User(username = username, first_name = first_name, last_name = last_name, email = email, company = company)
    user.hash_password(password)
    user.set_admin()

    User.create(username=user['username'],
        first_name=user['first_name'],
        last_name=user['last_name'],
        password_hash=user['password_hash'],
        email=user['email'],
        company=user['company'],
        group=user['group'],
        time_joined=user['time_joined']
    )

    return jsonify({'username': user['username']}), 201

@app.route('/api/statistic/v1.0/rawdata', methods=['POST'])
@auth.login_required
def getrawdata():
    # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    query = "SELECT * FROM kaspa.raw_data_by_company WHERE company='{}' LIMIT {}".format(
        company, limit
    )
    if year is not None:
        query = "SELECT * FROM kaspa.raw_data_by_company WHERE company='{}' and year={} LIMIT {}".format(
            company, year, limit
        )
        if month is not None:
            query = "SELECT * FROM kaspa.raw_data_by_company WHERE company='{}' and year={} and month={} LIMIT {}".format(
                company, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM kaspa.raw_data_by_company WHERE company='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM kaspa.raw_data_by_company WHERE company='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM kaspa.raw_data_by_company WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM kaspa.raw_data_by_company WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                company, year, month, day, hour, minute, second, limit
                            )

    statement = SimpleStatement(query)
    obj = {
        "company" : company,
        "count" : 0,
        "data" : []
    }
    for raw_data in session.execute(statement):
        obj['data'].append(raw_data)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/rawdata/<device_id>', methods=['POST'])
@auth.login_required
def getrawdatadev(device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    query = "SELECT * FROM kaspa.raw_data_by_device_id WHERE device_id='{}' LIMIT {}".format(
        device_id, limit
    )
    if year is not None:
        query = "SELECT * FROM kaspa.raw_data_by_device_id WHERE device_id='{}' and year={} LIMIT {}".format(
            device_id, year, limit
        )
        if month is not None:
            query = "SELECT * FROM kaspa.raw_data_by_device_id WHERE device_id='{}' and year={} and month={} LIMIT {}".format(
                device_id, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM kaspa.raw_data_by_device_id WHERE device_id='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM kaspa.raw_data_by_device_id WHERE device_id='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM kaspa.raw_data_by_device_id WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM kaspa.raw_data_by_device_id WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                device_id, year, month, day, hour, minute, second, limit
                            )

    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "count" : 0,
        "data" : []
    }
    for raw_data in session.execute(statement):
        obj['data'].append(raw_data)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/eventhit', methods=['POST'])
@auth.login_required
def geteventhit():
    # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    query = "SELECT * FROM event_hit_on_company_year WHERE company='{}' LIMIT {}".format(
        company, limit
    )
    if year is not None:
        query = "SELECT * FROM event_hit_on_company_month WHERE company='{}' and year={} LIMIT {}".format(
            company, year, limit
        )
        if month is not None:
            query = "SELECT * FROM event_hit_on_company_day WHERE company='{}' and year={} and month={} LIMIT {}".format(
                company, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM event_hit_on_company_hour WHERE company='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM event_hit_on_company_minute WHERE company='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM event_hit_on_company_sec WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, year, month, day, hour, minute, limit
                        )
            
    statement = SimpleStatement(query)
    obj = {
        "company" : company,
        "count" : 0,
        "data" : []
    }
    for eventhit in session.execute(statement):
        obj['data'].append(eventhit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/eventhit/<device_id>', methods=['POST'])
@auth.login_required
def geteventhitdev(device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    query = "SELECT * FROM event_hit_on_device_id_year WHERE device_id='{}' LIMIT {}".format(
        device_id, limit
    ) 
    if year is not None:
        query = "SELECT * FROM event_hit_on_device_id_month WHERE device_id='{}' and year={} LIMIT {}".format(
            device_id, year, limit
        )
        if month is not None:
            query = "SELECT * FROM event_hit_on_device_id_day WHERE device_id='{}' and year={} and month={} LIMIT {}".format(
                device_id, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM event_hit_on_device_id_hour WHERE device_id='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM event_hit_on_device_id_min WHERE device_id='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM event_hit_on_device_id_sec WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, year, month, day, hour, minute, limit
                        )
                
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "count" : 0,
        "data" : []
    }
    for eventhit in session.execute(statement):
        obj['data'].append(eventhit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)


@app.route('/api/statistic/v1.0/signaturehit', methods=['POST'])
@auth.login_required
def getsignaturehit():
    # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM signature_hit_on_company_year WHERE company='{}' and year={} LIMIT {}".format(
            company, year, limit
        )
        if month is not None:
            query = "SELECT * FROM signature_hit_on_company_month WHERE company='{}' and year={} and month={} LIMIT {}".format(
                company, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM signature_hit_on_company_day WHERE company='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM signature_hit_on_company_hour WHERE company='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM signature_hit_on_company_minute WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM signature_hit_on_company_sec WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                company, year, month, day, hour, minute, second, limit
                            )
                
    statement = SimpleStatement(query)
        
    obj = {
        "company" : company,
        "count" : 0,
        "data" : []
    }
    for signaturehit in session.execute(statement):
        obj['data'].append(signaturehit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)
    
@app.route('/api/statistic/v1.0/signaturehit/<device_id>', methods=['POST'])
@auth.login_required
def getsignaturehitdev(device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM signature_hit_on_device_id_year WHERE device_id='{}' and year={} LIMIT {}".format(
            device_id, year, limit
        )
        if month is not None:
            query = "SELECT * FROM signature_hit_on_device_id_month WHERE device_id='{}' and year={} and month={} LIMIT {}".format(
                device_id, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM signature_hit_on_device_id_day WHERE device_id='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM signature_hit_on_device_id_hour WHERE device_id='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM signature_hit_on_device_id_minute WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM signature_hit_on_device_id_sec WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                device_id, year, month, day, hour, minute, second, limit
                            )
                
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "count" : 0,
        "data" : []
    }
    for signaturehit in session.execute(statement):
        obj['data'].append(signaturehit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/protocolhit', methods=['POST'])
@auth.login_required
def getprotocolhit():
     # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM protocol_hit_on_company_year WHERE company='{}' and year={} LIMIT {}".format(
            company, year, limit
        )
        if month is not None:
            query = "SELECT * FROM protocol_hit_on_company_month WHERE company='{}' and year={} and month={} LIMIT {}".format(
                company, year, month, limit
            )
            if day is not None:                
                query = "SELECT * FROM protocol_hit_on_company_day WHERE company='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM protocol_hit_on_company_hour WHERE company='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM protocol_hit_on_company_minute WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM protocol_hit_on_company_sec WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                company, year, month, day, hour, minute, second, limit
                            )
                
    statement = SimpleStatement(query)
    obj = {
        "company" : company,
        "count" : 0,
        "data" : []
    }
    for protocolhit in session.execute(statement):
        obj['data'].append(protocolhit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)
    
@app.route('/api/statistic/v1.0/protocolhit/<device_id>', methods=['POST'])
@auth.login_required
def getprotocolhitdev(device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM protocol_hit_on_device_id_year WHERE device_id='{}' and year={} LIMIT {}".format(
            device_id, year, limit
        )
        if month is not None:
            query = "SELECT * FROM protocol_hit_on_device_id_month WHERE device_id='{}' and year={} and month={} LIMIT {}".format(
                device_id, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM protocol_hit_on_device_id_day WHERE device_id='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM protocol_hit_on_device_id_hour WHERE device_id='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM protocol_hit_on_device_id_minute WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM protocol_hit_on_device_id_sec WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                device_id, year, month, day, hour, minute, second, limit
                            )
                
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "count" : 0,
        "data" : []
    }
    for protocolhit in session.execute(statement):
        obj['data'].append(protocolhit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/protocolbysporthit/<protocol>', methods=['POST'])
@auth.login_required
def getprotocolbysporthit(protocol):
    # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM protocol_by_sport_hit_on_company_year WHERE company='{}' and protocol='{}' and year={} LIMIT {}".format(
            company, protocol, year, limit
        )
        if month is not None:
            query = "SELECT * FROM protocol_by_sport_hit_on_company_month WHERE company='{}' and protocol='{}' and year={} and month={} LIMIT {}".format(
                company, protocol, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM protocol_by_sport_hit_on_company_day WHERE company='{}' and protocol='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, protocol, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM protocol_by_sport_hit_on_company_hour WHERE company='{}' and protocol='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, protocol, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM protocol_by_sport_hit_on_company_minute WHERE company='{}' and protocol='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, protocol, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM protocol_by_sport_hit_on_company_sec WHERE company='{}' and protocol='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                company, protocol, year, month, day, hour, minute, second, limit
                            )
                
    statement = SimpleStatement(query)
    obj = {
        "company" : company,
        "protocol": protocol,
        "count" : 0,
        "data" : []
    }
    for protocolbysporthit in session.execute(statement):
        obj['data'].append(protocolbysporthit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/protocolbysporthit/<protocol>/<device_id>', methods=['POST'])
@auth.login_required
def getprotocolbysporthitdev(protocol, device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM protocol_by_sport_hit_on_device_id_year WHERE device_id='{}' and protocol='{}' and year={} LIMIT {}".format(
            device_id, protocol, year, limit
        )
        if month is not None:
            query = "SELECT * FROM protocol_by_sport_hit_on_device_id_month WHERE device_id='{}' and protocol='{}' and year={} and month={} LIMIT {}".format(
                device_id, protocol, year, month, limit
            )
            if day is not None:    
                query = "SELECT * FROM protocol_by_sport_hit_on_device_id_day WHERE device_id='{}' and protocol='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, protocol, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM protocol_by_sport_hit_on_device_id_hour WHERE device_id='{}' and protocol='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, protocol, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM protocol_by_sport_hit_on_device_id_minute WHERE device_id='{}' and protocol='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, protocol, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM protocol_by_sport_hit_on_device_id_sec WHERE device_id='{}' and protocol='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                device_id, protocol, year, month, day, hour, minute, second, limit
                            )
                
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "protocol": protocol,
        "count" : 0,
        "data" : []
    }
    for protocolbysporthit in session.execute(statement):
        obj['data'].append(protocolbysporthit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/protocolbydporthit/<protocol>', methods=['POST'])
@auth.login_required
def getprotocolbydporthit(protocol):
    # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM protocol_by_dport_hit_on_company_year WHERE company='{}' and protocol='{}' and year={} LIMIT {}".format(
            company, protocol, year, limit
        )
        if month is not None:
            query = "SELECT * FROM protocol_by_dport_hit_on_company_month WHERE company='{}' and protocol='{}' and year={} and month={} LIMIT {}".format(
                company, year, month, day, limit
            )
            if day is not None:
                query = "SELECT * FROM protocol_by_dport_hit_on_company_day WHERE company='{}' and protocol='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, protocol, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM protocol_by_dport_hit_on_company_hour WHERE company='{}' and protocol='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, protocol, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM protocol_by_dport_hit_on_company_minute WHERE company='{}' and protocol='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, protocol, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM protocol_by_dport_hit_on_company_second WHERE company='{}' and protocol='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                company, protocol, year, month, day, hour, minute, second, limit
                            )
                
    statement = SimpleStatement(query)
    obj = {
        "company" : company,
        "protocol": protocol,
        "count" : 0,
        "data" : []
    }
    for protocolbydporthit in session.execute(statement):
        obj['data'].append(protocolbydporthit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/protocolbydporthit/<protocol>/<device_id>', methods=['POST'])
@auth.login_required
def getprotocolbydporthitdev(protocol, device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM protocol_by_dport_hit_on_device_id_year WHERE device_id='{}' and protocol='{}' and year={} LIMIT {}".format(
            device_id, protocol, year, limit
        )
        if month is not None:
            query = "SELECT * FROM protocol_by_dport_hit_on_device_id_month WHERE device_id='{}' and protocol='{}' and year={} and month={} LIMIT {}".format(
                device_id, protocol, year, month, limit
            )
            if day is not None:    
                query = "SELECT * FROM protocol_by_dport_hit_on_device_id_day WHERE device_id='{}' and protocol='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, protocol, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM protocol_by_dport_hit_on_device_id_hour WHERE device_id='{}' and protocol='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, protocol, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM protocol_by_dport_hit_on_device_id_minute WHERE device_id='{}' and protocol='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, protocol, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM protocol_by_dport_hit_on_device_id_second WHERE device_id='{}' and protocol='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                device_id, protocol, year, month, day, hour, minute, second, limit
                            )
                
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "protocol": protocol,
        "count" : 0,
        "data" : []
    }
    for protocolbydporthit in session.execute(statement):
        obj['data'].append(protocolbydporthit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/ipsourcehit', methods=['POST'])
@auth.login_required
def getipsourcehit():
    # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM ip_source_hit_on_company_year WHERE company='{}' and year={} LIMIT {}".format(
            company, year, limit
        )
        if month is not None:
            query = "SELECT * FROM ip_source_hit_on_company_month WHERE company='{}' and year={} and month={} LIMIT {}".format(
                company, year, month, limit
                )
            if day is not None:
                query = "SELECT * FROM ip_source_hit_on_company_day WHERE company='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, year, month, day, limit
                    )
                if hour is not None:
                    query = "SELECT * FROM ip_source_hit_on_company_hour WHERE company='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM ip_source_hit_on_company_minute WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM ip_source_hit_on_company_sec WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                company, year, month, day, hour, minute, second, limit
                            )
    
    statement = SimpleStatement(query)
    obj = {
        "company" : company,
        "count" : 0,
        "data" : []
    }
    for ipsourcehit in session.execute(statement):
        obj['data'].append(ipsourcehit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/ipsourcehit/<device_id>', methods=['POST'])
@auth.login_required
def getipsourcehitdev(device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM ip_source_hit_on_device_id_year WHERE device_id='{}' and year={} LIMIT {}".format(
            device_id, year, limit
        )
        if month is not None:
            query = "SELECT * FROM ip_source_hit_on_device_id_month WHERE device_id='{}' and year={} and month={} LIMIT {}".format(
                device_id, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM ip_source_hit_on_device_id_day WHERE device_id='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM ip_source_hit_on_device_id_hour WHERE device_id='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM ip_source_hit_on_device_id_minute WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM ip_source_hit_on_device_id_sec WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                device_id, year, month, day, hour, minute, second, limit
                            )
    
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "count" : 0,
        "data" : []
    }
    for ipsourcehit in session.execute(statement):
        obj['data'].append(ipsourcehit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/ipdesthit', methods=['POST'])
@auth.login_required
def getipdesthit():
    # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM ip_dest_hit_on_company_year WHERE company='{}' and year={} LIMIT {}".format(
            company, year, limit
        )
        if month is not None:
            query = "SELECT * FROM ip_dest_hit_on_company_month WHERE company='{}' and year={} and month={} LIMIT {}".format(
                company, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM ip_dest_hit_on_company_day WHERE company='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM ip_dest_hit_on_company_hour WHERE company='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM ip_dest_hit_on_company_minute WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM ip_dest_hit_on_company_sec WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                company, year, month, day, hour, minute, second, limit
                            )
    
    statement = SimpleStatement(query)
    obj = {
        "company" : company,
        "count" : 0,
        "data" : []
    }
    for ipdesthit in session.execute(statement):
        obj['data'].append(ipdesthit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/ipdesthitdev/<device_id>', methods=['POST'])
@auth.login_required
def getipdesthitdev(device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM ip_dest_hit_on_device_id_year WHERE device_id='{}' and year={} LIMIT {}".format(
            device_id, year, limit
        )
        if month is not None:
            query = "SELECT * FROM ip_dest_hit_on_device_id_month WHERE device_id='{}' and year={} and month={} LIMIT {}".format(
                device_id, year, month, limit
            )
            if day is not None:              
                query = "SELECT * FROM ip_dest_hit_on_device_id_day WHERE device_id='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM ip_dest_hit_on_device_id_hour WHERE device_id='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM ip_dest_hit_on_device_id_minute WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM ip_dest_hit_on_device_id_sec WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                device_id, year, month, day, hour, minute, second, limit
                            )
    
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "count" : 0,
        "data" : []
    }
    for ipdesthit in session.execute(statement):
        obj['data'].append(ipdesthit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/countrysourcehit', methods=['POST'])
@auth.login_required
def getcountrysourcehit():
    # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM country_source_hit_on_company_day WHERE company='{}' and year={} LIMIT {}".format(
            company, year, limit
        )
        if month is not None:
            query = "SELECT * FROM country_source_hit_on_company_day WHERE company='{}' and year={} and month={} LIMIT {}".format(
                company, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM country_source_hit_on_company_day WHERE company='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM country_source_hit_on_company_hour WHERE company='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM country_source_hit_on_company_minute WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM country_source_hit_on_company_second WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                company, year, month, day, hour, minute, second, limit
                            )
    
    statement = SimpleStatement(query)
    obj = {
        "company" : company,
        "count" : 0,
        "data" : []
    }
    for countrysourcehit in session.execute(statement):
        obj['data'].append(countrysourcehit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)
    
@app.route('/api/statistic/v1.0/countrysourcehit/<device_id>', methods=['POST'])
@auth.login_required
def getcountrysourcehitdev(device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM country_source_hit_on_device_id_year WHERE device_id='{}' and year={} LIMIT {}".format(
            device_id, year, limit
        )
        if month is not None:
            query = "SELECT * FROM country_source_hit_on_device_id_month WHERE device_id='{}' and year={} and month={} LIMIT {}".format(
                device_id, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM country_source_hit_on_device_id_day WHERE device_id='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM country_source_hit_on_device_id_hour WHERE device_id='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM country_source_hit_on_device_id_minute WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM country_source_hit_on_device_id_second WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                device_id, year, month, day, hour, minute, second, limit
                            )
    
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "count" : 0,
        "data" : []
    }
    for ipdesthit in session.execute(statement):
        obj['data'].append(ipdesthit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/countrydesthit', methods=['POST'])
@auth.login_required
def getcountrydesthit():
    # company = g.user['company']
    company = request.json.get('company')
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM country_dest_hit_on_company_year WHERE company='{}' and year={} LIMIT {}".format(
            company, year, limit
        )
        if month is not None:
            query = "SELECT * FROM country_dest_hit_on_company_day WHERE company='{}' and year={} and month={} LIMIT {}".format(
                company, year, month, limit
            )
            if day is not None:
                query = "SELECT * FROM country_dest_hit_on_company_day WHERE company='{}' and year={} and month={} and day={} LIMIT {}".format(
                    company, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM country_dest_hit_on_company_hour WHERE company='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        company, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM country_dest_hit_on_company_minute WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            company, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM country_dest_hit_on_company_second WHERE company='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                company, year, month, day, hour, minute, second, limit
                            )
    
    statement = SimpleStatement(query)
    obj = {
        "company" : company,
        "count" : 0,
        "data" : []
    }
    for countrydesthit in session.execute(statement):
        obj['data'].append(countrydesthit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/statistic/v1.0/countrydesthit/<device_id>', methods=['POST'])
@auth.login_required
def getcountrydesthitdev(device_id):
    year = request.json.get('year')
    month = request.json.get('month')
    day = request.json.get('day')
    hour = request.json.get('hour')
    minute = request.json.get('minute')
    second = request.json.get('second')
    limit = request.json.get('limit')

    if year is not None:
        query = "SELECT * FROM country_dest_hit_on_device_id_year WHERE device_id='{}' and year={} LIMIT {}".format(
            device_id, year, limit
        )
        if month is not None:
            query = "SELECT * FROM country_dest_hit_on_device_id_month WHERE device_id='{}' and year={} and month={} LIMIT {}".format(
                device_id, year, month, limit
            )           
            if day is not None:
                query = "SELECT * FROM country_dest_hit_on_device_id_day WHERE device_id='{}' and year={} and month={} and day={} LIMIT {}".format(
                    device_id, year, month, day, limit
                )
                if hour is not None:
                    query = "SELECT * FROM country_dest_hit_on_device_id_hour WHERE device_id='{}' and year={} and month={} and day={} and hour={} LIMIT {}".format(
                        device_id, year, month, day, hour, limit
                    )
                    if minute is not None:
                        query = "SELECT * FROM country_dest_hit_on_device_id_minute WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} LIMIT {}".format(
                            device_id, year, month, day, hour, minute, limit
                        )
                        if second is not None:
                            query = "SELECT * FROM country_dest_hit_on_device_id_second WHERE device_id='{}' and year={} and month={} and day={} and hour={} and minute={} and second={} LIMIT {}".format(
                                device_id, year, month, day, hour, minute, second, limit
                            )
    
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "count" : 0,
        "data" : []
    }
    for countrydesthit in session.execute(statement):
        obj['data'].append(countrydesthit)
        obj['count'] = obj['count'] + 1
    
    return jsonify(obj)

@app.route('/api/sensor/v1.0/checkstatus/<device_id>', methods=['POST'])
@auth.login_required
def getSensorStatus(device_id):
    query = "SELECT * FROM sensor_status WHERE device_id='{}'".format(device_id)
    statement = SimpleStatement(query)
    obj = {
        "device_id" : device_id,
        "ts" : "",
        "status" : "",
    }
    for status in session.execute(statement):
        obj['ts'] = status['ts']
        obj['status'] = status['status']

    return jsonify(obj)

@app.route('/api/sensor/v1.0/startsensor/<device_id>', methods=['POST'])
def startSensor(device_id):
    session.execute(
        """
        INSERT INTO sensor_status (device_id, status, ts) 
        VALUES (%s, %s, %s)
        """,
        (device_id, "RUNNING", datetime.now())
    )

@app.route('/api/sensor/v1.0/stopsensor/<device_id>', methods=['POST'])
def stopSensor(device_id):
    session.execute(
        """
        INSERT INTO sensor_status (device_id, status, ts) 
        VALUES (%s, %s, %s)
        """,
        (device_id, "STOPPED", datetime.now())
    )

@auth.verify_password
def verify_password(username_or_token, password):
    user = User.verify_auth_token(username_or_token)
    if not user:
        user = User.objects.filter(username=username_or_token).first()
        if not user or not user.verify_password(password):
            return False
    g.user = user
    return True
