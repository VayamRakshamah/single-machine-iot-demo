# ADR 003: SQLite transactional outbox

**Status:** Accepted

Persist an envelope and durable sequence before MQTT publication. Delete or mark it delivered only after QoS 1 acknowledgement. SQLite supplies ordering, transactions, restart recovery, uniqueness, and easy inspection without external edge infrastructure.

