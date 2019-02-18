FROM jboss/wildfly:14.0.1.Final

ARG ARTTIME_VERSION=latest
ARG KEYCLOAK_ADAPTER_VERSION=4.8.3.Final

USER root

RUN mkdir -p /opt/configuration
RUN chown jboss /opt/configuration -R

USER jboss

# Install keycloak adapter for arttime-webapp.war

RUN curl -SL "https://downloads.jboss.org/keycloak/${KEYCLOAK_ADAPTER_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_ADAPTER_VERSION}.tar.gz" \
	| tar xvz -C /opt/jboss/wildfly/
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/adapter-install-offline.cli -Dserver.config=standalone-full.xml
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/adapter-elytron-install-offline.cli -Dserver.config=standalone-full.xml

# Download war file and configuration script from Github

RUN export RELEASE_INFO=$(curl -L -s -H 'Accept: application/json' https://github.com/artezio/ART-TIME/releases/$ARTTIME_VERSION) && \
	export ARTTIME_ARTIFACTS_VERSION=$(echo $RELEASE_INFO | sed -e 's/.*"tag_name":"\([^"]*\)".*/\1/') && \
	curl -L "https://github.com/Artezio/ART-TIME/releases/download/$ARTTIME_ARTIFACTS_VERSION/arttime-webapp.war" -o /opt/jboss/wildfly/standalone/deployments/arttime-webapp.war && \
	mkdir /tmp/arttime-repo && \
    curl "https://github.com/Artezio/ART-TIME/archive/$ARTTIME_ARTIFACTS_VERSION.tar.gz" -L -o /tmp/arttime.tar.gz && \
	tar xvf /tmp/arttime.tar.gz --directory=/tmp/arttime-repo --strip-components=1 && \
	cp /tmp/arttime-repo/arttime/docker/webapp/configuration/* /opt/configuration/ -R --verbose && \
	rm -r /tmp/arttime-repo && rm /tmp/arttime.tar.gz


# Configure wildfly automatically

RUN sed -i -e "s/<resolve-parameter-values>false/<resolve-parameter-values>true/" /opt/jboss/wildfly/bin/jboss-cli.xml
RUN /opt/jboss/wildfly/bin/add-user.sh --user admin --password admin --silent --enable
WORKDIR /opt/configuration/wildfly/
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --properties=app.subsystems.properties --file=configure-wildfly.cli

ENV keycloak_server_url http://keycloak:8081/auth
ENV keycloak_client_id arttime-demo
ENV keycloak_realm master
ENV keycloak_login admin
ENV keycloak_password admin
ENV arttime_db_host arttimedb
ENV arttime_db_port 5432
ENV arttime_db_name arttime
ENV arttime_db_login postgres
ENV arttime_db_password postgres

# Override JBOSS java params to use specified memory limits and extended server-stabilization timeout. 
# Memory limits are evaluated in run-time to allow per-container settings changes

ENV JAVA_OPTS "-server -Xms256m -Xmx\${max_heap_size_mb}m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=\${max_metaspace_size_mb}m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true -Djboss.as.management.blocking.timeout=1200"
ENV max_heap_size_mb 2048
ENV max_metaspace_size_mb 384

EXPOSE 8080
EXPOSE 9990

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-full.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
