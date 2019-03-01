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

### Web Application

Memory limit is set through `MAX_HEAP_SIZE_MB` environment variable, which will set maximum usage in megabytes

### Keycloak

Art-Time uses [Keycloak](https://www.keycloak.org/) as IDM

The image is bundled with pre-configured demo Keycloak server

To switch to external Keycloak server, set the following environment variables:

* `KEYCLOAK_SERVER_URL` to `http://<host>:<port>/auth/`, where `host` and `port` are external Keycloak server host and port
* `KEYCLOAK_CLIENT_ID`
* `KEYCLOAK_REALM`

### Database

Art-Time can be used with [H2](http://www.h2database.com), [PostgreSQL](https://www.postgresql.org/) databases.

The image is bundled with demo H2 database

To switch to external database, set the following environment variables:

* `ARTTIME_DB_VENDOR` can be set to `h2`(default, uses embedded H2 server) or `postgres`. All settings below only have effect if this setting differs from `h2`
* `ARTTIME_DB_HOST` - host or IP of database
* `ARTTIME_DB_PORT` - database port (usually, 5432 for postgres)
* `ARTTIME_DB_NAME` - database name
* `ARTTIME_DB_LOGIN` - login
* `ARTTIME_DB_PASSWORD` - password

### Default users

Keycloak demo server contains the following users:

* `exec` - Executive
* `pm` - Project Manager
* `officemanager` - Office Manager
* `admin` - Admin
* `accountant` - Accountant
* `employee1` - Employee 1
* `employee2` - Employee 2
* `employee3` - Employee 3
* `employee4` - Employee 4

Password for all users is `password`


### Building the image from Dockerfile

1. Download the `Dockerfile`
2. Use the following command:

   ```
   docker build --build-arg ARTTIME_VERSION=<version> --build-arg KEYCLOAK_ADAPTER_VERSION=<keycloak_version> --build-arg ARTTIME_CLI_ADMIN_LOGIN=<cli_admin_login> --build-arg ARTTIME_CLI_ADMIN_PASSWORD=<cli_admin_password>
   ```
   
* `ARTTIME_VERSION` is required. This argument must point to an existing release tag (e.g. v3.83) in [Art-Time repository](https://github.com/Artezio/ART-TIME/)
* `KEYCLOAK_ADAPTER_VERSION` specifies Keycloak Wildfly adapter version to use
* `ARTTIME_CLI_ADMIN_LOGIN` and `ARTTIME_CLI_ADMIN_PASSWORD` specify admin login and password for accessing server management console (http://localhost:9990/)

### Security

This image exposes ports 8080, 9080 and 9990.

Port 8080 is used for accessing Art-Time and may be considered safe for publishing

Port 9990 is used for accessing Art-Time server management console (JBoss) and is configured by default with login `admin` and password `admin`. For production use, either don't publish this port or rebuild the image with ARTTIME_CLI_ADMIN_LOGIN and ARTTIME_CLI_ADMIN_PASSWORD arguments set.

Port 9080 is used for demo Keycloak server and is not required in production. When running the demo server, the user which can access Keycloak administration console is `admin` with password `password`
