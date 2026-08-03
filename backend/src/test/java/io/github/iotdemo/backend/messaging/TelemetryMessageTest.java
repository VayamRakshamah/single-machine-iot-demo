package io.github.iotdemo.backend.messaging;
import jakarta.validation.Validation;import org.junit.jupiter.api.Test;import java.time.Instant;import java.util.Map;import static org.assertj.core.api.Assertions.assertThat;
class TelemetryMessageTest {
 @Test void rejectsUnsupportedSchemaAndIncompleteValues(){try(var factory=Validation.buildDefaultValidatorFactory()){var message=new TelemetryMessage("2.0","gateway","plant","machine",Instant.now(),1,Map.of("purity",93));assertThat(factory.getValidator().validate(message)).isNotEmpty();}}
}
