package io.github.iotdemo.backend.repository;
import io.github.iotdemo.backend.domain.LatestMachineState;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LatestStateRepository extends JpaRepository<LatestMachineState,String> {}

