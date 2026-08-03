# ADR 001: JSONB telemetry values

**Status:** Accepted

Store indexed machine, sequence, and timestamp metadata as columns and parameter values in PostgreSQL JSONB. This lets definitions change without migrations and is appropriate for one machine. A fleet deployment should evaluate time partitioning, TimescaleDB or a dedicated time-series platform, retention tiers, and asynchronous ingestion.

