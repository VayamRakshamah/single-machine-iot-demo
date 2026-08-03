package io.github.iotdemo.edge.modbus;

import io.github.iotdemo.edge.config.GatewayProperties;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ModbusTcpClient {
    private final GatewayProperties properties;
    private final AtomicInteger transaction = new AtomicInteger();

    public ModbusTcpClient(GatewayProperties properties) {
        this.properties = properties;
    }

    public Snapshot readSnapshot() throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(properties.plcHost(), properties.plcPort()), 1500);
            socket.setSoTimeout(1500);
            var input = new DataInputStream(socket.getInputStream());
            var output = new DataOutputStream(socket.getOutputStream());
            int[] registers = read(output, input, 3, 0, 7);
            int[] coils = read(output, input, 1, 0, 3);
            return new Snapshot(registers[0] / 100.0, registers[1] / 100.0,
                    registers[2] / 100.0, registers[3] / 100.0, registers[4] / 100.0,
                    registers[5], registers[6], coils[0] == 1, coils[1] == 1, coils[2] == 1);
        }
    }

    private int[] read(DataOutputStream output, DataInputStream input, int function, int address, int count) throws Exception {
        int id = transaction.updateAndGet(value -> (value + 1) & 0xffff);
        output.writeShort(id); output.writeShort(0); output.writeShort(6);
        output.writeByte(properties.plcUnitId()); output.writeByte(function);
        output.writeShort(address); output.writeShort(count); output.flush();
        int responseId = input.readUnsignedShort(); input.readUnsignedShort();
        int length = input.readUnsignedShort(); int unit = input.readUnsignedByte();
        int responseFunction = input.readUnsignedByte();
        if (responseId != id || unit != properties.plcUnitId() || responseFunction != function) {
            throw new IllegalStateException("Invalid Modbus response");
        }
        int byteCount = input.readUnsignedByte();
        if (length != byteCount + 3) throw new IllegalStateException("Invalid Modbus length");
        int[] values = new int[count];
        if (function == 3) {
            for (int i = 0; i < count; i++) values[i] = input.readUnsignedShort();
        } else {
            int packed = input.readUnsignedByte();
            for (int i = 0; i < count; i++) values[i] = (packed >> i) & 1;
        }
        return values;
    }

    public record Snapshot(double purity, double pressure, double temperature, double flow,
                           double motorCurrent, long cycleCount, long outputCount,
                           boolean running, boolean warning, boolean critical) {
    }
}

