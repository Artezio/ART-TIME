# ----------------------------------------------------------------------------------
# Download and build arttime from source code
FROM maven:3.5.2-jdk-8-alpine as maven_builder

RUN apk add --no-cache git
RUN cd / &&\
	git clone https://github.com/Artezio/ART-TIME.git &&\
	cd /ART-TIME/arttime &&\
	mvn clean install -Dmaven.test.skip=true


# ----------------------------------------------------------------------------------
# Build and configure embedded Keycloak server
FROM jboss/keycloak:6.0.1 as keycloak

# Set this variable to the host where keycloak is running
ARG KEYCLOAK_ADMIN_LOGIN=admin
ARG KEYCLOAK_ADMIN_PASSWORD=password

# Add demo keycloak db and login theme from dist image
COPY --chown=1000:0 --from=maven_builder /ART-TIME/arttime/docker/keycloak/keycloak.mv.db /opt/jboss/keycloak/standalone/data/keycloak.mv.db
COPY --chown=1000:0 --from=maven_builder /ART-TIME/arttime/docker/keycloak/themes/keycloak /opt/jboss/keycloak/themes/keycloak/

# Add keycloak admin user so art-time can connect to its API
RUN /opt/jboss/keycloak/bin/add-user.sh ${KEYCLOAK_ADMIN_LOGIN} ${KEYCLOAK_ADMIN_PASSWORD}

USER root
RUN mv /opt/jboss/tools/docker-entrypoint.sh /opt/jboss/tools/docker-entrypoint-keycloak.sh
USER 1000


# ----------------------------------------------------------------------------------
# Build wildfly with Art-Time web application and keycloak demo
FROM jboss/wildfly:17.0.0.Final as wildfly

ARG ARTTIME_CLI_ADMIN_LOGIN=admin
ARG ARTTIME_CLI_ADMIN_PASSWORD=admin
ARG KEYCLOAK_ADAPTER_VERSION=6.0.1
ARG KEYCLOAK_ADAPTER_DOWNLOAD_URL=https://downloads.jboss.org/keycloak/${KEYCLOAK_ADAPTER_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_ADAPTER_VERSION}.tar.gz
ARG JBOSS_CLI=/opt/jboss/wildfly/bin/jboss-cli.sh

# Possible values are: h2 (embedded demo db), postgres, mysql, mariadb
ENV ARTTIME_DB_VENDOR h2
# The following DB settings are only applied when DB vendor is not h2
ENV ARTTIME_DB_HOST localhost
ENV ARTTIME_DB_PORT 5432
ENV ARTTIME_DB_NAME arttime
ENV ARTTIME_DB_LOGIN postgres
ENV ARTTIME_DB_PASSWORD postgres
ENV KEYCLOAK_SERVER_URL http://localhost:9080/auth
ENV KEYCLOAK_CLIENT_ID arttime-demo
ENV KEYCLOAK_REALM master
ENV KEYCLOAK_USERNAME_ATTRIBUTE preferred_username

ENV MAX_HEAP_SIZE_MB 2048
ENV MAX_METASPACE_SIZE_MB 384
ENV JBOSS_ARGUMENTS ""

ENV JDBC_POSTGRES_VERSION 42.2.2
ENV JDBC_MYSQL_VERSION 5.1.46
ENV JDBC_MSSQL_VERSION 6.4.0.jre7
ENV JDBC_MARIADB_VERSION 2.2.3 
ENV JDBC_ORACLE_VERSION ojdbc6

# Deploy Art-Time webapp
COPY --chown=jboss --from=maven_builder /ART-TIME/arttime/arttime-webapp/target/arttime-webapp.war /opt/jboss/wildfly/standalone/deployments/arttime-webapp.war

# Install database drivers
COPY --chown=jboss --from=maven_builder /ART-TIME/arttime/docker/tools /opt/jboss/tools/arttime/
USER root
RUN chmod +x /opt/jboss/tools/arttime/*.sh && \ 
    /opt/jboss/tools/arttime/install-databases.sh
USER jboss

# Configure Wildfly	
RUN /opt/jboss/tools/arttime/configure-wildfly.sh

# Get configured Keycloak server
COPY --chown=jboss --from=keycloak /opt/jboss/keycloak /opt/jboss/keycloak
COPY --chown=jboss --from=keycloak /opt/jboss/tools /opt/jboss/tools

# Setup demo database
USER root
RUN mkdir -p /opt/arttime-demo-db && chown jboss:0 /opt/arttime-demo-db
COPY --chown=jboss --from=maven_builder /ART-TIME/arttime/docker/init_arttime_demo_db_h2.sql /opt/arttime-demo-db/init-arttime-demo-db.sql

USER jboss

EXPOSE 8080
EXPOSE 9080
EXPOSE 9990

CMD ["/opt/jboss/tools/arttime/docker-entrypoint.sh"]
