#!/bin/sh

# Remove stale PID file if exists
rm -f /var/run/docker.pid

# Start Docker daemon
dockerd &

# Wait for Docker daemon to be available
echo "Starting Docker daemon..."
while ! docker info > /dev/null 2>&1; do
  echo "Waiting for Docker daemon to be available..."
  sleep 1
done

# Wait for the registry to be available
while ! curl -s http://registery:8081/api/registery/nodes; do
    echo "Waiting for registry to be available..."
    sleep 5
done

# Register this node with the registry
NODE_IP=$(hostname -I | awk '{print $1}')
curl -X POST -H "Content-Type: application/json" -d "{\"address\":\"${NODE_IP}\",\"url\":\"http://${NODE_IP}:2375\"}" http://registery:8081/api/registery/nodes

# Keep the script running
tail -f /dev/null
