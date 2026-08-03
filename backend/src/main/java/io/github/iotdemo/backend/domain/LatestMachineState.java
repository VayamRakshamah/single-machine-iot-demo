package io.github.iotdemo.backend.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;

@Entity @Table(name="latest_machine_state")
public class LatestMachineState {
    @Id @Column(name="machine_id") public String machineId;
    @Column(name="sequence_number") public long sequenceNumber;
    @Column(name="recorded_at") public Instant recordedAt;
    @Column(name="received_at") public Instant receivedAt;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="values_json",columnDefinition="jsonb") public Map<String,Object> values;
    protected LatestMachineState() {}
    public LatestMachineState(String machineId) { this.machineId=machineId; }
}

