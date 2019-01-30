FROM postgres:10.5

ADD init_arttime_db.sql /docker-entrypoint-initdb.d/

EXPOSE 5432

CMD ["postgres"]