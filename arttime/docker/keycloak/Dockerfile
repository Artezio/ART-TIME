FROM jboss/keycloak:4.8.3.Final

ADD --chown=jboss:jboss keycloak.mv.db /opt/jboss/keycloak/standalone/data/

ARG keycloak_admin_login=admin
ARG keycloak_admin_password=admin

#ENV KEYCLOAK_SERVER_URL $keycloak_server_url
#ENV KEYCLOAK_REALM $keycloak_realm
#ENV KEYCLOAK_CLIENT_ID $keycloak_client_id

RUN /opt/jboss/keycloak/bin/add-user.sh ${keycloak_admin_login} ${keycloak_admin_password}

ENTRYPOINT ["/opt/jboss/tools/docker-entrypoint.sh"]
CMD ["-b", "0.0.0.0", "-Djboss.socket.binding.port-offset=1", "-bmanagement", "0.0.0.0"]

EXPOSE 8081
EXPOSE 9991
