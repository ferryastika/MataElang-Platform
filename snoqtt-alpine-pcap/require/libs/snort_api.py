from flask import Flask, request, jsonify
import xmlrpc.client as xc
from tinydb import TinyDB, Query
import uuid

db = TinyDB('snort_db.json')

snort_query = Query()

super_server = xc.ServerProxy('http://127.0.0.1:9001/RPC2')

app = Flask(__name__)


def is_running(service):
    state = super_server.supervisor.getProcessInfo(service)["statename"]
    if state == "RUNNING":
        return True
    else:
        return False


def snort_stop():
    service = "snort"
    running = is_running(service)
    if running:
        stop = super_server.supervisor.stopProcess(service)
        if stop:
            stop_success = True
            stopped = super_server.supervisor.getProcessInfo(service)
            stopped["stop_success"] = stop_success
            return jsonify(stopped), 202
        else:
            stop_success = False
            stopped = super_server.supervisor.getProcessInfo(service)
            stopped["stop_success"] = stop_success
            return jsonify(stopped), 500
    else:
        stop_error = {"stop_error": "Service is not running.  Must be Running to stop."}
        stopped = super_server.supervisor.getProcessInfo(service)
        stopped["stop_success"] = stop_error
        return jsonify(stopped), 400


def snort_start():
    service = "snort"
    running = is_running(service)
    if running:
        print(running)
        start_error = {"start_error": "Service is already running.  Must be stopped to start."}
        started = super_server.supervisor.getProcessInfo(service)
        started["start_success"] = start_error
        return jsonify(started), 400

    else:
        start = super_server.supervisor.startProcess(service)
        started = super_server.supervisor.getProcessInfo(service)
        if start:
            start_success = True
            started["start_success"] = start_success
            return jsonify(started), 202
        else:
            start_success = False
            started["start_success"] = start_success
            return jsonify(started), 500


def snort_check():
    service = "snort"
    running = is_running(service)
    if running:
        status = super_server.supervisor.getProcessInfo(service)
        status['running'] = True
        return jsonify(status), 202
    else:
        status = super_server.supervisor.getProcessInfo(service)
        status['running'] = False
        return jsonify(status), 500 
        

def snort_restart():
    service = "snort"
    stop = super_server.supervisor.stopProcess(service)
    start = super_server.supervisor.startProcess(service)
    if any((stop, start)):
        restart_success = True
        restart = super_server.supervisor.getProcessInfo(service)
        restart['restart_successful'] = restart_success
        return jsonify(restart), 202
    else:
        restart_success = False
        restart = super_server.supervisor.getProcessInfo(service)
        restart['restart_successful'] = restart_success
        return jsonify(restart), 500


def pp_json_elements(some_json):
    required = ["name", "protocol", "source_ip", "source_port", "dest_ip", "dest_port", "msg", "sid", "rule_type"]
    if any(elem not in some_json.keys() for elem in required):
        return False
    else:
        return True


def del_json_elements(some_json):
    required = ["sid"]

    if any(elem not in some_json.keys() for elem in required):
        return False
    else:
        return True


def get_url_elements(some_param):
    required = ["name", "protocol", "source_ip", "source_port",
                "dest_ip", "dest_port", "msg", "sid", "rule_type", "uuid", "rule_type"]

    if any(elem in some_param for elem in required):
        return True
    else:
        return False


def norm_values(snort_json):

    if "sid" in snort_json.keys():
        snort_json["sid"] = str(snort_json["sid"])

    if "source_port" in snort_json.keys():
        snort_json["source_port"] = str(snort_json["source_port"])

    if "dest_port" in snort_json.keys():
        snort_json["dest_port"] = str(snort_json["dest_port"])

    return snort_json


@app.route('/snort/api/v0.1/checkstatus', methods=['GET'])
def snort_check_status():
    status = snort_check()
    return status


@app.route('/snort/api/v0.1/restartsnort', methods=['GET'])
def snort_restart_status():
    restart = snort_restart()
    return restart


@app.route('/snort/api/v0.1/startsnort', methods=['GET'])
def snort_start_status():
    start = snort_start()
    return start


@app.route('/snort/api/v0.1/stopsnort', methods=['GET'])
def snort_stop_status():
    stop = snort_stop()
    return stop


@app.route('/snort/api/v0.1/rules', methods=['GET', 'POST', 'PUT', 'DELETE'])
def rules():
    if request.method == 'POST':
        snort_json = request.json

        if pp_json_elements(snort_json):
            snort_json = norm_values(snort_json)

            snort_db_entry = db.search(snort_query.sid == snort_json["sid"])

            if snort_db_entry == []:
                snort_uuid = str(uuid.uuid4())
                snort_json['uuid'] = snort_uuid

#               "rule": "alert icmp any any -> any any (msg:'Pinging detected'; sid:101;)"
                rule_string = '%s %s %s %s -> %s %s (msg: "%s"; sid:%s;)' % (snort_json["rule_type"],
                                                                             snort_json["protocol"],
                                                                             snort_json["source_ip"],
                                                                             snort_json["source_port"],
                                                                             snort_json["dest_ip"],
                                                                             snort_json["dest_port"],
                                                                             snort_json["msg"],
                                                                             snort_json["sid"])

                with open("/etc/snort/rules/snort.rules", 'a+') as snort_rules:
                    snort_rules.write(rule_string + '\r\n')

                db.insert(snort_json)

                return jsonify(snort_json), 201
            else:
                snort_error = {"request_error": "matching sid found in DB, please change sid to proceed!"}
                return jsonify(snort_error), 400
        else:
            snort_error = {"request_error": "Please check your request for required elements!!!"}
            return jsonify(snort_error), 400

    elif request.method == 'DELETE':
        snort_json = request.json

        if del_json_elements(snort_json):
            snort_json = norm_values(snort_json)

            snort_db_entry = db.search(snort_query.sid == snort_json["sid"])
            if snort_db_entry == []:
                snort_error = {"request_error": "matching sid not found in DB!"}
                return jsonify(snort_error), 400
            else:
                snort_remove = snort_db_entry[0]
                removal_rule_string = '%s %s %s %s -> %s %s (msg: "%s"; sid:%s;)' % (snort_remove["rule_type"],
                                                                                     snort_remove["protocol"],
                                                                                     snort_remove["source_ip"],
                                                                                     snort_remove["source_port"],
                                                                                     snort_remove["dest_ip"],
                                                                                     snort_remove["dest_port"],
                                                                                     snort_remove["msg"],
                                                                                     snort_remove["sid"])

                with open("/etc/snort/rules/snort.rules", 'r') as snort_rules:
                    snort_lines = snort_rules.readlines()

                with open("/etc/snort/rules/snort.rules", 'w') as snort_rules:
                    for line in snort_lines:
                        if line != removal_rule_string + '\n':
                            snort_rules.write(line)

                db.remove(snort_query.sid == snort_json["sid"])
                return '', 204
        else:
            snort_error = {"request_error": "Please check your request for required elements!!!"}
            return jsonify(snort_error), 400

    elif request.method == 'PUT':
        snort_json = request.json

        if pp_json_elements(snort_json):
            snort_json = norm_values(snort_json)

            snort_db_entry = db.search(snort_query.sid == snort_json["sid"])
            if snort_db_entry == []:
                snort_error = {"request_error": "matching sid not found in DB!, Please provide matching sid."}
                return jsonify(snort_error), 400
            else:
                snort_old = snort_db_entry[0]
                old_rule_string = '%s %s %s %s -> %s %s (msg: "%s"; sid:%s;)' % (snort_old["rule_type"],
                                                                                 snort_old["protocol"],
                                                                                 snort_old["source_ip"],
                                                                                 snort_old["source_port"],
                                                                                 snort_old["dest_ip"],
                                                                                 snort_old["dest_port"],
                                                                                 snort_old["msg"],
                                                                                 snort_old["sid"])

                sid = snort_json["sid"]
                del snort_json["sid"]
                db.update(snort_json, snort_query.sid == sid)
                snort_json["sid"] = sid
                snort_json["uuid"] = snort_old["uuid"]

                new_rule_string = '%s %s %s %s -> %s %s (msg: "%s"; sid:%s;)' % (snort_json["rule_type"],
                                                                                 snort_json["protocol"],
                                                                                 snort_json["source_ip"],
                                                                                 snort_json["source_port"],
                                                                                 snort_json["dest_ip"],
                                                                                 snort_json["dest_port"],
                                                                                 snort_json["msg"],
                                                                                 snort_json["sid"])

                with open("/etc/snort/rules/snort.rules", 'r') as snort_rules:
                    snort_lines = snort_rules.readlines()

                with open("/etc/snort/rules/snort.rules", 'w') as snort_rules:
                    for line in snort_lines:
                        if line != old_rule_string + '\n':
                            snort_rules.write(line)
                    snort_rules.write(new_rule_string + '\r\n')

                return jsonify(snort_json), 202
        else:
            snort_error = {"request_error": "Please check your request for required elements!!!"}
            return jsonify(snort_error), 400

    elif request.method == 'GET':
        url_param = dict(request.args)
        get_param = list(url_param.keys())

        if get_url_elements(get_param):
            get_key = str(get_param[0])
            get_value = request.args[get_key]
            snort_find = db.search(snort_query[get_key] == get_value)
            return jsonify(snort_find)
        else:
            return jsonify(db.all()), 200


if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0")
