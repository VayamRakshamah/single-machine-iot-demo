package io.github.iotdemo.backend.repository;
import io.github.iotdemo.backend.domain.GatewayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GatewayStatusRepository extends JpaRepository<GatewayStatus,String> {}

