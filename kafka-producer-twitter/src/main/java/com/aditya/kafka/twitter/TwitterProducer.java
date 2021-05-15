package com.aditya.kafka.twitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class TwitterProducer {

	Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());

	public TwitterProducer() {

	}

	public static void main(String[] args) {
		new TwitterProducer().run();
	}

	public void run() {

		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

		// Create a Twitter Client
		Client client = createTwitterClient(msgQueue);
		client.connect();

		// Create a Kafka Producer
		KafkaProducer<String, String> producer = createKafkaProducer();

		// Add a shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Stopping Client...");
			client.stop();
			logger.info("Closing Producer...");
			producer.close();
			logger.info("Done!");
		}));

		// Loop to send tweets to Kafka

		while (!client.isDone()) {
			String msg = null;
			try {
				msg = msgQueue.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				client.stop();
			}

			if (msg != null) {
				logger.info(msg);
				producer.send(new ProducerRecord<>("twitter_tweets", null, msg), new Callback() {
					@Override
					public void onCompletion(RecordMetadata metadata, Exception e) {
						if (e != null) {
							logger.error("Error Occured here", e);
						}
					}
				});
			}
		}
		logger.info("End Of Application");
	}

	Properties props = fetchTwitterKeys();

	String consumerKey = props.getProperty("consumerKey");
	String consumerSecret = props.getProperty("consumerSecret");
	String token = props.getProperty("token");
	String secret = props.getProperty("secret");

	List<String> terms = Lists.newArrayList("docker", "kafka", "kubernetes");

	public Client createTwitterClient(BlockingQueue<String> msgQueue) {

		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

		hosebirdEndpoint.trackTerms(terms);

		// These secrets should be read from a config file
		Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

		ClientBuilder builder = new ClientBuilder().name("Hosebird-Client-01") // optional: mainly for the logs
				.hosts(hosebirdHosts).authentication(hosebirdAuth).endpoint(hosebirdEndpoint)
				.processor(new StringDelimitedProcessor(msgQueue));

		Client hosebirdClient = builder.build();
		return hosebirdClient;

	}

	public KafkaProducer<String, String> createKafkaProducer() {

		String bootstrapServers = "127.0.0.1:9092";

		// Set Producer properties
		Properties properties = new Properties();

		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// creating Safe Producer
		properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		// properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
		properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
		properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

		// high throughput producer (compression and batching)
		properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
		properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
		properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));

		// Create the Producer
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

		return producer;

	}

	public Properties fetchTwitterKeys() {

		File configFile = new File("config.properties");
		Properties props = null;

		try {
			props = new Properties();
			FileReader reader = new FileReader(configFile);
			props.load(reader);

			reader.close();

		} catch (FileNotFoundException ex) {
			logger.info("config.properties not found.");
			ex.printStackTrace();
		} catch (IOException ex) {
			logger.info("Throwing generic IOException.");
			ex.printStackTrace();
		}

		return props;
	}
}
