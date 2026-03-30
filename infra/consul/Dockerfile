FROM hashicorp/consul:1.18.0

# Copy the configuration directory
COPY kv_config /consul/kv_config

# Copy the import script
COPY import-kv.sh /consul/import-kv.sh

# Make the script executable
RUN chmod +x /consul/import-kv.sh

# Set the entrypoint to run the script
ENTRYPOINT ["sh", "/consul/import-kv.sh"]
