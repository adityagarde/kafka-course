# kafka-course

Kafka Consumer and Producer implementation.

1. Starting and running kafka consumer and producer from CLI. (kafka-CLI-commands)
2. Basic java implementation. (kafka-basics)
3. Real time producing data from Twitter API. (kafka-producer-twitter)
4. Reading the fetched data from twitter in Elasticsearch. (kafka-consumer-elasticsearch)
5. Producing real time data from Twitter Source using Kafka Connect. (kafka-connect)
6. Filtering tweets using Kafka Streams. (kafka-streams-filter-tweets)

I have taken detailed notes. They are a bit rusty as they were taken on go. (kafka-notes)

References - 
  * For Twitter dependency refer - https://github.com/twitter/hbc
  * Kafka Connect Twitter jars are taken from here - https://github.com/jcustenborder/kafka-connect-twitter
      - Always take the latest release.
  * Docker images for Kafka environment with multiple combinations are available here. https://github.com/conduktor/kafka-stack-docker-compose
