FROM jboss/keycloak:4.7.0.Final

ADD --chown=jboss:jboss keycloak.mv.db /opt/jboss/keycloak/standalone/data/

RUN /opt/jboss/keycloak/bin/add-user.sh admin admin