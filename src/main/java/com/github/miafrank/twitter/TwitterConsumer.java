package com.github.miafrank.twitter;

import org.apache.commons.lang3.SerializationException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;

public class TwitterConsumer {
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        final String TOPIC = "popular_tweets";
        final Logger logger = LoggerFactory.getLogger(TwitterConsumer.class);
        KafkaConsumer<String, Tweets> consumer = new TwitterConsumerProperties().createKafkaConsumer();
        try {
            consumer.subscribe(Collections.singletonList(TOPIC));
            while (true) {
                final ConsumerRecords<String, Tweets> records = consumer.poll(Duration.ofMillis(100));
                for (final ConsumerRecord<String, Tweets> record : records) {
                    final String key = record.key();
                    final Tweets value = record.value();
                    System.out.printf("key = %s, value = %s%n", key, value);
                }
            }
        } catch (SerializationException e) {
            logger.error("Serialization Error Occurred:" + e);
        }
    }
}