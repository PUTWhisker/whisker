FROM envoyproxy/envoy:v1.20-latest

COPY ./proxy/envoy.yaml /tmpl/envoy.yaml.tmpl
COPY ./proxy/docker-entrypoint.sh /

RUN chmod 500 /docker-entrypoint.sh

RUN apt-get update && \
    apt-get install gettext -y

ENV SERVER_PORT=9900

ENTRYPOINT ["/docker-entrypoint.sh"]
