package com.aditya.kafka.demo;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerDemoWithThreads {

	public static void main(String[] args) {
		new ConsumerDemoWithThreads().run();
	}

	private void run() {

		Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class.getName());

		String bootstrapServer = "127.0.0.1:9092";
		String groupId = "my-sixth-group";
		String topic = "first_topic";

		CountDownLatch latch = new CountDownLatch(1);

		logger.info("Creating the consumer thread");

		Runnable myConsumerRunnable = new ConsumerRunnable(bootstrapServer, groupId, topic, latch);

		// Start the thread
		Thread th1 = new Thread(myConsumerRunnable);
		th1.start();

		// Add a shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Caught shutdown hook");
			((ConsumerRunnable) myConsumerRunnable).shutdown();
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("Application exited");
		}

		));

		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Applicatio got interrupted", e);
			e.printStackTrace();
		} finally {
			logger.info("Application closed : finally.");
		}

	}

	public class ConsumerRunnable implements Runnable {

		private CountDownLatch latch;
		private KafkaConsumer<String, String> consumer;
		private Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class.getName());

		public ConsumerRunnable(String bootstrapServer, String groupId, String topic, CountDownLatch latch) {
			this.latch = latch;

			Properties properties = new Properties();
			properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
			properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

			properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
			consumer = new KafkaConsumer<String, String>(properties);

			consumer.subscribe(Arrays.asList(topic));
		}

		public void run() {

			try {
				while (true) {
					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

					for (ConsumerRecord<String, String> record : records) {
						logger.info("\n Key = " + record.key() + " \n Value = " + record.value() + " \n Partition = "
								+ record.partition() + " \n Offset = " + record.offset());
					}
				}
			} catch (WakeupException e) {
				logger.info("Shutdown signal received.");
			} finally {
				consumer.close();
				latch.countDown();
			}

		}

		public void shutdown() {
			consumer.wakeup();
		}

	}

}
