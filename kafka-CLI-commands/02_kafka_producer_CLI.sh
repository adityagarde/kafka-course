## Starting Kafka producer from CLI
kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic

## use --producer-property to add various properties.
kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all

## When properties mentioned then default PartitionCount: 1	ReplicationFactor: 1.
kafka-console-producer --broker-list 127.0.0.1:9092 --topic new_topic