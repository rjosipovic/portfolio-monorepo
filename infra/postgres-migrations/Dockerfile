FROM postgres:12

# Copy migration scripts
COPY *.sql /postgres-migrations/

# Copy the entrypoint script (we'll create this next)
COPY run-migrations.sh /run-migrations.sh
RUN chmod +x /run-migrations.sh

ENTRYPOINT ["/run-migrations.sh"]
