import os
from dotenv import load_dotenv
basedir = os.path.abspath(os.path.dirname(__file__))
load_dotenv(os.path.join(basedir, '.env'))

class Config(object):
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'i-ll-be-damned-for-sure-if-this-is-weak'
    CASSANDRA_USERNAME = os.environ.get('CASSANDRA_USERNAME')
    CASSANDRA_PASSWORD = os.environ.get('CASSANDRA_PASSWORD')
    CASSANDRA_CLUSTER_HOST = os.environ.get('CASSANDRA_CLUSTER_HOST')
    DEFAULT_EXTERNAL_SUBNET = "!$HOME_NET"
    DEFAULT_GLOBAL_TOPIC = "snoqttv5"
    DEFAULT_OINKCODE = os.environ.get('DEFAULT_OINKCODE')
    BASEDIR = basedir
