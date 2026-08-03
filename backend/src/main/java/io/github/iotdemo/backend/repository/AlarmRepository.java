package io.github.iotdemo.backend.repository;
import io.github.iotdemo.backend.domain.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface AlarmRepository extends JpaRepository<Alarm,Long> {
 List<Alarm> findByMachineIdOrderByStartTimeDesc(String machineId);
 Optional<Alarm> findFirstByMachineIdAndTypeAndStatusIn(String machineId, Alarm.AlarmType type, List<Alarm.AlarmStatus> statuses);
 long countByStatusIn(List<Alarm.AlarmStatus> statuses);
}

