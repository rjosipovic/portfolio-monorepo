#!/bin/sh
set -e

# This script imports all configuration files from the /consul/kv_config directory.
# It relies on the 'depends_on' in docker-compose.yml to ensure Consul is ready.

echo "Importing KV configuration..."

# Loop through the config directory and import each file.
# The key will be the relative path of the file.
cd /consul/kv_config
for f in $(find . -type f); do
  # Remove the leading './' from the path to create the key
  KEY=$(echo $f | sed 's|./||')
  echo "Importing ${KEY}..."
  consul kv put "${KEY}" @${f}
done

echo "KV import complete."
