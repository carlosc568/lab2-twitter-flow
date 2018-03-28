package es.unizar.tmdad.lab2.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
 
@Configuration
public class TwitterConfigurationTemplate {
private String consumerKey = System.getenv("twitter.consumerKey");

    
    private String consumerSecret = System.getenv("twitter.consumerSecret");

    
    private String accessToken = System.getenv("twitter.accessToken");

    
    private String accessTokenSecret = System.getenv("twitter.accessTokenSecret");
 
    @Bean
    public TwitterTemplate twitterTemplate() {
        return new TwitterTemplate(consumerKey,
                consumerSecret, accessToken, accessTokenSecret);
    }
}