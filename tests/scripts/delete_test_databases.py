import requests

# CouchDB connection settings
COUCHDB_SERVER = 'http://localhost:5984'
COUCHDB_USERNAME = 'admin'
COUCHDB_PASSWORD = 'admin'
# Number of databases to create
NUM_DATABASES = 20
# List of database names to delete
database_names = ['database1', 'database2', 'database3']

# Delete databases
databases = []
for i in range(NUM_DATABASES):
    db_name = f'database{i+1}'
    url = f'{COUCHDB_SERVER}/{db_name}'
    response = requests.delete(url, auth=(COUCHDB_USERNAME, COUCHDB_PASSWORD))
    if response.status_code == 200:
        print(f'Deleted database {db_name}.')
    else:
        print(f'Failed to delete database {db_name}.')
