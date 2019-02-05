FROM jboss/keycloak:4.7.0.Final

ADD --chown=jboss:jboss keycloak.mv.db /opt/jboss/keycloak/standalone/data/

RUN /opt/jboss/keycloak/bin/add-user.sh admin admin

ENTRYPOINT ["/opt/jboss/tools/docker-entrypoint.sh"]
CMD ["-b", "0.0.0.0", "-Djboss.socket.binding.port-offset=1"]