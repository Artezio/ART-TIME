FROM jboss/wildfly:14.0.1.Final

ARG ARTTIME_VERSION
ARG ARTTIME_CLI_ADMIN_LOGIN=admin
ARG ARTTIME_CLI_ADMIN_PASSWORD=admin
ARG ARTTIME_WAR_DOWNLOAD_URL=https://github.com/Artezio/ART-TIME/releases/download/${ARTTIME_VERSION}/arttime-webapp.war
ARG KEYCLOAK_ADAPTER_VERSION=4.8.3.Final
ARG KEYCLOAK_ADAPTER_DOWNLOAD_URL=https://downloads.jboss.org/keycloak/${KEYCLOAK_ADAPTER_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_ADAPTER_VERSION}.tar.gz

ARG DB_DRIVER_NAME=postgresql
ARG DB_DRIVER_CLASS_NAME=org.postgresql.Driver
ARG DB_DRIVER_MODULE_NAME=org.postgresql
ARG DB_DRIVER_VERSION=42.2.2.jre7
ARG DB_DRIVER_JAR=postgresql-${DB_DRIVER_VERSION}.jar
ARG DB_DRIVER_DOWNLOAD_URL=https://jdbc.postgresql.org/download/${DB_DRIVER_JAR}
ARG DB_VALID_CONNECTION_CHECKER_CLASS_NAME=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker
ARG DB_EXCEPTION_SORTER_CLASS_NAME=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter
ARG JBOSS_CLI=/opt/jboss/wildfly/bin/jboss-cli.sh

ENV ARTTIME_DB_HOST arttime-demo-database
ENV ARTTIME_DB_PORT 5432
ENV ARTTIME_DB_NAME arttime
ENV ARTTIME_DB_LOGIN postgres
ENV ARTTIME_DB_PASSWORD postgres
ENV KEYCLOAK_SERVER_URL http://arttime-demo-keycloak:8081/auth
ENV KEYCLOAK_CLIENT_ID arttime-demo
ENV KEYCLOAK_REALM master
ENV KEYCLOAK_USERNAME_ATTRIBUTE preferred_username
# These two settings can be set on Settings/External Data Providers page
# ENV KEYCLOAK_LOGIN admin
# ENV KEYCLOAK_PASSWORD password
ENV MAX_HEAP_SIZE_MB 2048
ENV MAX_METASPACE_SIZE_MB 384

# Download binaries
ADD --chown=jboss ${DB_DRIVER_DOWNLOAD_URL} /tmp/${DB_DRIVER_JAR}
ADD --chown=jboss ${ARTTIME_WAR_DOWNLOAD_URL} /opt/jboss/wildfly/standalone/deployments/arttime-webapp.war

# Install Keycloak adapter
RUN curl -SL "${KEYCLOAK_ADAPTER_DOWNLOAD_URL}" | tar xvz -C /opt/jboss/wildfly/
RUN /opt/jboss/wildfly/bin/add-user.sh --user ${ARTTIME_CLI_ADMIN_LOGIN} --password ${ARTTIME_CLI_ADMIN_PASSWORD} --silent --enable
RUN $JBOSS_CLI --file=/opt/jboss/wildfly/bin/adapter-install-offline.cli -Dserver.config=standalone-full.xml
RUN $JBOSS_CLI --file=/opt/jboss/wildfly/bin/adapter-elytron-install-offline.cli -Dserver.config=standalone-full.xml

# Install DB Driver JAR as Jboss module
RUN $JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\
,"module add --name=${DB_DRIVER_MODULE_NAME} --resources=/tmp/${DB_DRIVER_JAR} --dependencies=[javax.api,javax.transaction.api]"\
,"/subsystem=datasources/jdbc-driver=${DB_DRIVER_NAME}:add( \
	driver-name=${DB_DRIVER_NAME}, \
	driver-module-name=${DB_DRIVER_MODULE_NAME}, \
	driver-class-name=${DB_DRIVER_CLASS_NAME})"\
,stop-embedded-server

# Add datasource
RUN $JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\
,"/subsystem=datasources/data-source=arttime-datasource:add( \
	jndi-name=\"java:jboss/datasources/com.artezio.arttime\", \
	driver-class=${DB_DRIVER_CLASS_NAME}, \
	driver-name=${DB_DRIVER_NAME}, \
	connection-url=\"jdbc:postgresql://\${env.ARTTIME_DB_HOST}:\${env.ARTTIME_DB_PORT}/\${env.ARTTIME_DB_NAME}\", \
	user-name=\${env.ARTTIME_DB_LOGIN}, \
	password=\${env.ARTTIME_DB_PASSWORD}, \
	valid-connection-checker-class-name=${DB_VALID_CONNECTION_CHECKER_CLASS_NAME}, \
	validate-on-match=true, \
	background-validation=true, \
	exception-sorter-class-name=${DB_EXCEPTION_SORTER_CLASS_NAME}, \
	idle-timeout-minutes=5, \
	initial-pool-size=10, \
	max-pool-size=50, \
	transaction-isolation=TRANSACTION_READ_COMMITTED, \
	new-connection-sql=\"SELECT 1\", \
	jta=true, use-ccm=true, \
	allocation-retry=100, \
	allocation-retry-wait-millis=20000)"\
,stop-embedded-server

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

# Apply Keycloak adapter to deployment
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

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-full.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
