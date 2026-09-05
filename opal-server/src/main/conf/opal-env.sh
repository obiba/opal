#
# Environment for the Opal server, sourced by bin/opal.
#
# This file is the tarball equivalent of /etc/default/opal on the deb and rpm packages, and of the
# environment passed with -e on the Docker image. Anything exported here reaches the Opal JVM.
#

# Memory and GC settings for the JVM.
#export JAVA_OPTS="-Xms1G -Xmx2G -XX:+UseG1GC"

#
# OpenTelemetry
#
# Setting an OTLP endpoint is all that is needed: it turns on the audit log export, the DataSHIELD
# traces and the DataSHIELD metrics together. With no endpoint set, nothing is exported and the
# appenders declared in conf/logback.xml stay inert.
#
# Opal ships the OTLP/HTTP sender only, so the endpoint is the http/protobuf one - port 4318 on a
# standard collector, not the 4317 gRPC port.
#
#export OTEL_EXPORTER_OTLP_ENDPOINT=https://collector.example.org:4318

# Name reported to the backend. Defaults to "opal".
#export OTEL_SERVICE_NAME=opal

# Extra resource attributes, e.g. to tell nodes apart in a federated study.
#export OTEL_RESOURCE_ATTRIBUTES=deployment.environment=production,service.namespace=my-node

# The DataSHIELD stream carries submitted R expressions, usernames and client addresses. Off
# localhost it needs TLS, and a token if the collector wants one.
#export OTEL_EXPORTER_OTLP_CERTIFICATE=/path/ca.pem
#export OTEL_EXPORTER_OTLP_CLIENT_CERTIFICATE=/path/client.pem
#export OTEL_EXPORTER_OTLP_CLIENT_KEY=/path/client.key
#export OTEL_EXPORTER_OTLP_HEADERS=Authorization=Bearer%20...

# Metrics are exported every 60s by default; lower it while trying things out.
#export OTEL_METRIC_EXPORT_INTERVAL=5000

# The OpenTelemetry Java agent adds the JDBC, Mongo and HTTP client calls made during a DataSHIELD
# operation. They nest inside the DataSHIELD spans, so a session trace then also shows the R server
# round trips and the SQL underneath each audited operation. Opal needs no change for this.
#
# Two things to expect. The agent logs a warning that GlobalOpenTelemetry.set calls are ignored:
# that is normal, Opal keeps its own SDK for the log appenders and uses the agent's for spans and
# metrics. And the HTTP server spans stay in traces of their own - a DataSHIELD trace is rooted on
# the session, not on a request, so it does not nest under the request that started it.
#export JAVA_OPTS="$JAVA_OPTS -javaagent:/path/opentelemetry-javaagent.jar"
