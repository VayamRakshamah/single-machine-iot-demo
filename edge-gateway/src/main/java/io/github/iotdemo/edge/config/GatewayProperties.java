package io.github.iotdemo.edge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gateway")
public record GatewayProperties(
        String plcHost, int plcPort, int plcUnitId, long pollIntervalMs,
        String mqttBrokerUri, String mqttTopic, String gatewayId, String plantId,
        String machineId, String bufferPath, String mqttUsername, String mqttPassword,
        boolean mqttTls, String mqttCaPath, String mqttCertificatePath, String mqttPrivateKeyPath) {
}

