package io.github.iotdemo.backend.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Alarm {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(name="machine_id") public String machineId;
    @Enumerated(EnumType.STRING) public AlarmType type;
    @Enumerated(EnumType.STRING) public Severity severity;
    @Enumerated(EnumType.STRING) public AlarmStatus status;
    @Column(name="start_time") public Instant startTime;
    @Column(name="current_value") public Double currentValue;
    public Double threshold;
    @Column(name="acknowledgement_user") public String acknowledgementUser;
    @Column(name="acknowledgement_time") public Instant acknowledgementTime;
    @Column(name="acknowledgement_comment") public String acknowledgementComment;
    @Column(name="cleared_time") public Instant clearedTime;
    protected Alarm() {}
    public Alarm(String machineId, AlarmType type, Severity severity, Instant startTime, Double value, Double threshold) {
        this.machineId=machineId; this.type=type; this.severity=severity; this.status=AlarmStatus.ACTIVE; this.startTime=startTime; this.currentValue=value; this.threshold=threshold;
    }
    public enum AlarmType { LOW_PURITY, HIGH_TEMPERATURE, LOW_PRESSURE, COMMUNICATION_LOST }
    public enum Severity { WARNING, CRITICAL }
    public enum AlarmStatus { ACTIVE, ACKNOWLEDGED, CLEARED }
}

