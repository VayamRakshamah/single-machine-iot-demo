package io.github.iotdemo.edge.config;

import io.github.iotdemo.edge.mqtt.MqttPublisher;
import io.github.iotdemo.edge.store.OutboxStore;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("edgeConnectivity")
public class EdgeHealthIndicator implements HealthIndicator {
    private final MqttPublisher mqtt; private final OutboxStore outbox;
    public EdgeHealthIndicator(MqttPublisher mqtt, OutboxStore outbox) { this.mqtt = mqtt; this.outbox = outbox; }
    @Override public Health health() {
        try { return Health.up().withDetail("mqttConnected", mqtt.connected()).withDetail("bufferedMessages", outbox.count()).build(); }
        catch (Exception error) { return Health.down(error).build(); }
    }
}

