package space.zeinab.demo.kafka.config;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.TopicBuilder;
import space.zeinab.demo.kafka.topology.DatasourceTopology;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Configuration
public class KafkaConfig {
    public static final String INPUT_TOPIC = "input-topic";
    public static final String OUTPUT_TOPIC = "output-topic";

    @Value("${kafka.streams.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.application.name}")
    private String applicationId;

    /*@Bean
    public NewTopic inputTopic() {
        return TopicBuilder.name(INPUT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic outputTopic() {
        return TopicBuilder.name(OUTPUT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }*/

    @Bean
    public Topology kafkaTopology(JdbcTemplate jdbcTemplate) {
        Topology topology = new Topology();
        topology.addSource("source", "input-topic")
                .addProcessor("statefulProcessor", () -> new DatasourceTopology(jdbcTemplate), "source")
                .addSink("output-sink", "output-topic", "datasourceTopology");

        return topology;
    }

    @Bean
    public KafkaStreams kafkaStreams(Topology topology) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        KafkaStreams streams = new KafkaStreams(topology, props);
        streams.start();
        return streams;
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return org.springframework.boot.jdbc.DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void createTopicsAndProduceMockData() {
        createTopics();
        produceMockData();
    }

    private void createTopics() {
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            NewTopic inputTopic = new NewTopic("input-topic", 1, (short) 1);
            NewTopic outputTopic = new NewTopic("output-topic", 1, (short) 1);
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
            String key = "key-" + i;//"vehicle1"
            String value = "value-" + i;
            ProducerRecord<String, String> record = new ProducerRecord<>("input-topic", key, value);
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
