package io.github.iotdemo.backend.service;

import io.github.iotdemo.backend.domain.*;
import io.github.iotdemo.backend.messaging.TelemetryMessage;
import io.github.iotdemo.backend.repository.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class TelemetryService {
    private final TelemetryRepository telemetry; private final LatestStateRepository latest; private final GatewayStatusRepository gateways;
    private final AlarmService alarmService; private final SimpMessagingTemplate events; private final Counter consumed,duplicates;
    public TelemetryService(TelemetryRepository telemetry,LatestStateRepository latest,GatewayStatusRepository gateways,AlarmService alarmService,SimpMessagingTemplate events,MeterRegistry registry){
        this.telemetry=telemetry;this.latest=latest;this.gateways=gateways;this.alarmService=alarmService;this.events=events;
        consumed=registry.counter("iot_messages_consumed_total");duplicates=registry.counter("iot_duplicate_messages_total");
    }
    @Transactional
    public boolean ingest(TelemetryMessage message){
        if(telemetry.existsByMachineIdAndSequenceNumber(message.machineId(),message.sequenceNumber())){duplicates.increment();return false;}
        Instant received=Instant.now();
        try{telemetry.saveAndFlush(new TelemetryRecord(message.machineId(),message.gatewayId(),message.sequenceNumber(),message.timestamp(),received,message.values()));}
        catch(DataIntegrityViolationException duplicate){duplicates.increment();return false;}
        LatestMachineState state=latest.findById(message.machineId()).orElseGet(()->new LatestMachineState(message.machineId()));
        if(state.recordedAt==null||!message.timestamp().isBefore(state.recordedAt)){state.sequenceNumber=message.sequenceNumber();state.recordedAt=message.timestamp();state.receivedAt=received;state.values=message.values();latest.save(state);}
        GatewayStatus gateway=gateways.findById(message.gatewayId()).orElseThrow();gateway.connected=true;gateway.lastSeen=received;gateway.updatedAt=received;
        alarmService.evaluate(message.machineId(),message.timestamp(),message.values());consumed.increment();
        events.convertAndSend("/topic/machines/"+message.machineId()+"/telemetry",message);return true;
    }
}

