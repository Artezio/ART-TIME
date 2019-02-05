FROM jboss/wildfly:14.0.1.Final

ENV keycloak_url http://localhost:8080/auth/

ADD arttime-webapp.war /opt/jboss/wildfly/standalone/deployments/
ADD configuration /opt/configuration/
ADD start.sh /opt/

#install keycloak adapter
RUN curl -SL https://downloads.jboss.org/keycloak/4.5.0.Final/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-4.5.0.Final.tar.gz \
    | tar xvz -C /opt/jboss/wildfly/
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/adapter-install-offline.cli -Dserver.config=standalone-full.xml
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/adapter-elytron-install-offline.cli -Dserver.config=standalone-full.xml

#configure wildfly
RUN sed -i -e "s/<resolve-parameter-values>false/<resolve-parameter-values>true/" /opt/jboss/wildfly/bin/jboss-cli.xml
RUN /opt/jboss/wildfly/bin/add-user.sh --user admin --password admin --silent --enable
WORKDIR /opt/configuration/wildfly/
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --properties=app.subsystems.properties --file=configure-wildfly.cli

CMD ["/bin/sh", "-c", "/opt/start.sh"]