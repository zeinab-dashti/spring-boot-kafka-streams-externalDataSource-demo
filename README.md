# spring-boot-kafka-streams-externalDataSource-demo

## Overview
Hands-on example of managing state in Kafka Streams without relying on RocksDB, utilizing external database.

This approach demonstrates interaction with an external database instead of persisting it internally. The state is not tightly integrated with Kafka Streams' state management API, it lacks the performance optimization and caching provided by Kafka Streams' built-in state stores and Kafka Streams has no direct awareness of the state. So, it needs manually state management to maintain state consistency, transaction logic, and fault tolerance. 

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
