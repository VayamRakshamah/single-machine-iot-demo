package io.github.iotdemo.backend.service;

import io.github.iotdemo.backend.domain.Alarm;
import io.github.iotdemo.backend.repository.AlarmRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlarmServiceTest {
    @Test void triggersOnlyAfterTenContinuousSeconds() {
        AlarmRepository repository=mock(AlarmRepository.class);SimpMessagingTemplate events=mock(SimpMessagingTemplate.class);
        when(repository.findFirstByMachineIdAndTypeAndStatusIn(anyString(),any(),anyList())).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation->invocation.getArgument(0));
        AlarmService service=new AlarmService(repository,events);Instant start=Instant.parse("2026-01-01T00:00:00Z");
        Map<String,Object> low=Map.of("purity",89d,"temperature",70d,"pressure",20d);
        service.evaluate("machine",start,low);service.evaluate("machine",start.plusSeconds(9),low);verify(repository,never()).save(any());
        service.evaluate("machine",start.plusSeconds(10),low);ArgumentCaptor<Alarm> alarm=ArgumentCaptor.forClass(Alarm.class);verify(repository).save(alarm.capture());
        assertThat(alarm.getValue().type).isEqualTo(Alarm.AlarmType.LOW_PURITY);
    }
}
