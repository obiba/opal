#!/bin/bash

CLASSPATH="${OPAL_HOME}/conf:${OPAL_DIST}/lib/*"

PLUGINS_DIR="${OPAL_HOME}/plugins/"
[ -e "${PLUGINS_DIR}/.archive" ] || mkdir -p "${PLUGINS_DIR}/.archive"

# Iterate over deprecated plugins
find "$PLUGINS_DIR" -type d -name "opal-search-*" | grep -v ".archive" | while IFS= read -r deprecated; do
    # Move the deprecated folder to archive folder
    mv "$deprecated" "${PLUGINS_DIR}/.archive"
done

# Iterate over plugins zip files
find "$PLUGINS_DIR" -type f -name "*-dist.zip" | grep -v ".archive" | while IFS= read -r zip_file; do
    # Unzip each zip file
    unzip -o "$zip_file" -d "${PLUGINS_DIR}"
    # Move the zip file to archive folder
    mv "$zip_file" "${PLUGINS_DIR}/.archive"
done

# Append plugins lib
while IFS= read -r dir; do
  if ! ls "$dir/../uninstall" > /dev/null 2>&1; then
    CLASSPATH="$CLASSPATH:$dir"
  fi
done < <(find "${OPAL_HOME}/plugins/" -type d -name lib | grep -v ".archive")

APP_OPTS="${JAVA_ARGS} -cp ${CLASSPATH} -DOPAL_HOME=${OPAL_HOME} -DOPAL_DIST=${OPAL_DIST} -DOPAL_LOG=${OPAL_LOG} -Dpolyglot.log.file=${OPAL_LOG}/polyglot.log -Dpolyglot.engine.WarnInterpreterOnly=false"
$JAVA $APP_OPTS org.obiba.opal.server.OpalServer --upgrade
$JAVA $APP_OPTS org.obiba.opal.server.OpalServer
