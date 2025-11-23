#!/bin/sh

set -e

# Create the password file from its environment variable
echo -n "$SMTP_PASSWORD" > /etc/alertmanager/smtp_password
chmod 600 /etc/alertmanager/smtp_password

# Use sed to substitute variables from the environment into the template
# and create the final config file. This is more portable than envsubst.
sed -e "s|\${SMTP_HOST}|${SMTP_HOST}|g" \
    -e "s|\${SMTP_PORT}|${SMTP_PORT}|g" \
    -e "s|\${SMTP_FROM}|${SMTP_FROM}|g" \
    -e "s|\${SMTP_USER}|${SMTP_USER}|g" \
    -e "s|\${SMTP_TO}|${SMTP_TO}|g" \
    /etc/alertmanager/alertmanager.yml.template > /etc/alertmanager/config.yml

# Now, execute the main Alertmanager process, pointing to the generated config
exec /bin/alertmanager --config.file=/etc/alertmanager/config.yml --storage.path=/alertmanager
