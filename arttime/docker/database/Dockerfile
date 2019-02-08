FROM postgres:10.5

ADD init_arttime_db.sql /docker-entrypoint-initdb.d/
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
    locale-gen
