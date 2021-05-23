## Creating a topic
kafka-topics --zookeeper 127.0.0.1:2181 --create --topic configured-topic --partitions 3 --replication-factor 1

## Describe the topc
kafka-configs --zookeeper 127.0.0.1:2181 --entity-type topics --entity-name configured-topic --describe

## Adding configs at topic level
kafka-configs --zookeeper 127.0.0.1:2181 --entity-type topics --entity-name configured-topic --add-config min.insync.replicas=2 --alter

## Deleting config assigned to topic
kafka-configs --zookeeper 127.0.0.1:2181 --entity-type topics --entity-name configured-topic --delete-config min.insync.replicas --alter