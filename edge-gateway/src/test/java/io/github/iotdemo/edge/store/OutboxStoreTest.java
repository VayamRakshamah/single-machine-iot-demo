package io.github.iotdemo.edge.store;

import io.github.iotdemo.edge.config.GatewayProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxStoreTest {
    @TempDir Path temp;

    @Test void sequenceAndOutboxSurviveNewStoreInstance() throws Exception {
        var properties = new GatewayProperties("localhost", 1502, 1, 1000, "tcp://localhost:1883", "topic",
                "gateway", "plant", "machine", temp.resolve("buffer.db").toString(), "", "", false, "", "", "");
        var first = new OutboxStore(properties); first.initialize();
        long sequence = first.nextSequence(); first.enqueue(sequence, "payload");
        var restarted = new OutboxStore(properties); restarted.initialize();
        assertThat(restarted.pending(10)).extracting(OutboxStore.Item::sequenceNumber).containsExactly(sequence);
        assertThat(restarted.nextSequence()).isEqualTo(sequence + 1);
        restarted.acknowledge(sequence); assertThat(restarted.count()).isZero();
    }
}

