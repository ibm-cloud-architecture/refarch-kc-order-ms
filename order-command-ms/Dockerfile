FROM openliberty/open-liberty:full-java8-openj9-ubi
COPY --chown=1001:0 src/main/liberty/config /config/
ADD target/ordercommandms.war /config/dropins
USER 1001