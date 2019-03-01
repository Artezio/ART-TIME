mkdir -p /opt/jboss/wildfly/modules/system/layers/base/com/mysql/jdbc/main
cd /opt/jboss/wildfly/modules/system/layers/base/com/mysql/jdbc/main
curl -L http://central.maven.org/maven2/mysql/mysql-connector-java/$JDBC_MYSQL_VERSION/mysql-connector-java-$JDBC_MYSQL_VERSION.jar > mysql-jdbc.jar
cp /opt/jboss/tools/arttime/databases/mysql/module.xml .

mkdir -p /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/jdbc/main
cd /opt/jboss/wildfly/modules/system/layers/base/org/postgresql/jdbc/main
curl -L http://central.maven.org/maven2/org/postgresql/postgresql/$JDBC_POSTGRES_VERSION/postgresql-$JDBC_POSTGRES_VERSION.jar > postgres-jdbc.jar
cp /opt/jboss/tools/arttime/databases/postgres/module.xml .

mkdir -p /opt/jboss/wildfly/modules/system/layers/base/org/mariadb/jdbc/main
cd /opt/jboss/wildfly/modules/system/layers/base/org/mariadb/jdbc/main
curl -L http://central.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/$JDBC_MARIADB_VERSION/mariadb-java-client-$JDBC_MARIADB_VERSION.jar > mariadb-jdbc.jar
cp /opt/jboss/tools/arttime/databases/mariadb/module.xml .

# TODO: Uncomment when msqql is supported
#mkdir -p /opt/jboss/wildfly/modules/system/layers/base/com/microsoft/mssql/jdbc/main
#cd /opt/jboss/wildfly/modules/system/layers/base/com/microsoft/mssql/jdbc/main
#curl -L http://central.maven.org/maven2/com/microsoft/sqlserver/mssql-jdbc/$JDBC_MSSQL_VERSION/mssql-jdbc-$JDBC_MSSQL_VERSION.jar > mssql-jdbc.jar
#cp /opt/jboss/tools/arttime/databases/mssql/module.xml .


