package space.zeinab.demo.kafka.topology;

import org.apache.kafka.streams.processor.api.ContextualProcessor;
import org.apache.kafka.streams.processor.api.Record;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatasourceTopology extends ContextualProcessor<String, String, String, String> {
    private final JdbcTemplate jdbcTemplate;

    public DatasourceTopology(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void process(Record<String, String> record) {
        // Process the stream and update state in the database
        updateDatabaseState(jdbcTemplate, record.key(), record.value());
        // Forward the processed value to the next step in the topology
        context().forward(record);
    }

    @Override
    public void close() {
        // Any cleanup code if needed
    }

    private void updateDatabaseState(JdbcTemplate jdbcTemplate, String key, String value) {
        String sql = "INSERT INTO stream_state (key, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = ?";
        jdbcTemplate.update(sql, key, value, value);
    }
}
