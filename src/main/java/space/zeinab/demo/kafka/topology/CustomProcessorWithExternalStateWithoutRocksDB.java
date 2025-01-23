package space.zeinab.demo.kafka.topology;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import space.zeinab.demo.kafka.config.KafkaConfig;

@Slf4j
@Component
public class CustomProcessorWithExternalStateWithoutRocksDB implements Processor<String, String, String, String> {
    private final JdbcTemplate jdbcTemplate;
    private ProcessorContext<String, String> context;

    public CustomProcessorWithExternalStateWithoutRocksDB(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void init(ProcessorContext<String, String> context) {
        this.context = context;
    }

    @Override
    public void process(Record<String, String> record) {
        updateExternalState(jdbcTemplate, record.key(), record.value());
        logDataFromExternalState(jdbcTemplate, record.key());

        context.forward(record);
    }

    @Override
    public void close() {
    }

    private void updateExternalState(JdbcTemplate jdbcTemplate, String key, String value) {
        jdbcTemplate.update(
                "MERGE INTO " + KafkaConfig.TABLE_NAME +
                        " USING dual ON (id = ?) " +
                        "WHEN MATCHED THEN UPDATE SET name = ? " +
                        "WHEN NOT MATCHED THEN INSERT (id, name) VALUES (?, ?)",
                key, value, key, value
        );
    }

    private void logDataFromExternalState(JdbcTemplate jdbcTemplate, String key) {
        log.info("data from DB: {}",
                jdbcTemplate.queryForList(
                        "SELECT name FROM " + KafkaConfig.TABLE_NAME +
                                " WHERE id = ?", String.class, key
                ).get(0)
        );
    }
}
