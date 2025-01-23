# spring-boot-kafka-streams-externalDataSource-demo

## Overview
This is a Java and Spring Boot and Kafka Streams API to illustrate the integration of a custom data source backed by an external database.

In this approach, 
* Read and write state to/from the database is done directly by the processor via JdbcTemplate. 
* The state is not tightly integrated with Kafka Streams’ state management API And Kafka Streams has no direct awareness of the state. So, it needs manually state management to maintain state consistency, transaction logic, and fault tolerance.
* It lacks the performance optimization and caching provided by Kafka Streams' built-in state stores, potentially leading to higher latency due to frequent database access.
* The database table schema is provided in [schema.sql](./src/main/resources/schema.sql) file.
* DB properties are configured using Spring Boot's `spring.datasource` configuration in `application.yml`.
* For simplicity the H2 database is configured as the datasource. 

## Prerequisites
* Java 17 or higher
* Maven
* Docker (optional, for running Docker Compose which include Zookeeper and Apache Kafka)


## Running the Application
1. **Start Kafka and Zookeeper by using Docker Compose file in the repository**:
   ```sh
   docker-compose up
   ```

2. **Build**:
   ```sh
   mvn clean package
   ```

3. **Run the application**

   ```sh
   mvn spring-boot:run
   ```
   Once the application is in Running state, it will start to produce dummy data and the Kafka Streams application reads data from `input-topic`, processes it using the custom processor and publishing the data to the supported database while logging soe info, and writes to `output-topic`.
