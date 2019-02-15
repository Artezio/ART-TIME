FROM jboss/keycloak:4.8.3.Final

ARG ARTTIME_VERSION=latest
ARG keycloak_admin_login=admin
ARG keycloak_admin_password=admin

USER root

RUN mkdir -p /opt/configuration
RUN chown 1000 /opt/configuration -R

USER 1000


# Download demo keycloak database from Github

RUN export RELEASE_INFO=$(curl -L -s -H 'Accept: application/json' https://github.com/artezio/ART-TIME/releases/$ARTTIME_VERSION) && \
	export ARTTIME_ARTIFACTS_VERSION=$(echo $RELEASE_INFO | sed -e 's/.*"tag_name":"\([^"]*\)".*/\1/') && \
	mkdir /tmp/arttime-repo && \
    curl "https://github.com/Artezio/ART-TIME/archive/$ARTTIME_ARTIFACTS_VERSION.tar.gz" -L -o /tmp/arttime.tar.gz && \
	tar xvf /tmp/arttime.tar.gz --directory=/tmp/arttime-repo --strip-components=1 && \
	cp /tmp/arttime-repo/arttime/docker/keycloak/keycloak.mv.db /opt/jboss/keycloak/standalone/data/keycloak.mv.db && \
	rm -r /tmp/arttime-repo && rm /tmp/arttime.tar.gz

RUN /opt/jboss/keycloak/bin/add-user.sh ${keycloak_admin_login} ${keycloak_admin_password}

ENTRYPOINT ["/opt/jboss/tools/docker-entrypoint.sh"]
CMD ["-b", "0.0.0.0", "-Djboss.socket.binding.port-offset=1"]

EXPOSE 8081
