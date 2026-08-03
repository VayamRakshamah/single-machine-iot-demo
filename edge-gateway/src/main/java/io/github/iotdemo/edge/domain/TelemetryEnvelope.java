package io.github.iotdemo.edge.domain;

import java.time.Instant;

public record TelemetryEnvelope(
        String schemaVersion, String gatewayId, String plantId, String machineId,
        Instant timestamp, long sequenceNumber, Values values) {
    public record Values(
            double purity, double pressure, double temperature, double flow,
            double motorCurrent, long cycleCount, long outputCount, boolean running,
            String operatingMode, String alarmCode) {
    }
}

