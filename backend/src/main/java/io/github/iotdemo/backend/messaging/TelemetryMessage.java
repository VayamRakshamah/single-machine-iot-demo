package io.github.iotdemo.backend.messaging;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Map;

public record TelemetryMessage(
 @Pattern(regexp="1\\.0") String schemaVersion,
 @NotBlank String gatewayId, @NotBlank String plantId, @NotBlank String machineId,
 @NotNull @PastOrPresent Instant timestamp, @Positive long sequenceNumber,
 @NotNull @Size(min=10) Map<String,Object> values) {}

