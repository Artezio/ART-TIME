#!/bin/bash -e

DB_VENDOR=$1

cd /opt/jboss/wildfly

SERVER_OPTS=--server-config=standalone-full.xml
export MAX_HEAP_SIZE_MB=1024
export MAX_METASPACE_SIZE_MB=256
export JAVA_OPTS="-server -Xms256m -Xmx1024m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true -Djboss.as.management.blocking.timeout=1200"

bin/jboss-cli.sh --file=/opt/jboss/tools/cli/databases/$DB_VENDOR/standalone-configuration.cli
rm -rf /opt/jboss/keycloak/standalone/configuration/standalone_xml_history

