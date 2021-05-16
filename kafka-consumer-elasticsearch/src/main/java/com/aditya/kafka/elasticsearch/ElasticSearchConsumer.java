package com.aditya.kafka.elasticsearch;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

public class ElasticSearchConsumer {

	public static RestHighLevelClient createClient() {

		String hostname = "kafka-course-4033486005.ap-southeast-2.bonsaisearch.net";
		String username = "a3p52t8huw";
		String password = "vvyr72pvpx";

		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

		RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 443, "https"))
				.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {

					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
						return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
					}
				});
		RestHighLevelClient client = new RestHighLevelClient(builder);

		return client;

	}

	public static void main(String[] args) throws IOException {

		Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());

		RestHighLevelClient client = createClient();

		KafkaConsumer<String, String> consumer = createConsumer("twitter_tweets");

		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

			logger.info("Received - " + records.count());

			for (ConsumerRecord<String, String> record : records) {

				// Generic ID
				// String id = record.topic() + "_" + record.partition() + "_" +
				// record.offset();

				// Getting id_str from twitter JSON
				String id = extractStringId(record.value());

				// Inserting data in ElasticSearch
				IndexRequest indexRequest = new IndexRequest("twitter").id(id).source(record.value(),
						XContentType.JSON);

				IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);

				logger.info(indexResponse.getId());

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			logger.info("Commiting Offsets.");
			consumer.commitSync();
			logger.info("Offsets Committed");
		}

		// client.close();

	}

	public static KafkaConsumer<String, String> createConsumer(String topic) {

		String bootstrapServer = "127.0.0.1:9092";
		String groupId = "kafka-demo-elasticsearch";

		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// disabling auto-commit
		properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

		consumer.subscribe(Arrays.asList(topic));

		return consumer;

	}

	private static String extractStringId(String jsonString) {

		JsonParser jsonParser = new JsonParser();

		return jsonParser.parse(jsonString).getAsJsonObject().get("id_str").getAsString();

	}

}
