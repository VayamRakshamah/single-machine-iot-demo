# Optional AWS IoT Core mode

AWS is not required for local mode. Charges and organizational security policies may apply.

1. In AWS IoT Core create a Thing named `GATEWAY-DEMO-001`.
2. Create and activate an X.509 certificate. Download the device certificate, private key, and Amazon Root CA 1 outside this repository.
3. Attach a least-privilege policy replacing `REGION` and `ACCOUNT_ID` locally:

```json
{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Action":["iot:Connect"],"Resource":["arn:aws:iot:REGION:ACCOUNT_ID:client/GATEWAY-DEMO-001"]},{"Effect":"Allow","Action":["iot:Publish"],"Resource":["arn:aws:iot:REGION:ACCOUNT_ID:topic/factory/demo/plant-001/machine/DOCS-DEMO-001/*"]}]}
```

4. Obtain the account-specific ATS data endpoint. Set `MQTT_BROKER_URI=ssl://YOUR_ENDPOINT:8883`, `MQTT_TLS=true`, and the three `MQTT_*_PATH` values.
5. Mount certificate files read-only into the edge container via a local Compose override. Never copy them into an image or Git.
6. Subscribe with the AWS IoT MQTT test client to `factory/demo/plant-001/machine/DOCS-DEMO-001/#`, start the gateway, and verify messages.

The configuration surface reserves TLS paths. Before AWS use, the Paho client must construct a mutually authenticated `SSLSocketFactory`, validate the hostname and CA, and implement certificate rotation. Local Mosquitto is the fully implemented default mode.
