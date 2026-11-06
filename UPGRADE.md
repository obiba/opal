# Opal Upgrade Notes

What a system administrator has to do when upgrading a production Opal instance to 6.0.0. The actions are listed in
the order they are taken: before stopping the old Opal, at the first start of the new one, and afterwards. Items
marked *optional* enable something new and can be skipped. For what changed and why, see the release notes.

## Upgrading to 6.0.0

### Before the upgrade

1. **Back up `${OPAL_HOME}/data` and `${OPAL_HOME}/conf` together.** The upgrade migrates the configuration database
   on first start and the previous state is what you roll back to. In particular keep `data/orientdb` and
   `data/opal-config.xml` from the same moment.

2. **Decide where the configuration database will live.** By default it becomes an embedded H2 database in
   `${OPAL_HOME}/data/config`, and nothing has to be done. To keep it on PostgreSQL instead, do this *before* the first
   start of the new Opal, because the migration writes to whatever is configured at that moment. Pointing an
   already-migrated Opal at an empty PostgreSQL gives you an empty configuration.

   Create an empty database, then add to `${OPAL_HOME}/conf/opal-config.properties`:

       config.datasource.url=jdbc:postgresql://localhost:5432/opal_config
       config.datasource.driverClass=org.postgresql.Driver
       config.datasource.username=opal
       config.datasource.password=secret
       config.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

   Opal creates the schema itself. The password is required for an external database.

3. **List the "Import from Opal" sources the instance uses** (web interface imports, `opal.assign.table` in R,
   identifiers imports). Opal now verifies the remote server's certificate and host name, and imports from an Opal with
   a self-signed certificate fail after the upgrade. For each such source, either:
   - import the remote certificate under *Administration > Identities > Credentials* on this Opal, or
   - set `org.obiba.opal.security.ssl.allowInvalidCertificates=true` in `conf/opal-config.properties`. This disables
     verification for every outbound connection of this Opal, not only these imports.

### Install and start

4. **Install the new version and start it once, then read the startup log.** The configuration migration from
   OrientDB to H2 runs by itself and reports the record counts it found, one line per class as it migrates, and a
   summary. The log is the only place those counts appear. Do not declare the upgrade done before reading it.

   - If the migration fails part way, the log names the class and the record it was on. Fix the cause and start Opal
     again: the OrientDB folder is untouched and a repeated run updates what is already written and adds what is
     missing. Running it again is always safe.
   - If a class wrote fewer records than it read, it is logged as a warning. Compare the counts for the class named
     before going further.

5. **On rpm, merge the `.rpmnew` files into yours.** `/etc/opal` and `/etc/default/opal` are installed `noreplace`,
   so new versions of `logback.xml`, `opal-config.properties`, `opal` and the others are left beside yours as
   `*.rpmnew` rather than replacing them.

### After the upgrade

6. **Move the configuration backup from `data/orientdb` to `data/config`.** A script that backs up
   `${OPAL_HOME}/data/orientdb` keeps succeeding after the upgrade and backs up nothing Opal reads any more.

7. **Back up `data/config` and `data/opal-config.xml` as a pair.** The configuration database is opened with a password
   Opal generates and stores encrypted in `data/opal-config.xml`, under the secret key in that same file. Restored from
   different backups, or with a replaced `<secretKey>`, Opal cannot open its own configuration and there is no way to
   recover the database without the key that was in use when it was created. Test a restore before you need it.

8. **Back up `${OPAL_HOME}/data/h2` as a whole folder, not as a list of known files.** Besides the H2 databases
   registered by an administrator it now holds a folder per project that was created with an *internal database*:

       ${OPAL_HOME}/data/h2/<project name>/data.mv.db

9. **Rotate the credentials used for "Import from Opal".** Until this version those imports did not verify the remote
    certificate, so the personal access tokens and passwords sent to remote Opals were exposed to anyone on the network
    path.

10. **Keep `${OPAL_HOME}/data/orientdb` until the upgraded instance is verified, then delete it.** Nothing reads it any
    more. It is the way back: rolling back means reinstalling the previous Opal version, which finds its configuration
    where it left it. Configuration changes made after the upgrade are lost in a rollback, because they were written to
    the new database.

### Optional: enable the OpenTelemetry export

Opal can send its logs, its DataSHIELD traces and its DataSHIELD metrics to an OpenTelemetry collector. Nothing is
exported, connected or printed until `OTEL_EXPORTER_OTLP_ENDPOINT` is set. If you do not want this, there is nothing
to do. Full settings: <https://opaldoc.obiba.org/en/latest/admin/configuration.html#opentelemetry>.

11. **Set the `OTEL_*` variables where the packaging expects them.**
    - zip: copy `conf/opal-env.sh` from the distribution into `${OPAL_HOME}/conf/` and put `JAVA_OPTS` and the `OTEL_*`
      variables there. `bin/opal` sources it when present, and unlike `bin/opal` it is not replaced by the next
      upgrade. Nothing copies it into an existing `OPAL_HOME` for you.
    - deb, rpm: put them in `/etc/default/opal`. A collector credential does not go there, because that file is world
      readable; put it in `/etc/default/opal-secrets`, which the service also reads, and `chmod 600` it. systemd reads
      it before dropping to the `opal` user.

12. **Add the OpenTelemetry appenders to `${OPAL_HOME}/conf/logback.xml`.** The upgrade never writes into that file,
    so an instance upgraded from 5.x has none of them and exports traces and metrics but not one log record. Opal
    warns at startup when that is the case:

        OpenTelemetry export enabled.
        WARNING: conf/logback.xml declares no OpenTelemetry appender, so no log record will be exported ...

    Copy the `otel`, `otelrest`, `otelraw` and `otelds` appenders, and the `appender-ref` entries that use them, from
    the distribution's `logback.xml` into yours. That file is at:
    - zip: `<dist>/conf/logback.xml`
    - deb: `/usr/share/opal-server-<version>/conf/logback.xml`
    - rpm: `/etc/opal/logback.xml.rpmnew` (see step 5)
    - docker: `/usr/share/opal/conf/logback.xml`

    The format of `${OPAL_HOME}/logs/datashield.log` is unchanged; tools that parse it keep working.

13. **Configure TLS to the collector unless it runs on `localhost`.** The DataSHIELD stream carries the R expressions
    users submit, their user names and their client addresses.

### Be aware of

These need no action, but change what an administrator is accountable for.

- **Projects can now own their database.** At creation, a project may be given an *internal database* that Opal creates
  in `data/h2/<project name>/`. Existing projects are not affected. Such databases are listed under
  *Administration > Databases* with the owning project named, cannot be edited or deleted from there while the project
  exists, and **are deleted with the project** together with everything in them. Deleting a project stored in a
  registered database still leaves the database itself alone. `DELETE /project/{name}?archive=true` keeps the internal
  database; it then shows in the databases page with no project using it, and can be deleted from there. The web
  interface always deletes without archiving.
- **Whoever may create projects can now provision storage** for that project, without system administration rights.
  This gives no access to the H2 folder or to other databases.
- **Unregistering an H2 database can now delete its files**, as a checkbox in the confirmation dialog or as
  `DELETE /system/database/{name}?deleteFiles=true`. Left unticked, the files stay as before. Note that H2 keeps the
  user name and password inside the file: a database registered again at the same URL with a different password cannot
  be opened unless the old file was deleted.

### If something goes wrong

- **"Cannot open the Opal configuration database ... the password does not match"**: `data/config` and
  `data/opal-config.xml` come from different installations, or the `<secretKey>` in that file was replaced. Restore
  both from the same backup.
- **An "Import from Opal" task fails with a TLS error**: see step 3.
- **Rolling back**: reinstall the previous Opal version with the `data/orientdb` folder still in place (step 10).
  Configuration changes made on 6.0 are lost.
