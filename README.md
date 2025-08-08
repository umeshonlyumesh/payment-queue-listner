# Payment Queue Listener

A Spring Boot application that listens to AWS SQS queues for payment data, enriches the data with additional information, and stores it in AWS DynamoDB.

## Features

- Listens to two AWS SQS queues for payment data
- Processes messages in batches for improved efficiency
- Enriches payment data with additional information (risk score, fraud status, etc.)
- Stores enriched data in AWS DynamoDB
- Uses Java 21 virtual threads for efficient asynchronous processing
- Includes comprehensive error handling and logging
- Follows industry best practices for AWS integration

## Architecture

The application follows a clean architecture with the following components:

- **Listeners**: Listen to SQS queues and process incoming messages
- **Services**: Enrich payment data with additional information
- **Repositories**: Store enriched data in DynamoDB
- **Models**: Define the data structure for payment data
- **Configuration**: Configure AWS services and application components

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- AWS account with SQS and DynamoDB services
- AWS credentials with appropriate permissions

## Configuration

The application can be configured using environment variables or by modifying the `application.properties` file:

```properties
# AWS Configuration
aws.region=us-east-1
aws.credentials.access-key=${AWS_ACCESS_KEY}
aws.credentials.secret-key=${AWS_SECRET_KEY}

# SQS Configuration
aws.sqs.queue1.url=${SQS_QUEUE1_URL}
aws.sqs.queue2.url=${SQS_QUEUE2_URL}
aws.sqs.max-number-of-messages=10
aws.sqs.visibility-timeout=30
aws.sqs.wait-time-seconds=20

# DynamoDB Configuration
aws.dynamodb.table-name=${DYNAMODB_TABLE_NAME}
```

## Building and Running

### Building the Application

```bash
mvn clean package
```

### Running the Application

```bash
java -jar target/payment-queue-listner-0.0.1-SNAPSHOT.jar
```

### Running with Docker

```bash
# Build the Docker image
docker build -t payment-queue-listener .

# Run the Docker container
docker run -p 8080:8080 \
  -e AWS_ACCESS_KEY=your-access-key \
  -e AWS_SECRET_KEY=your-secret-key \
  -e SQS_QUEUE1_URL=your-queue1-url \
  -e SQS_QUEUE2_URL=your-queue2-url \
  -e DYNAMODB_TABLE_NAME=your-table-name \
  payment-queue-listener
```

## Testing

The application includes unit tests for all components. To run the tests:

```bash
mvn test
```

## Message Format

The application expects payment messages in the following JSON format:

```json
{
  "id": "payment-123",
  "transactionId": "tx-456",
  "amount": 100.50,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "status": "PENDING",
  "customerId": "cust-789",
  "merchantId": "merch-012",
  "timestamp": "2023-10-15T14:30:00"
}
```

## Enriched Data

The application enriches the payment data with the following additional information:

- **enrichmentId**: A unique ID for the enrichment process
- **additionalData**: Additional data about the payment (payment channel, customer category, etc.)
- **riskScore**: A risk score based on the payment amount (LOW, MEDIUM, HIGH)
- **fraudStatus**: A fraud status based on the payment amount and currency (CLEAR, REVIEW_REQUIRED, SUSPICIOUS)
- **enrichmentTimestamp**: The timestamp when the enrichment was performed
- **processingStatus**: The status of the processing (COMPLETED, ERROR)
- **processingTimeMs**: The time taken to process the payment in milliseconds

## Performance Considerations

The application is designed for high performance and scalability:

- Uses Java 21 virtual threads for efficient asynchronous processing
- Processes SQS messages in batches for improved throughput
- Uses DynamoDB for scalable and low-latency storage
- Includes configurable thread pool settings for optimal performance
- Supports both JPA and native SQL queries for database operations

### JPA vs. Native SQL Queries

The application provides options for using either JPA or native SQL queries when fetching unprocessed transactions:

```properties
# Query Configuration
# Set to true to use native SQL queries for potentially better performance with large datasets
# Set to false to use standard JPA queries (default)
app.use-native-query=false
```

#### Performance Comparison:

1. **JPA Queries (Default)**:
   - Advantages:
     - Type-safe and easier to maintain
     - Portable across different database systems
     - Handles entity relationships automatically
     - Good performance for small to medium datasets
   - Disadvantages:
     - May have overhead for complex queries or large datasets
     - Less control over the exact SQL being executed

2. **Native SQL Queries**:
   - Advantages:
     - Can be significantly faster for large datasets
     - Allows database-specific optimizations
     - Full control over the exact SQL being executed
     - Can leverage database-specific features and optimizations
   - Disadvantages:
     - Not type-safe
     - Less portable across different database systems
     - Requires more maintenance when entity structure changes

#### Recommendations:

- For small to medium datasets (up to thousands of records), JPA queries are generally sufficient and provide better maintainability.
- For large datasets (tens of thousands or more), consider enabling native SQL queries for better performance.
- If your application processes a high volume of transactions, benchmark both approaches in your specific environment to determine the optimal configuration.
- Monitor database performance and adjust the configuration as needed based on your specific workload.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
