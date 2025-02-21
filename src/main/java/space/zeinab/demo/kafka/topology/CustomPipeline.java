package space.zeinab.demo.kafka.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import space.zeinab.demo.kafka.config.KafkaConfig;


@Component
public class CustomPipeline {
    private final JdbcTemplate jdbcTemplate;

    public CustomPipeline(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {
        streamsBuilder.build()
                .addSource("source", KafkaConfig.INPUT_TOPIC)
                .addProcessor("CustomProcessor", () -> new CustomProcessorWithExternalStateWithoutRocksDB(jdbcTemplate), "source")
                // following commented part is needed for CustomProcessorWithExternalStateWithRocksDB, instead of the line above
                // in that case the code interacts with both the Kafka state store and the external database
                //.addProcessor("CustomProcessor", () -> new CustomProcessorWithExternalStateWithRocksDB(jdbcTemplate), "source")
                .addStateStore(
                        Stores.keyValueStoreBuilder(
                                Stores.persistentKeyValueStore(KafkaConfig.TABLE_NAME),
                                Serdes.String(),
                                Serdes.String()
                        ), "CustomProcessor"
                )
                .addSink("sink", KafkaConfig.OUTPUT_TOPIC, "CustomProcessor");
    }
}
