package io.github.iotdemo.backend.controller;

import io.github.iotdemo.backend.domain.*;
import io.github.iotdemo.backend.repository.*;
import io.github.iotdemo.backend.service.AlarmService;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import java.time.Instant;import java.time.temporal.ChronoUnit;import java.util.*;

@RestController @RequestMapping("/api/v1")
public class ApiController {
    private final MachineRepository machines;private final LatestStateRepository latest;private final TelemetryRepository telemetry;private final ParameterDefinitionRepository parameters;private final AlarmRepository alarms;private final GatewayStatusRepository gateways;private final AlarmService alarmService;private final RestClient simulator;
    public ApiController(MachineRepository machines,LatestStateRepository latest,TelemetryRepository telemetry,ParameterDefinitionRepository parameters,AlarmRepository alarms,GatewayStatusRepository gateways,AlarmService alarmService,@Value("${iot.plc-scenario-url}")String simulatorUrl){
        this.machines=machines;this.latest=latest;this.telemetry=telemetry;this.parameters=parameters;this.alarms=alarms;this.gateways=gateways;this.alarmService=alarmService;this.simulator=RestClient.builder().baseUrl(simulatorUrl).build();}
    @GetMapping("/machines")public List<Machine> machines(){return machines.findAll();}
    @GetMapping("/machines/{id}")public Machine machine(@PathVariable String id){return machines.findById(id).orElseThrow();}
    @GetMapping("/machines/{id}/latest")public LatestMachineState latest(@PathVariable String id){return latest.findById(id).orElseThrow();}
    @GetMapping("/machines/{id}/parameters")public List<ParameterDefinition> parameters(@PathVariable String id){machine(id);return parameters.findAllByOrderByDisplayOrderAsc();}
    @GetMapping("/machines/{id}/alarms")public List<Alarm> alarms(@PathVariable String id){return alarms.findByMachineIdOrderByStartTimeDesc(id);}
    @GetMapping("/machines/{id}/telemetry")public List<TelemetryRecord> history(@PathVariable String id,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)Instant from,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)Instant to,@RequestParam(defaultValue="raw")String aggregation,@RequestParam(defaultValue="0")@Min(0)int page,@RequestParam(defaultValue="500")@Min(1)@Max(2000)int size){
        Instant end=to==null?Instant.now():to;Instant start=from==null?end.minus(15,ChronoUnit.MINUTES):from;if(!Set.of("raw","1m","5m","15m","1h").contains(aggregation))throw new IllegalArgumentException("Unsupported aggregation");return telemetry.findByMachineIdAndRecordedAtBetweenOrderByRecordedAtDesc(id,start,end,PageRequest.of(page,size));}
    @GetMapping(value="/machines/{id}/telemetry.csv",produces="text/csv")public String csv(@PathVariable String id,@RequestParam(required=false)Instant from,@RequestParam(required=false)Instant to){StringBuilder csv=new StringBuilder("timestamp,sequence,purity,pressure,temperature,flow,motorCurrent,cycleCount,outputCount,running\n");for(var row:history(id,from,to,"raw",0,2000)){csv.append(row.recordedAt).append(',').append(row.sequenceNumber);for(String key:List.of("purity","pressure","temperature","flow","motorCurrent","cycleCount","outputCount","running"))csv.append(',').append(row.values.getOrDefault(key,""));csv.append('\n');}return csv.toString();}
    @PostMapping("/alarms/{id}/acknowledge")public Alarm acknowledge(@PathVariable long id,@Valid@RequestBody Acknowledgement body){return alarmService.acknowledge(id,body.user(),body.comment());}
    @GetMapping("/gateways")public List<GatewayStatus> gateways(){return gateways.findAll();}
    @GetMapping("/health/summary")public Map<String,Object> health(){return Map.of("status","UP","activeAlarms",alarms.countByStatusIn(List.of(Alarm.AlarmStatus.ACTIVE,Alarm.AlarmStatus.ACKNOWLEDGED)),"simulated",true);}
    @GetMapping("/demo/scenario")public Object scenario(){return simulator.get().uri("/api/status").retrieve().body(Object.class);}
    @PutMapping("/demo/scenario/{scenario}")public Object scenario(@PathVariable String scenario){return simulator.put().uri("/api/scenario/{scenario}",scenario).retrieve().body(Object.class);}
    public record Acknowledgement(@NotBlank@Size(max=120)String user,@Size(max=500)String comment){}
}

