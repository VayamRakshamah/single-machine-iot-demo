package io.github.iotdemo.backend.repository;
import io.github.iotdemo.backend.domain.ParameterDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ParameterDefinitionRepository extends JpaRepository<ParameterDefinition,Long> { List<ParameterDefinition> findAllByOrderByDisplayOrderAsc(); }

