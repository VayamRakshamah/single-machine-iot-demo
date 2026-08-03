package io.github.iotdemo.backend.repository;
import io.github.iotdemo.backend.domain.TelemetryRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
public interface TelemetryRepository extends JpaRepository<TelemetryRecord,Long> {
 boolean existsByMachineIdAndSequenceNumber(String machineId,long sequenceNumber);
 List<TelemetryRecord> findByMachineIdAndRecordedAtBetweenOrderByRecordedAtDesc(String machineId, Instant from, Instant to, Pageable pageable);
}

