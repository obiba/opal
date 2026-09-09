# Opal Upgrade Notes

## Version 6.0.x

### The configuration database

Opal no longer keeps its own configuration - projects, users, permissions, registered databases, resources,
DataSHIELD profiles, R activity - in OrientDB. It is now an embedded H2 database in a new folder:

    ${OPAL_HOME}/data/config

The move happens by itself the first time an upgraded Opal starts, and needs nothing from you. It reports what it is
doing as it goes: the record counts it found, a line per class as it migrates, and a summary. If something fails it
names the record it was on. Read that log before declaring the upgrade done - it is the only place the counts appear.

Three things about it are worth knowing before you upgrade.

**Your backups are now pointing at the wrong folder.** If a script backs up `${OPAL_HOME}/data/orientdb`, it will keep
succeeding and keep producing files, and none of them will contain anything Opal reads any more. The folder is still
there because the upgrade deliberately leaves it alone. Point the backup at `${OPAL_HOME}/data/config` instead, and
check it before you need it.

**`data/config` and `data/opal-config.xml` have to be restored together.** The configuration database is opened with a
password that Opal generates for itself and keeps in `${OPAL_HOME}/data/opal-config.xml`, encrypted under the secret
key in that same file. The database is in one place and the key to it is in another, in two directories that are often
backed up separately. Restore them from different installations, or replace the secret key, and Opal will not open its
own configuration. It says so clearly when that happens, naming both files, but the fix is to restore the pair.

Nothing is lost by this compared with before - OrientDB used a fixed, published password and had its security switched
off, so the old folder protected nothing. But it did mean the folder could be moved on its own, and now it cannot.

**Keep `${OPAL_HOME}/data/orientdb` until you are satisfied.** It is untouched, and it is the way back: rolling this
upgrade back means reinstalling the previous Opal version, which will find its configuration exactly where it left it.
Any configuration change made after the upgrade is lost in that case, because it was written to the new database. Once
the upgraded installation has been verified, the folder can be deleted; nothing reads it.

### Using PostgreSQL instead

The configuration database does not have to be the embedded one. To keep it on a PostgreSQL server, create an empty
database and add to `${OPAL_HOME}/conf/opal-config.properties`:

    org.obiba.opal.config.datasource.url=jdbc:postgresql://localhost:5432/opal_config
    org.obiba.opal.config.datasource.driverClass=org.postgresql.Driver
    org.obiba.opal.config.datasource.username=opal
    org.obiba.opal.config.datasource.password=secret
    org.obiba.opal.config.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

Opal creates the schema itself on first start. An external database needs a password here: the generated one applies to
the embedded database only. Set this before the first start of the upgraded Opal, so that the migration writes there
directly; pointing an already-migrated Opal at an empty PostgreSQL gives you an empty configuration.

### If something goes wrong

**"Cannot open the Opal configuration database ... the password does not match"** - `data/config` and
`data/opal-config.xml` have come from different installations, or the `<secretKey>` in that file was replaced. Restore
both from the same backup. There is no way to recover the database without the key that was in use when it was
created.

**The migration log says a class wrote fewer records than it read.** It is a warning rather than a failure, and the
counts are in the log. Compare them against the class it names before going further; the OrientDB folder is still
intact, so nothing has been lost either way.

**The migration fails part way.** It names the class and the record it was on. Nothing has been taken from the
OrientDB folder, so the run can be repeated once the cause is dealt with. Each record is written under the key the
configuration identifies it by, so a repeated run updates what is already there and adds what is missing, whether it
starts from nothing, from a half-written table or from a complete one. Running it again is always safe.

### A project can own its database

Until now a project could only store its data in a database an operator had registered beforehand. That is right for
MySQL, MariaDB, PostgreSQL and MongoDB, where the database exists on a server outside Opal and its lifetime is not
Opal's business. It was never right for H2, where the database is a file Opal creates in its own folder: registering it
by hand bought nothing, and left nobody owning the file.

Creating a project now offers a third choice, beside a registered database and no storage at all: an **internal
database**, which Opal creates for that project alone, in a folder of its own:

    ${OPAL_HOME}/data/h2/<project name>/data.mv.db

Nothing changes for the projects you have. This is a choice at creation, and an existing project keeps the database it
was given. Moving a project's data into a database of its own is a copy rather than a setting, and Opal already has the
tool for it: create a project with internal storage and copy the tables into it.

**Deleting a project deletes its internal database and everything in it. Deleting a project stored in a registered
database does not** - there, Opal drops the tables the project put in the database and leaves the database itself
alone, as it always has. This is the difference to know before choosing, and the project creation dialog says which of
the two you are choosing as you choose it.

Deleting a project *and keeping its data* is still `DELETE /project/{name}?archive=true`, and it keeps the internal
database too: the row and the folder stay behind, belonging to nobody. They show in the databases page with the former
project named and nothing using them, and an operator can delete them there - which is the one place a project's
database is an operator's to remove, and which removes its files as well. Creating a project of that name again takes
the data back. The web interface always deletes without archiving.

**These databases are listed like every other one.** They appear under *Administration > Databases* with the owning
project named beside them, because an operator is accountable for the disk and a store holding 40 GB must not be
invisible in the one page that shows what Opal keeps. What is different is who manages them: while the owning project
exists they cannot be edited or deleted from that page - the attempt is refused - and the settings dialog opens
read only. Testing the connection works as it does for any database.

**A project creator now provisions storage.** Whoever may create projects can give one a place to put its tables,
where before that needed system administration as well. Nothing else changes: it gives no access to the H2 folder or to
another project's database.

**Back up `${OPAL_HOME}/data/h2` as project data.** It already held the H2 databases an operator registered; it now
also holds a folder per project that has one. A backup that copies the folder is complete; one that lists the files it
knows about is not.

### Unregistering an H2 database can now delete its files

An H2 database is a file in `${OPAL_HOME}/data/h2`, and that file used to outlive the registration: unregistering the
database left it there, and nothing in Opal ever removed it. That is a trap, because H2 keeps the user name and
password inside the file. Register a database again at the same URL with a different password and it cannot be opened -
the credentials that count are the ones the file was created with, not the ones you have just typed.

Unregistering an H2 database therefore now offers to delete its files, as a checkbox in the confirmation, or as
`DELETE /system/database/{name}?deleteFiles=true`. It is never implied: left unticked, the behaviour is what it has
always been, and what an operator declared may hold data nobody meant to lose. For a database a project owns, the files
go in any case, so there is nothing to choose.

### If you are running a 6.0 snapshot

Skip this if you are upgrading from Opal 5.x - it applies only to an installation that has already started a 6.0
snapshot build, such as a test server or a CI job.

The `owner_project` column that carries the ownership above was added to the initial changeset of the configuration
schema rather than as a migration of its own, because 6.0 is unreleased and every installation creates that schema from
scratch on its way to it. That changes the changeset's checksum, and Liquibase refuses to start rather than ignore it.
An installation that has already run the previous schema therefore has to build it again:

    rm -rf ${OPAL_HOME}/data/config

The OrientDB migration then runs on the next start, as it did the first time. Nothing else is needed, and nothing is
lost that was not already in `${OPAL_HOME}/data/orientdb` - but any configuration change made since that migration ran
is, so if the snapshot holds configuration nobody wants to re-migrate, take a copy of `data/config` first and ask
before deleting it.

### DataSHIELD activity over OpenTelemetry

Opal can now send its logs, its DataSHIELD traces and its DataSHIELD metrics to an OpenTelemetry collector. Nothing is
exported until you set `OTEL_EXPORTER_OTLP_ENDPOINT`: an installation that never sets it builds no SDK, opens no
connection, sends nothing and prints nothing. If this is not something you want, there is nothing to do.

**The format of `${OPAL_HOME}/logs/datashield.log` has not changed.** The `ds_*` fields are renamed to OpenTelemetry
names on the way to the collector only, in an appender placed after the file appender, and a test in the build compares
a written line against a fixture to keep it that way. Tools that parse the file keep working, and the file remains the
local record when the collector is unreachable.

**Turning the log export on needs a change to `${OPAL_HOME}/conf/logback.xml` that the upgrade does not make for you.**
That file belongs to your installation and you may well have edited it, so nothing writes into it - which means an Opal
upgraded from 5.x keeps a `logback.xml` with no OpenTelemetry appenders in it. Configure an endpoint on such an
installation and it will export its traces and its metrics and not one log record. Opal now says so at startup rather
than leaving you to discover it:

    OpenTelemetry export enabled.
    WARNING: conf/logback.xml declares no OpenTelemetry appender, so no log record will be exported ...

The fix is to copy the `otel`, `otelrest`, `otelraw` and `otelds` appenders - and the `appender-ref` entries that use
them - from the distribution's `logback.xml` into yours. Where that copy is depends on the packaging:

  - zip: `<dist>/conf/logback.xml`
  - deb: `/usr/share/opal-server-<version>/conf/logback.xml`
  - rpm: there is no `conf` directory under `/usr/share`; `/etc/opal` is installed `noreplace`, so the upgrade leaves
    the new file beside yours as `/etc/opal/logback.xml.rpmnew`
  - docker: `/usr/share/opal/conf/logback.xml`

**A new file, `conf/opal-env.sh`,** is sourced by `bin/opal` when it is present. It is where `JAVA_OPTS` and the `OTEL_*`
variables belong on a zip installation: it sits in `OPAL_HOME`, so the next upgrade will not replace it the way it
replaces `bin/opal`. It ships in the distribution's `conf` directory and nothing copies it into an existing
`OPAL_HOME`, so copy it across yourself if you want it.

On the deb and rpm packages those variables go in `/etc/default/opal` instead. A credential for the collector does not:
that file is world readable, so put it in the new `/etc/default/opal-secrets`, which the service also reads and which
can be `chmod 600` and owned by root - systemd reads it before dropping to the `opal` user.

**The DataSHIELD stream is sensitive.** It carries the R expressions users submit, their user names and their client
addresses - that is what makes it worth auditing, and it also makes the collector a processor of that content. Anywhere
other than a collector on `localhost`, configure TLS.

The settings are documented in full under *OpenTelemetry* in the administration guide:
<https://opaldoc.obiba.org/en/latest/admin/configuration.html#opentelemetry>.

### Importing from another Opal verifies its certificate

"Import from Opal" - a transient datasource of the REST kind, whether from the web interface, `opal.assign.table` or
the identifiers import - used to accept any certificate from the remote Opal, and any host name. It now verifies the
remote server like every other server Opal talks to: the certificate must be issued by an authority the JVM trusts or
be among the certificates of Opal's credentials key store, and its name must match the URL.

An import from an Opal with a self-signed certificate therefore fails after the upgrade, with a TLS error in the
task's log. Either import that certificate under *Administration > Identities > Credentials* on the importing Opal, or
set `org.obiba.opal.security.ssl.allowInvalidCertificates=true` in `conf/opal-config.properties`, which switches
verification off for every outbound connection of that Opal, not only these.

Since the credentials sent to the remote Opal were exposed to whoever sat on the network path, rotate the personal
access tokens and passwords that were used for such imports.

### For plugin authors

`org.obiba.opal.core.domain.HasUniqueProperties`, `org.obiba.opal.core.validator.Unique` and its `UniqueValidator`
were removed from opal-core-api. They existed to serve the document store. Nothing in Opal used the annotation, and no
validator factory was ever wired for it, so it had never run.

## Version 5.0.x

### Orientdb 

https://dba.stackexchange.com/questions/333660/orientdb-wont-start-after-upgrade-cannot-create-user-guest-role-guest-does-no
rm -rf OPAL_HOME/data/orientdb/opal-config/databases/OSystem/
