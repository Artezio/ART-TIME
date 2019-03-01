# ----------------------------------------------------------------------------------
# Build and configure embedded Keycloak server
FROM jboss/keycloak:4.8.3.Final as keycloak

# Set this variable to the host where keycloak is running
ENV KEYCLOAK_HOSTNAME localhost
ARG ARTTIME_VERSION
ARG ARTTIME_REPO_URL=https://github.com/Artezio/ART-TIME/raw/${ARTTIME_VERSION}
ARG KEYCLOAK_ADMIN_LOGIN=admin
ARG KEYCLOAK_ADMIN_PASSWORD=password

# Download demo keycloak database
ADD --chown=1000:0 ${ARTTIME_REPO_URL}/arttime/docker/keycloak/keycloak.mv.db /opt/jboss/keycloak/standalone/data/keycloak.mv.db

# Download demo keycloak login theme
ADD --chown=1000:0 ${ARTTIME_REPO_URL}/arttime/docker/keycloak/themes/keycloak /opt/jboss/keycloak/themes/keycloak/

# Add keycloak admin user so art-time can connect to its API
RUN /opt/jboss/keycloak/bin/add-user.sh ${KEYCLOAK_ADMIN_LOGIN} ${KEYCLOAK_ADMIN_PASSWORD}

USER root
RUN mv /opt/jboss/tools/docker-entrypoint.sh /opt/jboss/tools/docker-entrypoint-keycloak.sh
USER 1000


# ----------------------------------------------------------------------------------
# Build and configure wildfly server for running Art-Time web application
FROM jboss/wildfly:14.0.1.Final as wildfly

ARG ARTTIME_VERSION
ARG ARTTIME_CLI_ADMIN_LOGIN=admin
ARG ARTTIME_CLI_ADMIN_PASSWORD=admin
ARG ARTTIME_WAR_DOWNLOAD_URL=https://github.com/Artezio/ART-TIME/releases/download/${ARTTIME_VERSION}/arttime-webapp.war
ARG ARTTIME_DIST_DOWNLOAD_URL=https://github.com/Artezio/ART-TIME/archive/${ARTTIME_VERSION}.tar.gz
ARG ARTTIME_REPO_URL=https://github.com/Artezio/ART-TIME/raw/${ARTTIME_VERSION}
ARG KEYCLOAK_ADAPTER_VERSION=4.8.3.Final
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

# These two settings can be set on Settings/External Data Providers page in the application when logged in as Admin
# ENV KEYCLOAK_LOGIN admin
# ENV KEYCLOAK_PASSWORD password
ENV MAX_HEAP_SIZE_MB 2048
ENV MAX_METASPACE_SIZE_MB 384

ENV JDBC_POSTGRES_VERSION 42.2.2
ENV JDBC_MYSQL_VERSION 5.1.46
ENV JDBC_MSSQL_VERSION 6.4.0.jre7
ENV JDBC_MARIADB_VERSION 2.2.3 
ENV JDBC_ORACLE_VERSION ojdbc6

# Install Art-Time webapp
ADD --chown=jboss:0 ${ARTTIME_WAR_DOWNLOAD_URL} /opt/jboss/wildfly/standalone/deployments/arttime-webapp.war

# Download and run database configuration tools
ADD --chown=jboss:0 ${ARTTIME_REPO_URL}/arttime/docker/tools /opt/jboss/tools/
RUN chmod +x /opt/jboss/tools/*.sh && \
    /opt/jboss/tools/install-databases.sh
	
# Configure Wildfly	
RUN /opt/jboss/tools/configure-wildfly.sh

# Get configured Keycloak server
COPY --chown=jboss:0 --from=keycloak /opt/jboss/keycloak /opt/jboss/keycloak
COPY --chown=jboss:0 --from=keycloak /opt/jboss/tools /opt/jboss/tools

# Init Demo database
USER root
RUN mkdir -p /opt/arttime-demo-db && chown jboss:0 /opt/arttime-demo-db
ADD --chown=jboss:0 ${ARTTIME_REPO_URL}/arttime/docker/init_arttime_demo_db_h2.sql /opt/arttime-demo-db/init-arttime-demo-db.sql
USER jboss

EXPOSE 8080
EXPOSE 9080
EXPOSE 9990

CMD ["/opt/jboss/tools/docker-entrypoint.sh"]
