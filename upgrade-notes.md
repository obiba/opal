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

### For plugin authors

`org.obiba.opal.core.domain.HasUniqueProperties`, `org.obiba.opal.core.validator.Unique` and its `UniqueValidator`
were removed from opal-core-api. They existed to serve the document store. Nothing in Opal used the annotation, and no
validator factory was ever wired for it, so it had never run.

## Version 5.0.x

### Orientdb 

https://dba.stackexchange.com/questions/333660/orientdb-wont-start-after-upgrade-cannot-create-user-guest-role-guest-does-no
rm -rf OPAL_HOME/data/orientdb/opal-config/databases/OSystem/
