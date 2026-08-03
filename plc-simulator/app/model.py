from __future__ import annotations

import math
import random
from dataclasses import dataclass
from enum import Enum


class Scenario(str, Enum):
    NORMAL = "normal"
    LOW_PURITY = "low-purity"
    HIGH_TEMPERATURE = "high-temperature"
    PRESSURE_DROP = "pressure-drop"
    STOPPED = "stopped"
    COMMUNICATION_FAILURE = "communication-failure"


@dataclass(frozen=True)
class Snapshot:
    purity: float
    pressure: float
    temperature: float
    flow: float
    motor_current: float
    cycle_count: int
    output_count: int
    running: bool
    warning: bool
    critical: bool

    def holding_registers(self) -> list[int]:
        return [
            round(self.purity * 100),
            round(self.pressure * 100),
            round(self.temperature * 100),
            round(self.flow * 100),
            round(self.motor_current * 100),
            self.cycle_count % 65536,
            self.output_count % 65536,
        ]

    def coils(self) -> list[bool]:
        return [self.running, self.warning, self.critical]


class MachineModel:
    def __init__(self, seed: int | None = None) -> None:
        self.scenario = Scenario.NORMAL
        self.cycle_count = 1500
        self.output_count = 12000
        self._tick = 0
        self._random = random.Random(seed)

    def set_scenario(self, scenario: Scenario) -> None:
        self.scenario = scenario

    def update(self) -> Snapshot:
        self._tick += 1
        phase = math.sin(self._tick / 7)
        noise = self._random.uniform(-0.12, 0.12)
        running = self.scenario not in {Scenario.STOPPED, Scenario.COMMUNICATION_FAILURE}

        purity = 93.8 + phase * 0.25 + noise
        pressure = 22.4 + phase * 0.45 + noise
        temperature = 67.0 + phase * 1.2 + noise
        flow = 38.0 + phase * 1.5 + noise
        current = 14.2 + phase * 0.5 + noise

        if self.scenario == Scenario.LOW_PURITY:
            purity = 87.5 + noise
        elif self.scenario == Scenario.HIGH_TEMPERATURE:
            temperature = 84.0 + noise
        elif self.scenario == Scenario.PRESSURE_DROP:
            pressure = 12.5 + noise
        elif not running:
            flow = current = 0.0
            pressure = 0.5

        if running:
            self.cycle_count += 1
            self.output_count += 8

        warning = temperature > 80 or pressure < 15
        critical = purity < 90
        return Snapshot(
            purity=round(purity, 2), pressure=round(pressure, 2),
            temperature=round(temperature, 2), flow=round(flow, 2),
            motor_current=round(current, 2), cycle_count=self.cycle_count,
            output_count=self.output_count, running=running,
            warning=warning, critical=critical,
        )

