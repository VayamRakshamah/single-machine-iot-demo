# Security guidance

Local defaults prioritize demonstration: anonymous MQTT and an unauthenticated operator name are not production controls. The browser is restricted to configured origins, inputs are validated, errors are sanitized, and no secret is committed.

Production requires MQTT TLS/mTLS, unique gateway identities, least-privilege topic policies, certificate rotation/revocation, secret-managed database credentials, authenticated users, RBAC, immutable audit logs, rate limits, dependency scanning, network segmentation, egress controls, and monitoring. PLC connectivity must be vendor-approved, read-only, and isolated from control traffic. This demo must never issue commands to a physical machine.

Run `mvn org.owasp:dependency-check-maven:check`, `npm audit`, and an image scanner such as Trivy in the target environment.
