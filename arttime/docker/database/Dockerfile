FROM postgres:10.5

ARG ARTTIME_VERSION=latest

RUN apt-get update && apt-get install -y curl
RUN mkdir -p /opt/configuration


# Download database init scripts from latest release in Github

RUN export RELEASE_INFO=$(curl -L -s -H 'Accept: application/json' https://github.com/artezio/ART-TIME/releases/$ARTTIME_VERSION) && \
	export ARTTIME_ARTIFACTS_VERSION=$(echo $RELEASE_INFO | sed -e 's/.*"tag_name":"\([^"]*\)".*/\1/') && \
	mkdir /tmp/arttime-repo && \
    curl "https://github.com/Artezio/ART-TIME/archive/$ARTTIME_ARTIFACTS_VERSION.tar.gz" -L -o /tmp/arttime.tar.gz && \
	tar xvf /tmp/arttime.tar.gz --directory=/tmp/arttime-repo --strip-components=1 && \
	cp /tmp/arttime-repo/arttime/docker/database/init_arttime_demo_db.sql /docker-entrypoint-initdb.d/init_arttime_demo_db.sql && \
	rm -r /tmp/arttime-repo && rm /tmp/arttime.tar.gz

RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen

EXPOSE 5342
