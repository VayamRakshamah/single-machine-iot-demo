package io.github.iotdemo.backend.service;

import io.github.iotdemo.backend.repository.GatewayStatusRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;

@Service
public class ConnectivityMonitor {
    private final GatewayStatusRepository gateways; private final AlarmService alarms; private final SimpMessagingTemplate events;
    public ConnectivityMonitor(GatewayStatusRepository gateways, AlarmService alarms, SimpMessagingTemplate events, MeterRegistry registry) {
        this.gateways=gateways;this.alarms=alarms;this.events=events;
        Gauge.builder("iot_connected_gateways",gateways,repository->repository.findAll().stream().filter(status->status.connected).count()).register(registry);
    }
    @Scheduled(fixedDelay=1000)
    @Transactional
    public void check() {
        Instant now=Instant.now();
        for(var gateway:gateways.findAll()){
            if(gateway.lastSeen!=null&&Duration.between(gateway.lastSeen,now).compareTo(Duration.ofSeconds(10))>=0){
                alarms.communicationLost(gateway.machineId,gateway.lastSeen,now);
                if(gateway.connected){gateway.connected=false;gateway.updatedAt=now;events.convertAndSend("/topic/machines/"+gateway.machineId+"/gateway",gateway);}
            }
        }
    }
}
