#!/bin/bash

############
# DB setup #
############

# Lower case ARTTIME_DB_VENDOR
ARTTIME_DB_VENDOR=`echo $ARTTIME_DB_VENDOR | tr A-Z a-z`

# Default to h2 if DB type not detected
if [ "$ARTTIME_DB_VENDOR" == "" ]; then
    export ARTTIME_DB_VENDOR="h2"
fi

# Set DB name
case "$ARTTIME_DB_VENDOR" in
    postgres)
        DB_NAME="PostgreSQL";;
    mysql)
        DB_NAME="MySQL";;
    mariadb)
        DB_NAME="MariaDB";;
    mssql)
        DB_NAME="MSSQL";;
    oracle)
        DB_NAME="Oracle";;
    h2)
        DB_NAME="embedded H2";;
    *)
        echo "Unknown DB vendor $ARTTIME_DB_VENDOR"
        exit 1
esac

# Append '?' in the beggining of the string if JDBC_PARAMS value isn't empty
export JDBC_PARAMS=$(echo ${JDBC_PARAMS} | sed '/^$/! s/^/?/')

# Convert deprecated DB specific variables
function set_legacy_vars() {
  local suffixes=(ADDR DATABASE USER PASSWORD PORT)
  for suffix in "${suffixes[@]}"; do
    local varname="$1_$suffix"
    if [ ${!varname} ]; then
      echo WARNING: $varname variable name is DEPRECATED replace with DB_$suffix
      export DB_$suffix=${!varname}
    fi
  done
}
set_legacy_vars `echo $ARTTIME_DB_VENDOR | tr a-z A-Z`

# Configure DB

echo "========================================================================="
echo ""
echo "  Art-Time is using $DB_NAME database"
echo ""
echo "========================================================================="
echo ""

/bin/sh /opt/jboss/tools/arttime/databases/change-database.sh $ARTTIME_DB_VENDOR


##################
# Start Arttime #
##################

echo "========================================================================="

function start_embedded_keycloak() {
  echo "Starting embedded Keycloak server"
  local JBOSS_HOME=/opt/jboss/keycloak
  /opt/jboss/tools/docker-entrypoint-keycloak.sh "-Djboss.socket.binding.port-offset=1000" "-b" "0.0.0.0" &
}

# Start Keycloak if required
if [[ $KEYCLOAK_SERVER_URL == 'http://127.0.0.1:9080/auth' ]]; then
  start_embedded_keycloak
else
  echo Using external Keycloak
fi


# Start Wildfly and return result
echo "Starting Art-Time"
export JAVA_OPTS="-server -Xms256m -Xmx${MAX_HEAP_SIZE_MB}m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=${MAX_METASPACE_SIZE_MB}m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true -Djboss.as.management.blocking.timeout=1200"
exec /opt/jboss/wildfly/bin/standalone.sh "-c" "standalone-full.xml" "-b" "0.0.0.0" "-bmanagement" "0.0.0.0" ${JBOSS_ARGUMENTS}
exit $?
