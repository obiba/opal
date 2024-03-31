#!/bin/bash

CLASSPATH="${OPAL_HOME}/conf:${OPAL_DIST}/lib/*"

PLUGINS_DIR="${OPAL_HOME}/plugins/"
[ -e "${PLUGINS_DIR}/.archive" ] || mkdir -p "${PLUGINS_DIR}/.archive"

# Iterate over plugins zip files
find "$PLUGINS_DIR" -type f -name "*-dist.zip" | grep -v ".archive" | while IFS= read -r zip_file; do
    # Unzip each zip file
    unzip -u "$zip_file" -d "${PLUGINS_DIR}"
    # Move the zip file to archive folder
    mv "$zip_file" "${PLUGINS_DIR}/.archive"
done

# Append plugins lib
while IFS= read -r dir; do
  CLASSPATH="$CLASSPATH:$dir"
done < <(find "${OPAL_HOME}/plugins/" -type d -name lib | grep -v ".archive")

$JAVA ${JAVA_ARGS} -cp "${CLASSPATH}" -DOPAL_HOME="${OPAL_HOME}" -DOPAL_DIST="${OPAL_DIST}" -DOPAL_LOG="${OPAL_LOG}" -Dpolyglot.log.file="${OPAL_LOG}/polyglot.log" -Dpolyglot.engine.WarnInterpreterOnly=false org.obiba.opal.server.OpalServer $OPAL_ARGS
