FROM jboss/keycloak:4.8.3.Final

ARG ARTTIME_VERSION
ARG KEYCLOAK_ADMIN_LOGIN=admin
ARG KEYCLOAK_ADMIN_PASSWORD=password
ARG ARTTIME_REPO_URL=https://github.com/Artezio/ART-TIME/raw/${ARTTIME_VERSION}

# Set this variable to the host where keycloak is running
ENV KEYCLOAK_HOSTNAME arttime-demo-keycloak

# Download demo keycloak database
ADD --chown=1000 ${ARTTIME_REPO_URL}/arttime/docker/keycloak/keycloak.mv.db /opt/jboss/keycloak/standalone/data/keycloak.mv.db

# Download demo keycloak login theme
ADD --chown=1000 ${ARTTIME_REPO_URL}/arttime/docker/keycloak/themes/keycloak /opt/jboss/keycloak/themes/keycloak/

# Add admin user
RUN /opt/jboss/keycloak/bin/add-user.sh ${KEYCLOAK_ADMIN_LOGIN} ${KEYCLOAK_ADMIN_PASSWORD}

ENTRYPOINT ["/opt/jboss/tools/docker-entrypoint.sh"]
CMD ["-b", "0.0.0.0", "-Djboss.socket.binding.port-offset=1"]

EXPOSE 8081
