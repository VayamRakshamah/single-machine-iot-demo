# Architecture

The local data path mirrors common industrial telemetry boundaries while remaining safe and free to run. The simulated PLC is read-only from the gateway's perspective. SQLite provides an edge outbox, MQTT decouples transport, PostgreSQL retains history, and the browser combines REST snapshots with STOMP events.

All telemetry is simulated. A real deployment requires vendor-approved read-only PLC access, network segmentation, identity, TLS, monitoring, and an independent safety review.

