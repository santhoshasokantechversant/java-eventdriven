#!/bin/bash

# Script to create all required Kafka topics for Customer Management System
echo "=== Creating Kafka Topics with KRaft ==="

# Kafka broker address (where Kafka is running)
KAFKA_BROKER="localhost:9092"

# List of all topics we need for our system
TOPICS=(
  "customer.created"          # When new customer is created
  "customer.updated"          # When customer is updated
  "customer.deleted"          # When customer is deleted
  "customer.rollback"         # When customer needs rollback

  # User Service Topics
  "user.created"              # When user is successfully created
  "user.creation.failed"      # When user creation fails
  "user.rollback"             # NEW: When user needs rollback (saga compensation)

  # Account Service Topics
  "account.created"           # When account is successfully created
  "account.creation.failed"   # When account creation fails
  "account.rollback"          # NEW: When account needs rollback (saga compensation)
)

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
sleep 30

# Loop through each topic and create it
for TOPIC in "${TOPICS[@]}"; do
  echo "Creating topic: $TOPIC"

  # This command creates a Kafka topic with KRaft
  docker exec kafka-broker kafka-topics \
    --create \
    --topic "$TOPIC" \
    --bootstrap-server "$KAFKA_BROKER" \
    --partitions 3 \
    --replication-factor 1

  # Check if topic was created successfully
  if [ $? -eq 0 ]; then
    echo "✓ Successfully created: $TOPIC"
  else
    echo "✗ Failed to create: $TOPIC"
    echo "Retrying in 5 seconds..."
    sleep 5
    docker exec kafka-broker kafka-topics \
      --create \
      --topic "$TOPIC" \
      --bootstrap-server "$KAFKA_BROKER" \
      --partitions 3 \
      --replication-factor 1
  fi
done

echo "=== Topic creation completed ==="
echo "You can check topics using: docker exec kafka-broker kafka-topics --list --bootstrap-server localhost:9092"