# Popular Tweets
Stream tweets by keyword using Kafka Producer, Kafka Stream, and Dockerize HBase instance for persistence. 

## Setup
### Project setup - Intellij 
1. From the main menu, select `File` | `Open`.
   1. Alternatively, click `Open` or `Import` on the welcome screen.
1. In the dialog that opens, select the `pom.xml` file of the project you want to open.
Click **`OK`**.
1. In the dialog that opens, click `Open as Project`.

IntelliJ IDEA opens and syncs the Maven project in the IDE. 
If you need to adjust importing options when you open the project, refer to the 
[Maven](https://www.jetbrains.com/help/idea/maven1.importing.html) settings.

### Additional Setup
1. [Confluent Platform](https://www.confluent.io/download/?_ga=2.250927620.387832776.1589147004-1583349907.1588185083) 5.2 or later
1. [Confluent CLI](https://docs.confluent.io/current/cli/installing.html#cli-install)
1. Java 1.8 or 1.11 to run Confluent Platform
    1. MacOS Java 8 installation: 
        ```bash
        brew tap adoptopenjdk/openjdk
        brew cask install adoptopenjdk8
        ```
1. [Maven](https://maven.apache.org/) to compile the client Java code (If using Intellij -- Maven comes bundled in the IDE, so you can skip this step)
1. [Docker](https://www.docker.com/get-started)
1. Apache HBase Sink Connector (writes data from a topic in Kafka to a table in the specified HBase instance)
    ```bash
    confluent-hub install confluentinc/kafka-connect-hbase:latest
    ```
### Apply for Twitter Developer Account
Apply for a [Twitter Developer Account](https://developer.twitter.com/en/docs/basics/developer-portal/faq) to receive 
access tokens and keys to use Twitter API. When the tokens are received, keys and tokens can be generated by creating an `App` in the Twitter
Developer dashboard.

To crate an app:  
    1. `Apps` -> `Create an app` -> Fill out App details form.    
    2. Copy key and tokens into `twitter.properties` file. 

## Running the app
1. Create a Dockerized HBase Instance
    1. Get the Docker image: 
        ```bash
        docker pull aaionap/hbase:1.2.0
        ```
    2. Start the HBase Docker Image
        ```bash
        docker run -d --name hbase --hostname hbase -p 2182:2181 -p 8080:8080 -p 8085:8085 -p 9090:9090 -p 9095:9095 -p 16000:16000 -p 16010:16010 -p 16201:16201 -p 16301:16301 aaionap/hbase:1.2.0
        ```
    3. Add an entry `127.0.0.1   hbase` to `/etc/hosts`.
       
1. Run Kafka Producer and Kafka Stream
    ```bash
    sh run.sh
    ```
2. Check HBase for data: 
    1. Start HBase Shell: 
          ```bash
          docker exec -it hbase /bin/bash entrypoint.sh
          ```
    2. Verify the table `popular-tweets-avro` exists. Output should be: 
          ```bash
         TABLE
         example_table
         1 row(s) in 0.2750 seconds
         => ["popular-tweets-avro"]
          ```
    3. Verify table received data: 
        ```bash
        scan 'popular-tweets-avro'
        ```
3. Clean up resources
    1. Delete the connector `confluent local unload hbase`
    2. Stop Confluent: `confluent local stop`
    3. Delete Dockerized Hbase instance
        ```bash
           docker stop hbase
           docker rm -f hbase
        ```

## Kafka Producer
The producer ingests tweets from Twitter API configured by a list of search terms.
```java
public class TwitterProducer {
    List<String> terms = Lists.newArrayList("conspiracy", "conspiracyTheory", "fakenews");
}
```
All messages conform to a certain schema (class) defined in `/resources/avro/Tweets.avsc` before they are sent to Kafka topic in Avro format. 
```java
public Tweets(java.lang.CharSequence tweet, java.lang.CharSequence userName, java.lang.Integer userNumFollowers) {
    this.tweet = tweet;
    this.userName = userName;
    this.userNumFollowers = userNumFollowers;
  }
```
To create the code-generated class, compile the Java class from the Tweets.avsc file: 
```bash
mvn clean compile package
```

## Kafka Stream
The stream ingests messages from the Kafka topic and filters on user's number of followers. After filtering, 
the messages are persisted in a new Kafka Topic. 

## Dockerized Hbase Instance (Using Confluent HBase Sink Connector)
A single configuration file `hbase-avro.json` is used to configure which topic the Hbase instance should 
subscribe to (filtered kafka topic) in order to persist messages to an HBase table.