FROM open-liberty:microProfile3-java11
LABEL maintainer="IBM Cloud Architecture Solution Engineering at IBM Cloud"
# This is the common image for websphere liberty to run only local

ARG LICENSE_JAR_URL
ENV KAFKA_BROKERS=""
ENV KAFKA_APIKEY=""

COPY --chown=1001:0 target/ordercommandms-1.0-SNAPSHOT.war /config/dropins