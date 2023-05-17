from diagrams import Cluster, Diagram
from diagrams.onprem.database import Couchdb
from diagrams.aws.database import RDS
from diagrams.aws.network import Route53
from diagrams.onprem.compute import Server
from diagrams.onprem.queue import Kafka

diagram_attr = {
    "newrank": "true"
}

kafka_cluster_attr = {
    "rank": "same"
}

kafka_connect_cluster_attr = {
    "rank": "same"
}

with Diagram("Kafkouch:: Couchdb connnector for Kafka", show=False, graph_attr=diagram_attr):

    # with Cluster("Couchdb Database Cluster"):
    #     db_global_changes = Couchdb("_global_changes")
    #     db_database1= Couchdb("database1")
    #     db_database2 = Couchdb("database2")
    #     db_database3 = Couchdb("database3")
    #     db_databaseN = Couchdb("databaseN")

    with Cluster("Kafka cluster",graph_attr=kafka_cluster_attr):
        kafka_topic1 = Kafka("Topic 1")
        kafka_topic2 = Kafka("Topic 2")
        kafka_topic3 = Kafka("Topic 3")
        kafka_topicN = Kafka("Topic N")

    with Cluster("Kafka connect cluster"):
        conn_source_connector = Server("Source Connector")
        conn_task1= Server("Task1")
        conn_task2 = Server("Task2")
        conn_taskN = Server("TaskN")




    [kafka_topic1, kafka_topic2] << conn_task1
    [kafka_topic3] << conn_task2
    [kafka_topicN] << conn_taskN

    # conn_source_connector >> db_global_changes
    # conn_task1 >> db_database1
    # conn_task1 >> db_database2

    # conn_task2 >> db_database3
    # conn_taskN >> db_databaseN


