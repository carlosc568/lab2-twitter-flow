package es.unizar.tmdad.lab2.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.Tweet;
import es.unizar.tmdad.lab2.domain.MyTweet;
import es.unizar.tmdad.lab2.domain.TargetedTweet;
import es.unizar.tmdad.lab2.service.TwitterLookupService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableIntegration
@IntegrationComponentScan
@ComponentScan
public class TwitterFlow {
	
	@Autowired
	private TwitterLookupService lookupService;
	
	@Bean
	public DirectChannel requestChannel() {
		return new DirectChannel();
	}

	// Tercer paso
	// Los mensajes se leen de "requestChannel" y se envian al método "sendTweet" del
	// componente "streamSendingService"
	@Bean
	public IntegrationFlow sendTweet() {
        //
        // CAMBIOS A REALIZAR:
        //
        // Usando Spring Integration DSL
		return IntegrationFlows.from(requestChannel())
				 // Filter --> asegurarnos que el mensaje es un Tweet
				.filter((Object o) -> o instanceof Tweet)
				 // Transform --> convertir un Tweet en un TargetedTweet con tantos tópicos como coincida
				.<Tweet,TargetedTweet>transform(t -> { MyTweet tweet = new MyTweet(t);
					List<String> topics = lookupService.getQueries().stream()
					.filter(q -> tweet.getUnmodifiedText().contains(q))
					.collect(Collectors.toList());
					return new TargetedTweet(tweet,topics);
				// Split --> dividir un TargetedTweet con muchos tópicos en tantos TargetedTweet como tópicos haya
				}).split(TargetedTweet.class, t -> {
					List<TargetedTweet> l = new ArrayList<TargetedTweet>(t.getTargets().size());
					for (String s : t.getTargets()) l.add(new TargetedTweet(t.getTweet(), s));
					return l;
				// Transform --> señalar el contenido de un TargetedTweet
				}).<TargetedTweet,TargetedTweet>transform(t -> {
					t.getTweet().setUnmodifiedText(t.getTweet().getUnmodifiedText().replaceAll(t.getFirstTarget(), "<big><strong>"+ t.getFirstTarget() +"</strong></big>"));
					return t;				
				}).handle("streamSendingService", "sendTweet").get();
	}

}

// Segundo paso
// Los mensajes recibidos por este @MessagingGateway se dejan en el canal "requestChannel"
@MessagingGateway(name = "integrationStreamListener", defaultRequestChannel = "requestChannel")
interface MyStreamListener extends StreamListener {

}
