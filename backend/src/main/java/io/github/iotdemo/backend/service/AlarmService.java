package io.github.iotdemo.backend.service;

import io.github.iotdemo.backend.domain.Alarm;
import io.github.iotdemo.backend.repository.AlarmRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class AlarmService {
    private static final List<Alarm.AlarmStatus> OPEN = List.of(Alarm.AlarmStatus.ACTIVE, Alarm.AlarmStatus.ACKNOWLEDGED);
    private final AlarmRepository alarms; private final SimpMessagingTemplate events;
    private final Map<String,Instant> pending = new HashMap<>();
    public AlarmService(AlarmRepository alarms,SimpMessagingTemplate events){this.alarms=alarms;this.events=events;}

    @Transactional
    public synchronized void evaluate(String machineId, Instant timestamp, Map<String,Object> values) {
        rule(machineId, Alarm.AlarmType.LOW_PURITY, Alarm.Severity.CRITICAL, number(values,"purity") < 90, number(values,"purity"),90d,timestamp);
        rule(machineId, Alarm.AlarmType.HIGH_TEMPERATURE, Alarm.Severity.WARNING, number(values,"temperature") > 80,number(values,"temperature"),80d,timestamp);
        rule(machineId, Alarm.AlarmType.LOW_PRESSURE, Alarm.Severity.WARNING, number(values,"pressure") < 15,number(values,"pressure"),15d,timestamp);
        clear(machineId, Alarm.AlarmType.COMMUNICATION_LOST, timestamp);
    }

    private void rule(String machineId,Alarm.AlarmType type,Alarm.Severity severity,boolean condition,double value,double threshold,Instant now){
        String key=machineId+":"+type;
        if(!condition){pending.remove(key);clear(machineId,type,now);return;}
        var open=alarms.findFirstByMachineIdAndTypeAndStatusIn(machineId,type,OPEN);
        if(open.isPresent()){open.get().currentValue=value;return;}
        Instant since=pending.computeIfAbsent(key,k->now);
        if(Duration.between(since,now).compareTo(Duration.ofSeconds(10))>=0){
            Alarm alarm=alarms.save(new Alarm(machineId,type,severity,since,value,threshold)); pending.remove(key);
            events.convertAndSend("/topic/machines/"+machineId+"/alarms",alarm);
        }
    }
    private void clear(String machineId,Alarm.AlarmType type,Instant now){
        alarms.findFirstByMachineIdAndTypeAndStatusIn(machineId,type,OPEN).ifPresent(alarm->{alarm.status=Alarm.AlarmStatus.CLEARED;alarm.clearedTime=now;events.convertAndSend("/topic/machines/"+machineId+"/alarms",alarm);});
    }
    @Transactional public Alarm acknowledge(long id,String user,String comment){
        Alarm alarm=alarms.findById(id).orElseThrow(()->new NoSuchElementException("Alarm not found"));
        if(alarm.status!=Alarm.AlarmStatus.ACTIVE)throw new IllegalStateException("Only active alarms can be acknowledged");
        alarm.status=Alarm.AlarmStatus.ACKNOWLEDGED;alarm.acknowledgementUser=user;alarm.acknowledgementComment=comment;alarm.acknowledgementTime=Instant.now();
        events.convertAndSend("/topic/machines/"+alarm.machineId+"/alarms",alarm);return alarm;
    }
    @Transactional public synchronized void communicationLost(String machineId, Instant lastSeen, Instant now) {
        if (Duration.between(lastSeen, now).compareTo(Duration.ofSeconds(10)) < 0) return;
        if (alarms.findFirstByMachineIdAndTypeAndStatusIn(machineId, Alarm.AlarmType.COMMUNICATION_LOST, OPEN).isEmpty()) {
            Alarm alarm = alarms.save(new Alarm(machineId, Alarm.AlarmType.COMMUNICATION_LOST,
                    Alarm.Severity.CRITICAL, lastSeen.plusSeconds(10), null, 10d));
            events.convertAndSend("/topic/machines/" + machineId + "/alarms", alarm);
        }
    }
    private static double number(Map<String,Object> values,String key){Object value=values.get(key);return value instanceof Number n?n.doubleValue():Double.NaN;}
}
