# Trying the OpenTelemetry export by hand

`DatashieldOtelExportTest` already asserts what gets exported, in process. This is for looking at the
real thing: a collector on localhost printing each record as it arrives.

```bash
docker compose -f src/test/resources/otel/docker-compose.yml up
```

Then point an Opal at it:

```bash
cp $OPAL_HOME/conf/logback.otel.xml $OPAL_HOME/conf/logback.xml
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
$OPAL_DIST/bin/opal
```

Opal prints `OpenTelemetry export enabled.` at startup when the endpoint is picked up; if it does not,
the SDK was never built and nothing will be exported. Run a DataSHIELD session and the collector
prints records on the `datashield.user` scope carrying `datashield.action`, `datashield.profile`,
`datashield.session.id`, `datashield.script`, `enduser.id` and `client.address`.

Plain HTTP is fine against localhost. Anywhere else the stream carries submitted R expressions and
usernames, so it needs `https://` and the TLS settings documented in `conf/logback.otel.xml`.
