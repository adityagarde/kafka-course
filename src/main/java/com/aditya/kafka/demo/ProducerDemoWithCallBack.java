package com.aditya.kafka.demo;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerDemoWithCallBack {

	public static void main(String[] args) {

		final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallBack.class);

		String bootstrapServers = "127.0.0.1:9092";

		// Set Producer properties
		Properties properties = new Properties();

		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// Create the Producer
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

		// Create a producer record
		for (int i = 0; i < 25; i++) {
			ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "Hello World!" + i);

			// Send Data - Asynchronous - happens in the background.
			producer.send(record, new Callback() {
				public void onCompletion(RecordMetadata metadata, Exception ex) {
					if (ex == null) {
						logger.info("\n Partition = " + metadata.partition() + "\n Offset = " + metadata.offset()
								+ "\n Topic = " + metadata.topic() + "\n TimeStamp = " + metadata.timestamp());
					} else {
						logger.error("Error Case", ex);
					}

				}
			});
		}
		// Flush the data
		producer.flush();

		// Flush and Close the data
		producer.close();
	}

}
