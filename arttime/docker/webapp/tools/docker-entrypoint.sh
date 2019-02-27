#!/bin/bash

############
# DB setup #
############

# Lower case ARTTIME_DB_VENDOR
ARTTIME_DB_VENDOR=`echo $ARTTIME_DB_VENDOR | tr A-Z a-z`

# Default to postgres if DB type not detected
if [ "$ARTTIME_DB_VENDOR" == "" ]; then
    export ARTTIME_DB_VENDOR="postgres"
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

/bin/sh /opt/jboss/tools/databases/change-database.sh $ARTTIME_DB_VENDOR


##################
# Start Arttime #
##################

exec /opt/jboss/wildfly/bin/standalone.sh $@
exit $?