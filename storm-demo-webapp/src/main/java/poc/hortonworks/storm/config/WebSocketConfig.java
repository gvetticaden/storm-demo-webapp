package poc.hortonworks.storm.config;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.StompBrokerRelayRegistration;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import poc.hortonworks.storm.config.service.AppConfigService;


@Configuration
@org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
@EnableScheduling
@ComponentScan(basePackages="poc.hortonworks.storm")
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	
	@Autowired
	private AppConfigService appConfigSerice;
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/monitor").withSockJS();
		registry.addEndpoint("/streamgenerator").withSockJS();
		registry.addEndpoint("/resetdemo").withSockJS();
	}

	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		StompBrokerRelayRegistration registration = registry.enableStompBrokerRelay("/queue", "/topic");
		String activeMQHost = appConfigSerice.getActiveMQHost();
		registration.setRelayHost(activeMQHost);
		registration.setRelayPort(61613);
		registry.setApplicationDestinationPrefixes("/app");
	}	



	@Override
	public void configureClientInboundChannel(ChannelRegistration inboundChannel) {
		
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration out) {
		
	}
	

}
