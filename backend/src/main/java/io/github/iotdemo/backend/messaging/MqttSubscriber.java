package io.github.iotdemo.backend.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.iotdemo.backend.service.TelemetryService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.validation.Validator;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttSubscriber {
    private static final Logger log=LoggerFactory.getLogger(MqttSubscriber.class);
    private final String uri,topic;private final ObjectMapper json;private final Validator validator;private final TelemetryService service;private MqttClient client;
    public MqttSubscriber(@Value("${iot.mqtt-broker-uri}")String uri,@Value("${iot.mqtt-topic}")String topic,ObjectMapper json,Validator validator,TelemetryService service){this.uri=uri;this.topic=topic;this.json=json;this.validator=validator;this.service=service;}
    @PostConstruct void connect(){
        try{client=new MqttClient(uri,"backend-DOCS-DEMO-001");MqttConnectOptions options=new MqttConnectOptions();options.setAutomaticReconnect(true);options.setCleanSession(false);client.connect(options);client.subscribe(topic,1,(t,p)->consume(p));}
        catch(MqttException error){log.error("Initial MQTT connection failed; automatic recovery unavailable until restart",error);}
    }
    void consume(MqttMessage mqtt){try{TelemetryMessage message=json.readValue(mqtt.getPayload(),TelemetryMessage.class);var violations=validator.validate(message);if(!violations.isEmpty()){log.warn("Rejected invalid telemetry: {}",violations);return;}service.ingest(message);}catch(Exception error){log.warn("Rejected MQTT payload: {}",error.getMessage());}}
    @PreDestroy void close()throws Exception{if(client!=null&&client.isConnected())client.disconnect();}
}

