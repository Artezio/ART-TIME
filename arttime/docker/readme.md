# Art-Time Docker image

The image contains Art-Time with Keycloak and embedded h2 database

```
docker run -p 8080:8080 -p 9080:9080 artezio/arttime
```

### Published applications

* Art-Time web application (http://localhost:8080/arttime)
* Art-Time server web management interface (http://localhost:9990)
* Keycloak demo (http://localhost:9080/auth)

### Base image

The image extends [jboss/wildfly:14.0.1.Final](https://github.com/jboss-dockerfiles/wildfly)

### Keycloak

Art-Time uses [Keycloak](https://www.keycloak.org/) as IDM

The image is bundled with pre-configured demo Keycloak server

To switch to external Keycloak server, set the following environment variables:

* `KEYCLOAK_SERVER_URL` to `http://<host>:<port>/auth/`, where `host` and `port` are external Keycloak server host and port
* `KEYCLOAK_CLIENT_ID`
* `KEYCLOAK_REALM`

The following variables are only required if Keycloak is selected as External Data Provider in Art-Time:

* `KEYCLOAK_LOGIN`  User with `view-users` role. This user will be used to connect to Keycloak API and list users
* `KEYCLOAK_PASSWORD` Password of the user which will be used to connect to Keycloak API

### Database

Art-Time can be used with [H2](http://www.h2database.com), [PostgreSQL](https://www.postgresql.org/) databases.

The image is bundled with demo H2 database

To switch to external database, set the following environment variables:

* `ARTTIME_DB_VENDOR` to `postgres`. All settings below only have effect if this setting differs from `h2`
* `ARTTIME_DB_HOST` - host or IP of database
* `ARTTIME_DB_PORT` - database port (5432 for postgres)
* `ARTTIME_DB_NAME` - database name
* `ARTTIME_DB_LOGIN` - login
* `ARTTIME_DB_PASSWORD` - password
