#!/bin/sh

# Build the project using Maven
mvn clean install

# Build the Docker image for the main application
docker build -t gestiondeprojet-loginapp:latest .

# Create a Docker network for the load balancer
docker network create -d bridge lb-net || true

# Build and start the Docker Compose stack
docker-compose up --build -d
