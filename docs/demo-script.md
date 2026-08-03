# Five-minute demonstration script

**0:00–0:45 — Context.** Open Overview, point to “Simulated Data,” and explain the read-only PLC-to-dashboard path.

**0:45–1:30 — Live telemetry.** Show the four charts, changing values, running animation, WebSocket live status, and last timestamp.

**1:30–2:45 — Alarm lifecycle.** Open Scenarios, choose Low purity, explain the ten-second persistence rule, open Alarms, acknowledge the new critical alarm, return to Normal, and show it clear.

**2:45–3:45 — Resilience.** Stop Mosquitto, show the stale/connection state and increasing SQLite buffer metric, restart Mosquitto, then show ordered backfill and recovery.

**3:45–4:30 — Engineering detail.** Open Architecture and OpenAPI. Explain Modbus scaling, MQTT QoS 1, backend idempotency, JSONB parameter extensibility, and PostgreSQL history.

**4:30–5:00 — Production path.** Explain physical PLC read-only integration, TLS/identity/RBAC requirements, optional AWS IoT Core configuration, and scaling limitations.
