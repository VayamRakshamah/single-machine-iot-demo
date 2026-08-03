package io.github.iotdemo.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="gateway_status")
public class GatewayStatus {
    @Id @Column(name="gateway_id") public String gatewayId;
    @Column(name="machine_id") public String machineId;
    public boolean connected;
    @Column(name="last_seen") public Instant lastSeen;
    @Column(name="updated_at") public Instant updatedAt;
    protected GatewayStatus() {}
}

