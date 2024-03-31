#!/bin/bash

$JAVA $JAVA_ARGS -cp "${OPAL_HOME}/conf:${OPAL_DIST}/lib/*" -DOPAL_HOME=${OPAL_HOME} -DOPAL_DIST=${OPAL_DIST} -DOPAL_LOG=${OPAL_LOG} -Dpolyglot.log.file=${OPAL_LOG}/polyglot.log -Dpolyglot.engine.WarnInterpreterOnly=false org.obiba.opal.server.OpalServer $OPAL_ARGS
