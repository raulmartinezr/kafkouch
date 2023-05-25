import random
import string
import time
from couchdb import Server
import requests  # pip install couchdb

# CouchDB connection settings
COUCHDB_SERVER = "http://localhost:5984"
COUCHDB_USERNAME = "admin"
COUCHDB_PASSWORD = "admin"

# Number of databases to create
NUM_DATABASES = 20

# Documents per minute rate
DOCS_PER_MINUTE = 10000

# Document field lengths
FIELD_LENGTH = 10

doc_types = ["user", "visit", "access"]

# Generate random alphanumeric string
def generate_random_string(length):
    letters = string.ascii_letters + string.digits
    return "".join(random.choice(letters) for _ in range(length))


def generate_random_doc_type(length):
    letters = string.ascii_letters + string.digits
    return random.choice(doc_types)


# Create random documents
def generate_random_documents():
    docs = []
    for _ in range(DOCS_PER_MINUTE):
        doc = {
            "doc_type": random.choice(doc_types),
            "field1": generate_random_string(FIELD_LENGTH),
            "field2": generate_random_string(FIELD_LENGTH),
            "field3": generate_random_string(FIELD_LENGTH),
        }
        docs.append(doc)
    return docs


# Connect to CouchDB server
server = Server(COUCHDB_SERVER)
server.resource.credentials = (COUCHDB_USERNAME, COUCHDB_PASSWORD)


# Create global changes database if it doesn't exist
global_chanfes_db_name = "_global_changes"
url = f"{COUCHDB_SERVER}/{global_chanfes_db_name}"
response = requests.head(url, auth=(COUCHDB_USERNAME, COUCHDB_PASSWORD))
if response.status_code == 200:
    print(f"Database {global_chanfes_db_name} exists.")
else:
    print(f"Database {global_chanfes_db_name} does not exist.")
    server.create(global_chanfes_db_name)


# Create databases
databases = []
for i in range(NUM_DATABASES):
    db_name = f"database{i+1}"
    db = server.create(db_name)
    databases.append(db)
    print(f"Created database {db_name}.")


# Constant document updates
while True:
    for db in databases:
        documents = generate_random_documents()
        db.update(documents)
        print(f"Inserted {DOCS_PER_MINUTE} documents into database {db.name}.")
    time.sleep(60 / DOCS_PER_MINUTE)  # Adjust rate by dividing 60 by documents per minute
