# Trying the OpenTelemetry export by hand

The tests already assert what gets exported, in process: `DatashieldOtelExportTest` for the audit
records, `DataShieldTracerTest` for the spans, `DataShieldMetricsTest` for the instruments. This is
for looking at the real thing - a collector on localhost printing all three signals as they arrive.

```bash
docker compose -f src/test/resources/otel/docker-compose.yml up
```

Then point an Opal at it:

```bash
cp $OPAL_HOME/conf/logback.otel.xml $OPAL_HOME/conf/logback.xml
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
$OPAL_DIST/bin/opal
```

Opal prints `OpenTelemetry export enabled.` at startup when the endpoint is picked up. If it does
not, the SDK was never built and nothing at all will be exported - check the variable is exported to
the process, and note that `OTEL_EXPORTER_OTLP_LOGS_ENDPOINT` on its own also works.

Now open a DataSHIELD session and run an aggregation. Three things should show up.

**Logs**, on the `datashield.user` scope, one record per audited action:

    datashield.action=AGGREGATE  datashield.profile=default  datashield.session.id=...
    datashield.script=meanDS(D$age)  enduser.id=administrator  client.address=...

The `PARSE` record carries `datashield.script.submitted`, `.generated` and `.mapping` instead - what
the user wrote, what it was rewritten to, and the function mapping in between.

**Traces**, on the `org.obiba.opal.datashield` scope: `datashield.aggregate`, `datashield.assign`,
`datashield.open`, `datashield.close`, `datashield.ws_save`, `datashield.ws_restore`. A failed
operation carries status ERROR and the recorded exception. Spans are roots here; run Opal under the
OpenTelemetry Java agent and they hang under the HTTP server span instead, with no change to Opal.

**Metrics**, same scope, exported every 60s by default - export `OTEL_METRIC_EXPORT_INTERVAL=5000`
if that is too long to wait:

    datashield.operation.count      by action, profile, outcome
    datashield.operation.duration   seconds, same dimensions
    datashield.session.active       open sessions by profile, read on collection
    datashield.quota.rejection      by quota metric, when a user is over their allowance

`datashield.session.active` is observed rather than counted, so it is correct even after a session
times out; leave a session idle past its timeout and watch it drop.

Plain HTTP is fine against localhost. Anywhere else these streams carry submitted R expressions and
usernames, so they need `https://` and the TLS settings documented in `conf/logback.otel.xml`.
