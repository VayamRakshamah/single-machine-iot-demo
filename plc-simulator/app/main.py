from __future__ import annotations

import asyncio
import logging
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI, HTTPException
from pymodbus.datastore import ModbusSequentialDataBlock, ModbusServerContext, ModbusSlaveContext
from pymodbus.server import ModbusTcpServer

from .model import MachineModel, Scenario

logging.basicConfig(level=logging.INFO, format='%(asctime)s %(levelname)s %(name)s %(message)s')
logger = logging.getLogger("plc-simulator")

model = MachineModel()
store = ModbusSlaveContext(
    di=ModbusSequentialDataBlock(0, [0] * 16),
    co=ModbusSequentialDataBlock(0, [0] * 16),
    hr=ModbusSequentialDataBlock(0, [0] * 32),
    ir=ModbusSequentialDataBlock(0, [0] * 32),
    zero_mode=True,
)
context = ModbusServerContext(slaves=store, single=True)
server: ModbusTcpServer | None = None
server_task: asyncio.Task | None = None


async def set_modbus_enabled(enabled: bool) -> None:
    global server, server_task
    if enabled and server_task is None:
        server = ModbusTcpServer(context, address=("0.0.0.0", 1502))
        server_task = asyncio.create_task(server.serve_forever())
        logger.info("Modbus TCP listening on 1502")
    elif not enabled and server_task is not None:
        assert server is not None
        await server.shutdown()
        await server_task
        server = None
        server_task = None
        logger.warning("Modbus TCP stopped for communication-failure scenario")


async def update_loop() -> None:
    while True:
        snapshot = model.update()
        store.setValues(3, 0, snapshot.holding_registers())
        store.setValues(1, 0, snapshot.coils())
        await asyncio.sleep(1)


@asynccontextmanager
async def lifespan(_: FastAPI):
    await set_modbus_enabled(True)
    updater = asyncio.create_task(update_loop())
    yield
    updater.cancel()
    await set_modbus_enabled(False)


app = FastAPI(title="Simulated PLC Control API", version="1.0.0", lifespan=lifespan)


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "UP"}


@app.get("/api/status")
async def status() -> dict[str, str | bool]:
    return {
        "machineId": "DOCS-DEMO-001",
        "scenario": model.scenario.value,
        "modbusAvailable": server_task is not None,
        "dataClassification": "SIMULATED",
    }


@app.put("/api/scenario/{scenario}")
async def change_scenario(scenario: str) -> dict[str, str | bool]:
    normalized = "normal" if scenario == "reset" else scenario
    try:
        selected = Scenario(normalized)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail="Unsupported scenario") from exc
    model.set_scenario(selected)
    await set_modbus_enabled(selected != Scenario.COMMUNICATION_FAILURE)
    return await status()


if __name__ == "__main__":
    uvicorn.run("app.main:app", host="0.0.0.0", port=8081, log_level="info")

