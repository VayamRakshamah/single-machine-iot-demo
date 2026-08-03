package io.github.iotdemo.backend.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.Map;

@Entity @Table(name="telemetry_record", uniqueConstraints=@UniqueConstraint(columnNames={"machine_id","sequence_number"}))
public class TelemetryRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(name="machine_id") public String machineId;
    @Column(name="gateway_id") public String gatewayId;
    @Column(name="sequence_number") public long sequenceNumber;
    @Column(name="recorded_at") public Instant recordedAt;
    @Column(name="received_at") public Instant receivedAt;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name="values_json", columnDefinition="jsonb") public Map<String,Object> values;
    protected TelemetryRecord() {}
    public TelemetryRecord(String machineId,String gatewayId,long sequenceNumber,Instant recordedAt,Instant receivedAt,Map<String,Object> values) {
        this.machineId=machineId; this.gatewayId=gatewayId; this.sequenceNumber=sequenceNumber; this.recordedAt=recordedAt; this.receivedAt=receivedAt; this.values=values;
    }
}

