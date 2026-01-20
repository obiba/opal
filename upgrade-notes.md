# Opal Upgrade Notes

## Version 5.5 - Java 25 + H2 Migration

### Breaking Change: Java 25 + Database Migration Required

Opal 5.5 upgrades from Java 21 to Java 25 and replaces OrientDB with H2 for
configuration storage. **Manual data migration is required.**

### Why Migration is Required

OrientDB is incompatible with Java 22+ due to removed internal APIs (`sun.misc.Unsafe.ensureClassInitialized`).
The configuration database has been migrated to H2. Your existing configuration
data must be exported BEFORE upgrading.

### Migration Process Overview

```
┌─────────────────────────────────────┐     ┌─────────────────────────────────────┐
│  BEFORE UPGRADE (Opal 5.4.x)        │     │  AFTER UPGRADE (Opal 5.5.x)         │
│  Java 21 + OrientDB                 │     │  Java 25 + H2                       │
├─────────────────────────────────────┤     ├─────────────────────────────────────┤
│  1. Stop Opal                       │     │  4. Install Opal 5.5                │
│  2. Backup OrientDB directory       │ ──► │  5. Import configuration            │
│  3. Export to JSON                  │     │  6. Start Opal                      │
└─────────────────────────────────────┘     └─────────────────────────────────────┘
```

### Step 1: Export Configuration (BEFORE upgrading)

**This step MUST be completed before upgrading. OrientDB data cannot be read by Opal 5.5+.**

```bash
# Stop Opal
sudo systemctl stop opal

# Backup OrientDB data (recommended)
cp -r $OPAL_HOME/data/orientdb $OPAL_HOME/data/orientdb.backup

# Export configuration to JSON (using your CURRENT Opal installation)
cd /usr/share/opal
java -jar opal-server.jar OrientDbUtil export /tmp/opal-backup.json

# Verify export file was created
ls -la /tmp/opal-backup.json
```

The export file contains all configuration entities:
- Configuration: OpalGeneralConfig, AppsConfig, Database, KeyStoreState
- Security: Group, SubjectAcl, SubjectProfile, SubjectToken
- Projects: Project, ProjectMetrics, VCFSamplesMapping, ResourceReference
- Analysis: OpalAnalysis
- R Integration: RSessionActivity, DataShieldProfile
- Runtime: PodSpec, App

### Step 2: Upgrade Opal

**Debian/Ubuntu:**
```bash
# Install Java 25
apt install openjdk-25-jdk

# Install new Opal version
apt install opal=5.5.0
```

**Docker:**
```bash
# Pull new image
docker pull obiba/opal:5.5
```

### Step 3: Import Configuration (AFTER upgrading)

```bash
# Import configuration into H2
cd /usr/share/opal
java -jar opal-server.jar OrientDbUtil import /tmp/opal-backup.json

# Start Opal
sudo systemctl start opal

# Verify startup
tail -f /var/log/opal/opal.log
```

### Rollback Procedure

If issues occur after migration:

```bash
# Stop new Opal
sudo systemctl stop opal

# Restore OrientDB backup
cp -r $OPAL_HOME/data/orientdb.backup $OPAL_HOME/data/orientdb

# Reinstall Opal 5.4.x and Java 21
apt install opal=5.4.x openjdk-21-jdk

# Start old version
sudo systemctl start opal
```

### Export File Format

The export produces a JSON array that can be inspected/edited if needed:

```json
[
  {
    "entity_type": "Project",
    "unique_key": "name=myproject",
    "document": { /* full entity JSON */ },
    "created": 1705123456789,
    "updated": 1705123456789
  }
]
```

### New Data Location

After migration, configuration data is stored in:
- `$OPAL_HOME/data/h2/opal-config.mv.db`

The old OrientDB directory (`$OPAL_HOME/data/orientdb`) can be deleted after successful migration verification.

### Troubleshooting

**Export fails with Java version error:**
- Ensure you're using Java 21 with Opal 5.4.x for the export step
- Java 22+ cannot read OrientDB data

**Import reports "file not found":**
- Check the export file path is correct
- Ensure the file was created during Step 1

**Opal won't start after import:**
- Check logs at `$OPAL_HOME/logs/opal.log`
- Verify Java 25 is installed: `java -version`
- Ensure H2 data directory exists: `$OPAL_HOME/data/h2/`

### Reference

This migration approach follows the same pattern used by Sonatype Nexus Repository Manager
for their OrientDB to H2 migration.

---

## Version 5.0.x

### OrientDB Upgrade Issue

If OrientDB fails to start after upgrading to 5.0.x with guest user errors:

```bash
rm -rf $OPAL_HOME/data/orientdb/opal-config/databases/OSystem/
```

See: https://dba.stackexchange.com/questions/333660/orientdb-wont-start-after-upgrade-cannot-create-user-guest-role-guest-does-no
