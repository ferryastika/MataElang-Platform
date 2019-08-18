from flask import Flask
from flask_httpauth import HTTPBasicAuth
from config import Config
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
from cassandra.cqlengine import connection

app = Flask(__name__)
auth = HTTPBasicAuth()
app.config.from_object(Config)

cassandra_auth_provider = PlainTextAuthProvider(username=app.config['CASSANDRA_USERNAME'], password=app.config['CASSANDRA_PASSWORD'])
cluster = Cluster([app.config['CASSANDRA_CLUSTER_HOST']], auth_provider = cassandra_auth_provider)
session = cluster.connect()
session.set_keyspace('kaspa')

connection.register_connection('clusterKaspa', session=session)

from app import routes, models