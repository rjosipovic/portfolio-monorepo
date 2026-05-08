FROM nginx:1.27-alpine

RUN addgroup -g 101 -S nginx || true && \
    adduser -S -D -H -u 101 -h /var/cache/nginx -s /sbin/nologin -G nginx nginx || true

COPY index.html /usr/share/nginx/html/
COPY styles/ /usr/share/nginx/html/styles/
COPY images/ /usr/share/nginx/html/images/
COPY config.js /usr/share/nginx/html/config.js

RUN chown -R 101:101 /var/cache/nginx /var/run /usr/share/nginx/html && \
    sed -i 's/listen\s*80;/listen 8080;/' /etc/nginx/conf.d/default.conf

USER 101

EXPOSE 8080
