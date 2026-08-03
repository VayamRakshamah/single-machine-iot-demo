# Scaling to multiple machines

For hundreds of machines, replace fixed configuration with a gateway registry and per-machine register profiles. Partition telemetry by time and machine, apply retention/compression, batch database writes, and consider TimescaleDB or a managed time-series service. Run ingestion and alarm evaluation as horizontally scalable consumers using partitioned topics and distributed idempotency. Replace the in-process STOMP broker with a broker relay and apply tenant authorization, fleet health aggregation, and per-site backpressure.
