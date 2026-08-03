# MQTT contract

## Topics

- `factory/demo/plant-001/machine/DOCS-DEMO-001/telemetry` — complete one-second snapshots, QoS 1.
- `factory/demo/plant-001/machine/DOCS-DEMO-001/status` — retained gateway/machine connectivity status.
- `factory/demo/plant-001/machine/DOCS-DEMO-001/events` — discrete gateway events.

Telemetry uses schema version `1.0`, UTC ISO-8601 timestamps, and the durable `(machineId, sequenceNumber)` idempotency key. Receivers ignore unknown JSON fields and reject unsupported major schema versions. Local development allows anonymous MQTT; production requires TLS and a unique identity per gateway.

```json
{"schemaVersion":"1.0","gatewayId":"GATEWAY-DEMO-001","plantId":"PLANT-001","machineId":"DOCS-DEMO-001","timestamp":"2026-01-01T00:00:00Z","sequenceNumber":12345,"values":{"purity":93.82,"pressure":22.41,"temperature":67.3,"flow":38.1,"motorCurrent":14.2,"cycleCount":1503,"outputCount":12046,"running":true,"operatingMode":"PRODUCTION","alarmCode":"NONE"}}
```
