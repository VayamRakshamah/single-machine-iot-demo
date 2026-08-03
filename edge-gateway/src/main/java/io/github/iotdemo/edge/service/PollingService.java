package io.github.iotdemo.edge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.iotdemo.edge.config.GatewayProperties;
import io.github.iotdemo.edge.domain.TelemetryEnvelope;
import io.github.iotdemo.edge.modbus.ModbusTcpClient;
import io.github.iotdemo.edge.mqtt.MqttPublisher;
import io.github.iotdemo.edge.store.OutboxStore;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PollingService {
    private static final Logger log = LoggerFactory.getLogger(PollingService.class);
    private final GatewayProperties properties; private final ModbusTcpClient modbus;
    private final OutboxStore outbox; private final MqttPublisher mqtt; private final ObjectMapper json;
    private final Counter published; private final Counter failures;
    private final AtomicBoolean modbusConnected = new AtomicBoolean();

    public PollingService(GatewayProperties properties, ModbusTcpClient modbus, OutboxStore outbox,
                          MqttPublisher mqtt, ObjectMapper json, MeterRegistry registry) {
        this.properties = properties; this.modbus = modbus; this.outbox = outbox; this.mqtt = mqtt; this.json = json;
        published = registry.counter("edge_messages_published_total");
        failures = registry.counter("edge_poll_failures_total");
        Gauge.builder("edge_buffered_messages", outbox, store -> { try { return store.count(); } catch (Exception e) { return -1; } }).register(registry);
        Gauge.builder("edge_modbus_connected", modbusConnected, value -> value.get() ? 1 : 0).register(registry);
    }

    @Scheduled(fixedDelayString = "${gateway.poll-interval-ms:1000}")
    public void poll() {
        try {
            var value = modbus.readSnapshot(); modbusConnected.set(true);
            validate(value);
            long sequence = outbox.nextSequence();
            String alarm = value.critical() ? "LOW_PURITY" : value.warning() ? "PROCESS_WARNING" : "NONE";
            var envelope = new TelemetryEnvelope("1.0", properties.gatewayId(), properties.plantId(), properties.machineId(),
                    Instant.now(), sequence, new TelemetryEnvelope.Values(value.purity(), value.pressure(), value.temperature(),
                    value.flow(), value.motorCurrent(), value.cycleCount(), value.outputCount(), value.running(),
                    value.running() ? "PRODUCTION" : "STOPPED", alarm));
            outbox.enqueue(sequence, json.writeValueAsString(envelope));
        } catch (Exception error) {
            modbusConnected.set(false); failures.increment(); log.warn("Modbus poll failed: {}", error.getMessage());
        }
        drain();
    }

    void drain() {
        try {
            for (var item : outbox.pending(100)) {
                mqtt.publish(item.payload()); outbox.acknowledge(item.sequenceNumber()); published.increment();
            }
        } catch (Exception error) { log.warn("MQTT unavailable; telemetry remains buffered: {}", error.getMessage()); }
    }

    static void validate(ModbusTcpClient.Snapshot value) {
        if (value.purity() < 0 || value.purity() > 100 || value.pressure() < 0 || value.pressure() > 40
                || value.temperature() < -20 || value.temperature() > 150 || value.flow() < 0 || value.flow() > 100
                || value.motorCurrent() < 0 || value.motorCurrent() > 50) throw new IllegalArgumentException("PLC value outside valid range");
    }
}

