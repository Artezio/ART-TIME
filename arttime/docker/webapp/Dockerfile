# Input environment variables are:
# keycloak_server_url - keycloak auth url with schema and port, e.g. http://example.com:2244/auth
# keycloak_client_id - keycloak client id
# keycloak_realm - keycloak realm
# arttime_db_host - database host or IP
# arttime_db_port - database port
# arttime_db_name - database name
# arttime_db_login - database login
# arttime_db_password - database password

FROM jboss/wildfly:14.0.1.Final

ADD arttime-webapp.war /opt/jboss/wildfly/standalone/deployments/
ADD configuration /opt/configuration/

#install keycloak adapter
RUN curl -SL https://downloads.jboss.org/keycloak/4.8.3.Final/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-4.8.3.Final.tar.gz \
    | tar xvz -C /opt/jboss/wildfly/
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/adapter-install-offline.cli -Dserver.config=standalone-full.xml
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/bin/adapter-elytron-install-offline.cli -Dserver.config=standalone-full.xml

#configure wildfly
RUN sed -i -e "s/<resolve-parameter-values>false/<resolve-parameter-values>true/" /opt/jboss/wildfly/bin/jboss-cli.xml
RUN /opt/jboss/wildfly/bin/add-user.sh --user admin --password admin --silent --enable
WORKDIR /opt/configuration/wildfly/

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --properties=app.subsystems.properties --file=configure-wildfly.cli

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-full.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
