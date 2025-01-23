package space.zeinab.demo.kafka.config;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Configuration
public class KafkaConfig {
    public static final String INPUT_TOPIC = "input-topic";
    public static final String OUTPUT_TOPIC = "output-topic";
    public static final String TABLE_NAME = "customStateStore";

    @Value("${spring.kafka.streams.bootstrap-servers}")
    private String bootstrapServers;

    @PostConstruct
    public void createTopicsAndProduceMockData() {
        createTopics();
        produceMockData();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private void createTopics() {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            NewTopic inputTopic = new NewTopic(INPUT_TOPIC, 1, (short) 1);
            NewTopic outputTopic = new NewTopic(OUTPUT_TOPIC, 1, (short) 1);
            adminClient.createTopics(Collections.singletonList(inputTopic)).all().get();
            adminClient.createTopics(Collections.singletonList(outputTopic)).all().get();
            System.out.println("Topics created successfully");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void produceMockData() {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        for (int i = 0; i < 10; i++) {
            String key = "key-" + i;
            String value = "value-" + i;
            ProducerRecord<String, String> record = new ProducerRecord<>(INPUT_TOPIC, key, value);
            try {
                RecordMetadata metadata = producer.send(record).get();
                System.out.printf("Produced record to topic %s partition [%d] @ offset %d%n", metadata.topic(), metadata.partition(), metadata.offset());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        producer.close();
    }
}
