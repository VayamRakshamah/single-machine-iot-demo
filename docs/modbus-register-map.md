# Modbus register map

The simulator listens on TCP port `1502`, unit ID `1`, and updates once per second. Documentation uses conventional one-based labels; client offsets are zero-based.

| Address | Type | Client offset | Scale | Unit | Expected range |
|---|---|---:|---:|---|---|
| 40001 | Holding | 0 | 0.01 | % | 0–100 |
| 40002 | Holding | 1 | 0.01 | bar | 0–30 |
| 40003 | Holding | 2 | 0.01 | °C | -20–120 |
| 40004 | Holding | 3 | 0.01 | Nm³/h | 0–60 |
| 40005 | Holding | 4 | 0.01 | A | 0–30 |
| 40006 | Holding | 5 | 1 | cycles | 0–65535 |
| 40007 | Holding | 6 | 1 | units | 0–65535 |
| 00001 | Coil | 0 | boolean | — | running |
| 00002 | Coil | 1 | boolean | — | warning |
| 00003 | Coil | 2 | boolean | — | critical |

Counters wrap at the 16-bit register boundary in this demo. The gateway treats these values as illustrative rather than production counters.

