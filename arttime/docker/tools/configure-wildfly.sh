#!/bin/bash

# Install Keycloak adapter
curl -SL "${KEYCLOAK_ADAPTER_DOWNLOAD_URL}" | tar xvz -C /opt/jboss/wildfly/
/opt/jboss/wildfly/bin/add-user.sh --user ${ARTTIME_CLI_ADMIN_LOGIN} --password ${ARTTIME_CLI_ADMIN_PASSWORD} --silent --enable
$JBOSS_CLI --file=/opt/jboss/wildfly/bin/adapter-install-offline.cli -Dserver.config=standalone-full.xml
$JBOSS_CLI --file=/opt/jboss/wildfly/bin/adapter-elytron-install-offline.cli -Dserver.config=standalone-full.xml

# Create mail session
$JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\
,"/subsystem=mail/mail-session=com.artezio.arttime:add(jndi-name=\"java:jboss/mail/com.artezio.arttime\")"\
,stop-embedded-server

# Setup JMS queue
$JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\
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
$JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\
,"/subsystem=undertow/server=default-server/http-listener=default:write-attribute(name=max-parameters, value=5000)"\
,stop-embedded-server

# Apply Keycloak adapter to war deployment
$JBOSS_CLI --commands="embed-server --server-config=standalone-full.xml"\
,"/subsystem=keycloak/secure-deployment=arttime-webapp.war:add( \
	realm=\${env.KEYCLOAK_REALM}, \
	resource=\${env.KEYCLOAK_CLIENT_ID}, \
    enable-basic-auth=true, \
	public-client=true, \
	auth-server-url=\${env.KEYCLOAK_SERVER_URL}, \
	ssl-required=EXTERNAL, \
	principal-attribute=\${env.KEYCLOAK_USERNAME_ATTRIBUTE})"\
,stop-embedded-server
