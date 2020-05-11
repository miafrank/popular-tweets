#!/usr/bin/env bash

# Used to run kafka producer for certain amount of time
function timeout() { perl -e 'alarm shift; exec @ARGV' "$@"; }

# Start Kafka, Zookeeper, and Schema Registry.
confluent local stop connect && confluent local start connect
# Configure Hbase to read from Kafka topic
confluent local load hbase -- -d hbase-avro.json

# Run Kafka Topic
timeout 30 mvn exec:java -Dexec.mainClass=com.github.miafrank.twitter.TwitterProducer
# Run Kafka Stream that applies a filter to messages from TwitterProducer
timeout 60 exec:java -Dexec.mainClass=com.github.miafrank.twitter.TwitterStream

