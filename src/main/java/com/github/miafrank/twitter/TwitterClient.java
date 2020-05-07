package com.github.miafrank.twitter;

import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class TwitterClient {
    public Client createTwitterClient(BlockingQueue<String> msgQueue, List<String> terms) {
        TwitterProperties twitterProperties = new TwitterProperties();

        // Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth)
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);

        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        hosebirdEndpoint.trackTerms(terms);

        // Twitter API credentials read from config file
        Authentication hosebirdAuth = new OAuth1(
                twitterProperties.getTwitter_consumer_key(),
                twitterProperties.getTwitter_consumer_secret(),
                twitterProperties.getTwitter_access_token(),
                twitterProperties.getTwitter_access_token_secret());

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue)); // use this if you want to process client events

        return builder.build();
    }
}
