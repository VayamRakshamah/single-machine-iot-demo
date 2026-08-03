from app.model import MachineModel, Scenario


def test_registers_are_scaled_and_counters_advance() -> None:
    model = MachineModel(seed=1)
    first = model.update()
    second = model.update()
    assert 9300 <= first.holding_registers()[0] <= 9500
    assert second.cycle_count == first.cycle_count + 1
    assert second.output_count == first.output_count + 8


def test_alarm_scenarios_change_registers_and_coils() -> None:
    model = MachineModel(seed=1)
    model.set_scenario(Scenario.LOW_PURITY)
    low = model.update()
    assert low.purity < 90
    assert low.critical

    model.set_scenario(Scenario.HIGH_TEMPERATURE)
    hot = model.update()
    assert hot.temperature > 80
    assert hot.warning

    model.set_scenario(Scenario.PRESSURE_DROP)
    pressure = model.update()
    assert pressure.pressure < 15
    assert pressure.warning


def test_stopped_machine_has_no_flow_or_current() -> None:
    model = MachineModel(seed=1)
    model.set_scenario(Scenario.STOPPED)
    snapshot = model.update()
    assert not snapshot.running
    assert snapshot.flow == 0
    assert snapshot.motor_current == 0

