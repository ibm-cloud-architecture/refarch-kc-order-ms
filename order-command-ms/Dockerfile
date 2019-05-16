FROM websphere-liberty:19.0.0.3-microProfile2
LABEL maintainer="IBM Cloud Architecture Solution Engineering at IBM Cloud"
# This is the common image for websphere liberty to run only local

USER root

RUN apt-get update -q -y && apt-get dist-upgrade -q -y 


ARG LICENSE_JAR_URL
ENV KAFKA_BROKERS=""
ENV KAFKA_ENV=""
ENV KAFKA_APIKEY=""
ENV POSTGRESQL_URL=""
ENV POSTGRESQL_USER=""
ENV POSTGRESQL_PWD=""

# 3 next lines need to be there to avoid NPE on javametrics-rest, javametrics-dash,
COPY /target/liberty/wlp/usr/servers/defaultServer /config/
COPY /target/liberty/wlp/usr/shared/resources /config/resources/
COPY /src/main/liberty/config/jvmbx.options /config/jvm.options
# Grant write access to apps folder, this is to support old and new docker versions.
# Liberty document reference : https://hub.docker.com/_/websphere-liberty/

USER 1001

# install any missing features required by server config
RUN installUtility install defaultServer --acceptLicense  


# Upgrade to production license if URL to JAR provided
ARG LICENSE_JAR_URL
RUN \ 
  if [ $LICENSE_JAR_URL ]; then \
    wget $LICENSE_JAR_URL -O /tmp/license.jar \
    && java -jar /tmp/license.jar -acceptLicense /opt/ibm \
    && rm /tmp/license.jar; \
  fi
