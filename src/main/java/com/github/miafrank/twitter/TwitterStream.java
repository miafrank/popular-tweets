package com.github.miafrank.twitter;

import com.google.gson.JsonParser;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class TwitterStream {
    final Logger logger = LoggerFactory.getLogger(TwitterStream.class);

    public TwitterStream() {}

    public static void main(String[] args) {
        new TwitterStream().run();
    }

    public void run() {
        final String topic = "popular_tweets";

        Properties streamProperties = new TwitterStreamProperties().createTwitterStreamProperties();
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, Tweets> tweetsKStream = streamsBuilder.stream(topic);

        KStream<String, Tweets> popularTweetsKStream =
                tweetsKStream.filter((userId, tweet) -> tweet.getUserNumFollowers() > 10);

        popularTweetsKStream.to("filtered_popular_tweets");

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), streamProperties);

        try {
            kafkaStreams.cleanUp();
            kafkaStreams.start();
            logger.info("Started kafka stream");
        } catch (StreamsException e) {
            logger.error("Streaming error occurred");
        }
        // Close Kafka Stream
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }
}
