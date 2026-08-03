CREATE TABLE machine (
  id VARCHAR(64) PRIMARY KEY, name VARCHAR(120) NOT NULL, plant_id VARCHAR(64) NOT NULL,
  description VARCHAR(500) NOT NULL, simulated BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE parameter_definition (
  id BIGSERIAL PRIMARY KEY, code VARCHAR(64) UNIQUE NOT NULL, display_name VARCHAR(120) NOT NULL,
  unit VARCHAR(32), data_type VARCHAR(24) NOT NULL, minimum_valid_value DOUBLE PRECISION,
  maximum_valid_value DOUBLE PRECISION, warning_low DOUBLE PRECISION, warning_high DOUBLE PRECISION,
  critical_low DOUBLE PRECISION, critical_high DOUBLE PRECISION, display_order INTEGER NOT NULL,
  visible BOOLEAN NOT NULL, chart_enabled BOOLEAN NOT NULL, decimal_places INTEGER NOT NULL
);
CREATE TABLE telemetry_record (
  id BIGSERIAL PRIMARY KEY, machine_id VARCHAR(64) NOT NULL REFERENCES machine(id), gateway_id VARCHAR(64) NOT NULL,
  sequence_number BIGINT NOT NULL, recorded_at TIMESTAMPTZ NOT NULL, received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  values_json JSONB NOT NULL, CONSTRAINT uq_telemetry_machine_sequence UNIQUE(machine_id, sequence_number)
);
CREATE INDEX idx_telemetry_machine_time ON telemetry_record(machine_id, recorded_at DESC);
CREATE INDEX idx_telemetry_values_gin ON telemetry_record USING GIN(values_json);
CREATE TABLE latest_machine_state (
  machine_id VARCHAR(64) PRIMARY KEY REFERENCES machine(id), sequence_number BIGINT NOT NULL,
  recorded_at TIMESTAMPTZ NOT NULL, received_at TIMESTAMPTZ NOT NULL, values_json JSONB NOT NULL
);
CREATE TABLE alarm (
  id BIGSERIAL PRIMARY KEY, machine_id VARCHAR(64) NOT NULL REFERENCES machine(id), type VARCHAR(64) NOT NULL,
  severity VARCHAR(16) NOT NULL, status VARCHAR(20) NOT NULL, start_time TIMESTAMPTZ NOT NULL,
  current_value DOUBLE PRECISION, threshold DOUBLE PRECISION, acknowledgement_user VARCHAR(120),
  acknowledgement_time TIMESTAMPTZ, acknowledgement_comment VARCHAR(500), cleared_time TIMESTAMPTZ
);
CREATE UNIQUE INDEX uq_alarm_open_type ON alarm(machine_id, type) WHERE status IN ('ACTIVE','ACKNOWLEDGED');
CREATE INDEX idx_alarm_machine_status ON alarm(machine_id, status, start_time DESC);
CREATE TABLE gateway_status (
  gateway_id VARCHAR(64) PRIMARY KEY, machine_id VARCHAR(64) NOT NULL REFERENCES machine(id), connected BOOLEAN NOT NULL,
  last_seen TIMESTAMPTZ, updated_at TIMESTAMPTZ NOT NULL
);

INSERT INTO machine(id,name,plant_id,description,simulated) VALUES
 ('DOCS-DEMO-001','Oxygen Generation Process Demo','PLANT-001','Illustrative simulated process machine',TRUE);
INSERT INTO gateway_status(gateway_id,machine_id,connected,updated_at) VALUES ('GATEWAY-DEMO-001','DOCS-DEMO-001',FALSE,now());
INSERT INTO parameter_definition(code,display_name,unit,data_type,minimum_valid_value,maximum_valid_value,warning_low,warning_high,critical_low,critical_high,display_order,visible,chart_enabled,decimal_places) VALUES
 ('purity','Oxygen Purity','%', 'DECIMAL',0,100,NULL,NULL,90,NULL,1,TRUE,TRUE,2),
 ('pressure','Pressure','bar','DECIMAL',0,40,15,NULL,NULL,NULL,2,TRUE,TRUE,2),
 ('temperature','Temperature','°C','DECIMAL',-20,150,NULL,80,NULL,NULL,3,TRUE,TRUE,1),
 ('flow','Flow Rate','Nm³/h','DECIMAL',0,100,NULL,NULL,NULL,NULL,4,TRUE,TRUE,1),
 ('motorCurrent','Motor Current','A','DECIMAL',0,50,NULL,NULL,NULL,NULL,5,TRUE,TRUE,1),
 ('cycleCount','Cycle Count','cycles','INTEGER',0,NULL,NULL,NULL,NULL,NULL,6,TRUE,FALSE,0),
 ('outputCount','Output Count','units','INTEGER',0,NULL,NULL,NULL,NULL,NULL,7,TRUE,FALSE,0);

