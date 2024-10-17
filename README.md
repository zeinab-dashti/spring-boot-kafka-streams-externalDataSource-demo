# spring-boot-kafka-streams-externalDataSource-demo


* This application reads from "input-topic", processes the data using Processor API, and writes it to "output-topic".
* Instead of using RocksDB for state storage, this implementation uses an unnumbered relational database (like MySQL or PostgreSQL) via JdbcTemplate.
* The database table `stream_state` is expected to have columns `key` (unique identifier) and `value` to store the state.
* The state is maintained using a simple SQL `INSERT ... ON DUPLICATE KEY UPDATE` to ensure the state is consistently updated.
* Relational DB properties are configured using Spring Boot's `spring.datasource` configuration in `application.properties` or `application.yml`.
* Added a Kafka producer to generate mock data for the "input-topic".
* Added AdminClient to create topics "input-topic" and "output-topic" before producing mock data.

## compare the two approaches for using an external relational database instead of RocksDB in Kafka Streams
### Direct Relational Database Interaction via Processor API (JdbcTemplate)

In this approach, read and write state to/from the relational database is done directly by the processor via JdbcTemplate or similar. However, the state is not tightly integrated with Kafka Streams’ state management API. 

#### Advantages
This approach is straightforward and simple
Developer has complete control over the database transactions, updates, and how the state is maintained

#### Disadvantages
Kafka Streams has no direct awareness of the state, and developers need to manage state consistency, transaction logic, and fault tolerance manually.
Lacks the performance optimization and caching provided by Kafka Streams' built-in state stores, potentially leading to higher latency due to frequent database access.