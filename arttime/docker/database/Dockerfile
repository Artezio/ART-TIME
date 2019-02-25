FROM postgres:10.5

ARG ARTTIME_VERSION
ARG ARTTIME_REPO_URL=https://github.com/Artezio/ART-TIME/raw/${ARTTIME_VERSION}

# Download demo database generation script
ADD --chown=postgres ${ARTTIME_REPO_URL}/arttime/docker/database/init_arttime_demo_db.sql /docker-entrypoint-initdb.d/init_arttime_demo_db.sql

# Ensure that the required system locale is available to postgres
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen

EXPOSE 5342
