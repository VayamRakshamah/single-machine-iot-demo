# Offline buffering demonstration

1. Start the stack and observe telemetry on the dashboard.
2. Run `docker compose stop mosquitto`; leave the simulator and edge gateway running.
3. Wait at least 15 seconds. Inspect `edge_buffered_messages` at `http://localhost:8082/actuator/prometheus`; it should increase.
4. Run `docker compose start mosquitto`.
5. Watch edge logs with `docker compose logs -f edge-gateway`; queued rows publish oldest-first and the gauge returns to zero.
6. Query telemetry history and confirm sequence numbers are ordered. Re-publishing a captured payload with the same machine and sequence is counted by `iot_duplicate_messages_total` and does not add a row.

SQLite is stored in the `edge-data` volume and therefore survives an edge-container restart.
