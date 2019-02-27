FROM jboss/wildfly:14.0.1.Final

ARG ARTTIME_VERSION
ARG ARTTIME_CLI_ADMIN_LOGIN=admin
ARG ARTTIME_CLI_ADMIN_PASSWORD=admin
ARG ARTTIME_WAR_DOWNLOAD_URL=https://github.com/Artezio/ART-TIME/releases/download/${ARTTIME_VERSION}/arttime-webapp.war
ARG ARTTIME_DIST_DOWNLOAD_URL=https://github.com/Artezio/ART-TIME/archive/${ARTTIME_VERSION}.tar.gz
ARG ARTTIME_REPO_URL=https://github.com/Artezio/ART-TIME/raw/${ARTTIME_VERSION}
ARG KEYCLOAK_ADAPTER_VERSION=4.8.3.Final
ARG KEYCLOAK_ADAPTER_DOWNLOAD_URL=https://downloads.jboss.org/keycloak/${KEYCLOAK_ADAPTER_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_ADAPTER_VERSION}.tar.gz
ARG JBOSS_CLI=/opt/jboss/wildfly/bin/jboss-cli.sh

# Possible values are: postgres, mysql, mariadb
ENV ARTTIME_DB_VENDOR postgres
ENV ARTTIME_DB_HOST arttime-demo-database
ENV ARTTIME_DB_PORT 5432
ENV ARTTIME_DB_NAME arttime
ENV ARTTIME_DB_LOGIN postgres
ENV ARTTIME_DB_PASSWORD postgres
ENV KEYCLOAK_SERVER_URL http://arttime-demo-keycloak:8081/auth
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

# Download binaries
ADD --chown=jboss ${ARTTIME_WAR_DOWNLOAD_URL} /opt/jboss/wildfly/standalone/deployments/arttime-webapp.war

# Download and run database configuration tools
RUN mkdir -p /opt/jboss/tools
ADD --chown=jboss ${ARTTIME_REPO_URL}/arttime/docker/webapp/tools /opt/jboss/tools/
RUN chmod +x /opt/jboss/tools/*.sh && \
    /opt/jboss/tools/install-databases.sh

# Install Keycloak adapter
RUN curl -SL "${KEYCLOAK_ADAPTER_DOWNLOAD_URL}" | tar xvz -C /opt/jboss/wildfly/ && \
    /opt/jboss/wildfly/bin/add-user.sh --user ${ARTTIME_CLI_ADMIN_LOGIN} --password ${ARTTIME_CLI_ADMIN_PASSWORD} --silent --enable && \
    $JBOSS_CLI --file=/opt/jboss/wildfly/bin/adapter-install-offline.cli -Dserver.config=standalone-full.xml && \
    $JBOSS_CLI --file=/opt/jboss/wildfly/bin/adapter-elytron-install-offline.cli -Dserver.config=standalone-full.xml

# Create mail session
RUN $JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\
,"/subsystem=mail/mail-session=com.artezio.arttime:add(jndi-name=\"java:jboss/mail/com.artezio.arttime\")"\
,stop-embedded-server

# Setup JMS queue
RUN $JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\
,"/subsystem=messaging-activemq/server=default/jms-queue=arttime:add(entries=[java:/jms/queue/arttime])"\
,"/subsystem=messaging-activemq/server=default/address-setting=jms.queue.arttime:add( \
	dead-letter-address=jms.queue.DLQ, \
	expiry-address=jms.queue.ExpiryQueue, \
    max-delivery-attempts=3, \
	max-redelivery-delay=3000, \
	max-size-bytes=10485760, \
	message-counter-history-day-limit=10, \
	page-size-bytes=2097152)"\
,stop-embedded-server

# Increase max count of request params
RUN $JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\	
,"/subsystem=undertow/server=default-server/http-listener=default:write-attribute(name=max-parameters, value=5000)"\	
,stop-embedded-server

# Apply Keycloak adapter to war deployment
RUN $JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\	
,"/subsystem=keycloak/secure-deployment=arttime-webapp.war:add( \
	realm=\${env.KEYCLOAK_REALM}, \
	resource=\${env.KEYCLOAK_CLIENT_ID}, \
    enable-basic-auth=true, \
	public-client=true, \
	auth-server-url=\${env.KEYCLOAK_SERVER_URL}, \
	ssl-required=EXTERNAL, \
	principal-attribute=\${env.KEYCLOAK_USERNAME_ATTRIBUTE})"\
,stop-embedded-server

ENV JAVA_OPTS "-server -Xms256m -Xmx\${MAX_HEAP_SIZE_MB}m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=\${MAX_METASPACE_SIZE_MB}m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true -Djboss.as.management.blocking.timeout=1200"

EXPOSE 8080
EXPOSE 9990

ENTRYPOINT ["/opt/jboss/tools/docker-entrypoint.sh"]

CMD ["-c", "standalone-full.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
