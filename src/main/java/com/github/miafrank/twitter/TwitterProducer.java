package com.github.miafrank.twitter;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.twitter.hbc.core.Client;
import org.apache.commons.lang3.SerializationException;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {
    final Logger logger = LoggerFactory.getLogger(TwitterProducer.class);
    List<String> terms = Lists.newArrayList("conspiracy", "conspiracyTheory", "fakenews");

    public TwitterProducer() {
    }

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {
        logger.info("Setting up...");
        // Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);
        TwitterClient twitterClient = new TwitterClient();

        Client client = twitterClient.createTwitterClient(msgQueue, terms);
        client.connect();

        TwitterProducerProperties twitterProducerProperties = new TwitterProducerProperties();
        KafkaProducer<String, Tweets> producer = twitterProducerProperties.createKafkaProducer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Twitter Client");
            client.stop();
            producer.close();
            logger.info("Shutting down Producer");
        }));

        // on a different thread, or multiple different threads....
        // TODO clean up this code -- it's gross
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }
            if (msg != null) {
                logger.info("Got tweet from Twitter API...");

                JsonObject jsonObject = new Gson().fromJson(msg, JsonObject.class);
                String text = jsonObject.get("text").getAsString();

                JsonObject userObj = jsonObject.get("user").getAsJsonObject();
                String userName = userObj.get("name").getAsString();
                String userNumFollowers = userObj.get("followers_count").getAsString();
                String userId = userObj.get("id_str").getAsString();
                String topic = "popular_tweets";

                final Tweets tweet = new Tweets(text, userName, userNumFollowers);
                final ProducerRecord<String, Tweets> record = new ProducerRecord<>(topic, userId, tweet);
                logger.info("Adding message to topic: " + topic + " with id: " + userId);

                try {
                    producer.send(record, new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                            if (e != null) {
                                logger.error("Something went wrong...", e);
                            }
                        }
                    });
                } catch (SerializationException e) {
                    logger.error("Serialization Exception! " + e);
                }
            }
        }
        logger.info("End of application");
    }
}
