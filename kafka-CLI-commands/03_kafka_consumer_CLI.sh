## basic syntax of consumer CLI
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic

## reading all the messages from beginning.
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from_beginning


##Kafka Console Consumers in groups

##use --group
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-group

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-second-group --from-beginning