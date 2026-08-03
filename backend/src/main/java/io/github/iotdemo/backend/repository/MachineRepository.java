package io.github.iotdemo.backend.repository;
import io.github.iotdemo.backend.domain.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MachineRepository extends JpaRepository<Machine,String> {}

