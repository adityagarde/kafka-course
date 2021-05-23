##Kafka Consumer Groups CLI

##List the consumer groups
kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --list

##Describes the details
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-second-group

##Resetting Offsets --reset-offsets

##No action will be performed as the --execute option is missing
kafka-consumer-groups --bootstrap-server localhost:9092 --group my-first-group --reset-offsets --to-earliest

## resets the offsets to 0 for all partitions of the said topic.
kafka-consumer-groups --bootstrap-server localhost:9092 --group my-first-group --reset-offsets --to-earliest --execute --topic first_topic

kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-first-group

## --shift-by 2 -> Will shift it 2 forward && --shift-by -2 -> Will shift it 2 backward
kafka-consumer-groups --bootstrap-server localhost:9092 --group my-first-group --reset-offsets --shift-by 2 --execute --topic first_topic