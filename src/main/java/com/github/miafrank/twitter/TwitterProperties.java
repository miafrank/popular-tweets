package com.github.miafrank.twitter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class TwitterProperties {
    @Getter
    private String twitter_access_token;
    @Getter
    private String twitter_access_token_secret;
    @Getter
    private String twitter_consumer_key;
    @Getter
    private String twitter_consumer_secret;

    public TwitterProperties() {
        Logger logger = LoggerFactory.getLogger(TwitterProperties.class.getName());
        try {
            InputStream inputStream = new FileInputStream("twitter.properties");
            Properties properties = new Properties();

            properties.load(inputStream);

            this.twitter_access_token = properties.getProperty("twitter.oauth.accessToken");
            this.twitter_access_token_secret = properties.getProperty("twitter.oauth.accessTokenSecret");
            this.twitter_consumer_key = properties.getProperty("twitter.oauth.consumerKey");
            this.twitter_consumer_secret = properties.getProperty("twitter.oauth.consumerSecret");

        } catch (IOException e) {
            logger.error("Error with fetching credentials:" + e.getMessage());
        }
    }
}
