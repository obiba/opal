# Trying the OpenTelemetry export by hand

The tests already assert what gets exported, in process: `DatashieldOtelExportTest` for the audit
records, `DataShieldTracerTest` for the spans, `DataShieldMetricsTest` for the instruments. This is
for looking at the real thing - a collector on localhost printing all three signals as they arrive.

```bash
docker compose -f src/test/resources/otel/docker-compose.yml up
```

Then point an Opal at it:

```bash
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
$OPAL_DIST/bin/opal
```

That one variable is the whole of it: the appenders are already in `conf/logback.xml` and inert until
an endpoint is set, and the same variable turns on the traces and the metrics. On a permanent install
put it in `$OPAL_HOME/conf/opal-env.sh` (tarball), `/etc/default/opal` (deb/rpm) or `-e` (Docker).

Opal prints `OpenTelemetry export enabled.` at startup when the endpoint is picked up. If it does
not, the SDK was never built and nothing at all will be exported - check the variable is exported to
the process, and note that `OTEL_EXPORTER_OTLP_LOGS_ENDPOINT` on its own also works.

Now open a DataSHIELD session and run an aggregation. Three things should show up.

**Logs**, on the `datashield.user` scope, one record per audited action:

    datashield.action=AGGREGATE  datashield.profile=default  datashield.session.id=...
    datashield.script=meanDS(D$age)  enduser.id=administrator  client.address=...

The `PARSE` record carries `datashield.script.submitted`, `.generated` and `.mapping` instead - what
the user wrote, what it was rewritten to, and the function mapping in between.

**Traces**, on the `org.obiba.opal.datashield` scope: one trace per DataSHIELD session, rooted on a
`datashield.session` span that stays open for as long as the session does, with `datashield.open`,
`datashield.assign`, `datashield.parse`, `datashield.aggregate`, `datashield.ws_save`,
`datashield.ws_restore` and `datashield.close` underneath it. The operations are exported as they
end, so the trace is readable while the session is still open; the root arrives when the session is
closed, expires, or Opal shuts down. A failed operation carries status ERROR and the recorded
exception - `datashield.parse` in particular, for a script the restriction refused. Run Opal under
the OpenTelemetry Java agent and the session trace stays a trace of its own, linked to the HTTP
request spans that asked for each operation.

**Metrics**, same scope, exported every 60s by default - export `OTEL_METRIC_EXPORT_INTERVAL=5000`
if that is too long to wait:

    datashield.operation.count      by action, profile, outcome
    datashield.operation.duration   seconds, same dimensions
    datashield.session.active       open sessions by profile, read on collection
    datashield.quota.rejection      by quota metric, when a user is over their allowance

`datashield.session.active` is observed rather than counted, so it is correct even after a session
times out; leave a session idle past its timeout and watch it drop.

Plain HTTP is fine against localhost. Anywhere else these streams carry submitted R expressions and
usernames, so they need `https://` and the TLS settings documented in `conf/logback.xml`.
