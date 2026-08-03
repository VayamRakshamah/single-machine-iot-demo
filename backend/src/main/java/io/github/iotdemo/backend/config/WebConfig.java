package io.github.iotdemo.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.*;

@Configuration @EnableWebSocketMessageBroker
public class WebConfig implements WebSocketMessageBrokerConfigurer, WebMvcConfigurer {
    private final String origins;
    public WebConfig(@Value("${iot.allowed-origins}")String origins){this.origins=origins;}
    @Override public void configureMessageBroker(MessageBrokerRegistry registry){registry.enableSimpleBroker("/topic");registry.setApplicationDestinationPrefixes("/app");}
    @Override public void registerStompEndpoints(StompEndpointRegistry registry){registry.addEndpoint("/ws").setAllowedOrigins(origins.split(","));}
    @Override public void addCorsMappings(CorsRegistry registry){registry.addMapping("/api/**").allowedOrigins(origins.split(",")).allowedMethods("GET","POST","PUT");}
}

