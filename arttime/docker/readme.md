# Art-Time Docker image

The image contains Art-Time with Keycloak and embedded h2 database

### Building the image from Dockerfile

1. Download the `Dockerfile`
2. Use the following command in the directory with `Dockerfile`:

   ```
   docker build --build-arg KEYCLOAK_ADAPTER_VERSION=<keycloak_version> --build-arg ARTTIME_CLI_ADMIN_LOGIN=<cli_admin_login> --build-arg ARTTIME_CLI_ADMIN_PASSWORD=<cli_admin_password> --tag arteziollc/art-time .
   ```
   
   Arguments used:
   
   * `KEYCLOAK_ADAPTER_VERSION` (optional, for using with external Keycloak server). Specifies which Keycloak Wildfly Adapter version to use. Usually this version is the same as your Keycloak server version (e.g. `4.8.0.Final`). Default value `4.8.3.Final` is used for running embedded Keycloak
   * `ARTTIME_CLI_ADMIN_LOGIN`, `ARTTIME_CLI_ADMIN_PASSWORD` (optional). Specify admin login and password for accessing server management console (http://localhost:9990/). Default values are `admin`, `admin` respectively


### Start demo with embedded Keycloak and database
```
docker run -p 8080:8080 -p 9080:9080 arteziollc/art-time
```


### Start art-time with external Keycloak and database, and set memory limits. Env variables are explained below
```
docker run -p 8080:8080 -e KEYCLOAK_SERVER_URL="http://<keycloak_host>:<port>/auth" -e KEYCLOAK_CLIENT_ID=<client_id> -e KEYCLOAK_REALM=<realm> -e ARTTIME_DB_VENDOR=<vendor> -e ARTTIME_DB_HOST=<host> -e ARTTIME_DB_PORT=<port> -e ARTTIME_DB_NAME=<db_name> -e ARTTIME_DB_LOGIN=<login> -e ARTTIME_DB_PASSWORD=<password> -e MAX_HEAP_SIZE_MB=<max_size> arteziollc/arttime
```

### Published applications

* Art-Time web application (http://localhost:8080/arttime)
* Art-Time server management console (http://localhost:9990)
* Embedded Keycloak management console (http://localhost:9080/auth) for demo purposes only


### Web Application

Memory limit is set through `MAX_HEAP_SIZE_MB` environment variable, which will set maximum usage in megabytes


### Keycloak

Art-Time uses [Keycloak](https://www.keycloak.org/) as IDM

The image is bundled with pre-configured Keycloak server for demo purposes only. For production use, switch to external Keycloak server using the following environment variables:

* `KEYCLOAK_SERVER_URL` to `http://<host>:<port>/auth/`, where `host` and `port` are external Keycloak server host and port
* `KEYCLOAK_CLIENT_ID`
* `KEYCLOAK_REALM`
* `KEYCLOAK_USERNAME_ATTRIBUTE` to the attribute which is used as username (e.g. `preferred_username` or `email`)

To use external Keycloak server as data source for Art-Time, you also need to configure it in Art-Time Settings page.


### Database configuration

Art-Time can be used with [H2](http://www.h2database.com), [PostgreSQL](https://www.postgresql.org/) databases.

This image is bundled with H2 database for demo purposes only. For production use, switch to an external database using the following environment variables:

* `ARTTIME_DB_VENDOR` can be set to `h2`(default, uses embedded H2 server) or `postgres`. All settings below only have effect if this setting differs from `h2`
* `ARTTIME_DB_HOST` - host or IP of database
* `ARTTIME_DB_PORT` - database port
* `ARTTIME_DB_NAME` - database name
* `ARTTIME_DB_LOGIN` - login
* `ARTTIME_DB_PASSWORD` - password

Database driver version can be set through the following variables:

* `JDBC_POSTGRES_VERSION` - PostgreSQL Driver version. Consult (https://jdbc.postgresql.org/download.html) to determine which version to use with your Postgres server. Default is `42.2.2` for postgres version **8.2** or greater


### Default users

Keycloak demo server contains the following users:

* `admin` - Admin, has admin access to both Art-Time and demo Keycloak
* `exec` - Executive
* `projectmanager` - Project Manager
* `officemanager` - Office Manager 1
* `officemanager2` - Office Manager 2
* `accountant` - Accountant
* `employee1` - Employee 1
* `employee2` - Employee 2
* `employee3` - Employee 3
* `employee4` - Employee 4

Password for all users is `password`


### Security

This image exposes ports 8080, 9080 and 9990.

Port 8080 is used for accessing Art-Time and may be considered safe for publishing

Port 9990 is used for accessing Art-Time server management console (JBoss) and is configured by default with login `admin` and password `admin`. For production use, either don't publish this port or rebuild the image with ARTTIME_CLI_ADMIN_LOGIN and ARTTIME_CLI_ADMIN_PASSWORD arguments set.

Port 9080 is used for demo Keycloak server and is not required in production. When running the demo server, the user which can access Keycloak administration console is `admin` with password `password`
