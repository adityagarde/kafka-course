## Starting topic with 3 partitions and replication factor as 1
kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1

##Listing topics 
kafka-topics --zookeeper 127.0.0.1:2181 --list

##Describing topics
kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --describe

kafka-topics --zookeeper 127.0.0.1:2181 --topic second_topic --create --partitions 6 --replication-factor 1

##Deleting topics delete.topic.enable should be set to true for deletion.
kafka-topics --zookeeper 127.0.0.1:2181 --topic second_topic --delete