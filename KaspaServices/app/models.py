from app import app
from cassandra.cqlengine import columns, models, connection
from cassandra.cqlengine.models import Model
from datetime import datetime
from passlib.apps import custom_app_context as pwd_context
from itsdangerous import (TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired)

import uuid, os

connection.set_default_connection('clusterKaspa')
models.DEFAULT_KEYSPACE = 'kaspa'

class User(Model):
    username = columns.Text(primary_key=True)
    first_name = columns.Text()
    last_name = columns.Text()
    email = columns.Text()
    password_hash = columns.Text()
    group = columns.Text(default="client")
    company = columns.Text()
    time_joined = columns.DateTime(default=datetime.now())

    def hash_password(self, password):
        self.password_hash = pwd_context.encrypt(password)

    def verify_password(self, password):
        return pwd_context.verify(password, self.password_hash)
    
    def set_admin(self):
        self.group = "admin"

    def generate_auth_token(self):
        EXPIRES_IN_A_YEAR = 365 * 24 * 60 * 60
        s = Serializer(app.config['SECRET_KEY'], expires_in=EXPIRES_IN_A_YEAR)
        return s.dumps({'username': self.username})

    @staticmethod
    def verify_auth_token(token):
        s = Serializer(app.config['SECRET_KEY'])
        try:
            data = s.loads(token)
        except SignatureExpired:
            return None
        except BadSignature:
            return None
        user = User.objects.filter(username=data['username']).first()
        return user

class Sensor(Model):
    company = columns.Text(primary_key=True)
    device_id = columns.Text(primary_key=True, clustering_order="ASC")
    device_name = columns.Text()
    hostname = columns.Text()
    ip_address = columns.Text()
    location = columns.Text()
    protected_subnet = columns.Text()
    external_subnet = columns.Text(default=app.config['DEFAULT_EXTERNAL_SUBNET'])
    oinkcode = columns.Text(default=app.config['DEFAULT_OINKCODE'])
    topic_global = columns.Text(default=app.config['DEFAULT_GLOBAL_TOPIC'])
    topic_cmd = columns.Text()
    topic_resp = columns.Text()
    sensor_key = columns.UUID(default=uuid.uuid4())
    time_created = columns.DateTime(default=datetime.now())

    def create_dev_id(self, device_name):
        self.device_id = "{}-{}".format(device_name, uuid.uuid4())
    
    def create_topic_cmd(self):
        self.topic_cmd = "cmd-{}".format(uuid.uuid4())
    
    def create_topic_resp(self):
        self.topic_resp = "resp-{}".format(uuid.uuid4())

    def set_external_subnet(self, external_subnet):
        self.external_subnet = external_subnet
    
    def set_oinkcode(self, oinkcode):
        self.external_subnet = oinkcode

    


