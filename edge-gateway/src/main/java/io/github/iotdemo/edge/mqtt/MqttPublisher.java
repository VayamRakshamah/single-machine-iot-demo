package io.github.iotdemo.edge.mqtt;

import io.github.iotdemo.edge.config.GatewayProperties;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Component;

@Component
public class MqttPublisher {
    private final GatewayProperties properties;
    private MqttAsyncClient client;

    public MqttPublisher(GatewayProperties properties) { this.properties = properties; }

    public synchronized void publish(String payload) throws MqttException {
        ensureConnected();
        client.publish(properties.mqttTopic(), payload.getBytes(), 1, false).waitForCompletion(3000);
    }

    public boolean connected() { return client != null && client.isConnected(); }

    private void ensureConnected() throws MqttException {
        if (connected()) return;
        client = new MqttAsyncClient(properties.mqttBrokerUri(), properties.gatewayId(), new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true); options.setCleanSession(false); options.setConnectionTimeout(3);
        if (properties.mqttUsername() != null && !properties.mqttUsername().isBlank()) {
            options.setUserName(properties.mqttUsername());
            options.setPassword(properties.mqttPassword().toCharArray());
        }
        client.connect(options).waitForCompletion(5000);
    }

    @PreDestroy
    void close() throws MqttException { if (connected()) client.disconnect(); }
}

