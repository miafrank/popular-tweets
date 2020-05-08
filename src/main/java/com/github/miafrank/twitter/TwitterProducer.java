package com.github.miafrank.twitter;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitter.hbc.core.Client;
import org.apache.commons.lang3.SerializationException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {
    final Logger logger = LoggerFactory.getLogger(TwitterProducer.class);
    final String topic = "popular_tweets";
    List<String> terms = Lists.newArrayList("conspiracy", "conspiracyTheory", "fakenews");

    public TwitterProducer() {
    }

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {
        logger.info("Setting up Twitter Client and Kafka Producer");

        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);
        TwitterClient twitterClient = new TwitterClient();

        Client client = twitterClient.createTwitterClient(msgQueue, terms);
        client.connect();

        TwitterProducerProperties twitterProducerProperties = new TwitterProducerProperties();
        KafkaProducer<String, Tweets> producer = twitterProducerProperties.createKafkaProducer();

        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                logger.error("Exception: " + e.getClass().getName() + "occurred. Message Queue timed out");
                client.stop();
            }
            if (msg != null) {
                logger.info("Got tweet from Twitter API...");

                JsonObject jsonObject = new Gson().fromJson(msg, JsonObject.class);
                JsonObject userObj = jsonObject.get("user").getAsJsonObject();
                String userId = userObj.get("id_str").getAsString();

                final Tweets tweet = new Tweets(jsonObject.get("text").getAsString(),
                                                userObj.get("name").getAsString(),
                                                userObj.get("followers_count").getAsString());

                final ProducerRecord<String, Tweets> record = new ProducerRecord<>(topic, userId, tweet);
                logger.info("Adding message to topic: " + topic + " with id: " + userId);

                producer.send(record);
                try {
                    producer.send(record, (recordMetadata, e) -> {
                        if (e != null) {
                            logger.error("Something went wrong." + e);
                        }
                    });
                } catch (SerializationException e) {
                    logger.error("Serialization Exception! " + e);
                }
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Twitter Client");
            client.stop();
            producer.close();
            logger.info("Shutting down Producer");
        }));
    }
}
