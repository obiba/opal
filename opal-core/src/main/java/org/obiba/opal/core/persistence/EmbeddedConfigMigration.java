/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.persistence;

import liquibase.change.Change;
import liquibase.change.core.CreateTableChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.obiba.core.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * Copies the embedded H2 configuration database into the external database Opal is now configured with, once.
 * <p>
 * It runs between Liquibase and Hibernate, from {@link ConfigDatabaseConfiguration}, so in whichever of the two runs of
 * {@code bin/opal} opens the configuration database first: the upgrade run. Its steps write to the configuration
 * database, and those writes must land after the copy. The server run then finds the target populated and does
 * nothing.
 * <p>
 * The rules that make it safe to leave the setting on and to restart:
 * <ul>
 *   <li>It only copies into an empty target. A target holding any row is left alone, whoever wrote it.</li>
 *   <li>It never writes to the H2 store: it reads a copy, and the original stays in {@code data/config} as the way
 *   back.</li>
 *   <li>The whole copy is one transaction on the target, committed only once every table has been read back with the
 *   same row count and the same digest as the source. Anything else rolls back and stops startup.</li>
 *   <li>Both databases must be at the same changelog: the store is copied table for table, column for column, and a
 *   changeset that only ran on the target may also have transformed data.</li>
 * </ul>
 * It works on plain JDBC, table by table, from the metadata of both databases, rather than through the entities: what
 * is copied is exactly what is stored, encrypted values included. The tables are the ones the configuration changelog
 * creates, and only those: the target schema may hold other tables, the data of a project for instance, which are
 * neither counted, copied, nor have their sequences touched.
 */
class EmbeddedConfigMigration {

  private static final Logger log = LoggerFactory.getLogger(EmbeddedConfigMigration.class);

  private static final int BATCH_SIZE = 500;

  enum Outcome {
    /** No embedded store to read from. */
    NOTHING_TO_MIGRATE,
    /** The target already holds a configuration, nothing was written. */
    SKIPPED,
    MIGRATED
  }

  private final File store;

  private final String user;

  private final Supplier<String> password;

  private final DataSource target;

  private final String changelog;

  /**
   * @param store the embedded store file, {@code opal-config.mv.db}
   * @param user the embedded database user
   * @param password the embedded database password, only asked for when there is something to migrate, so that a
   *                 setting left in place cannot fail a startup over a password the target does not need
   * @param target the configuration database Opal is configured with, its schema already created by Liquibase
   * @param changelog the class path of the changelog that created it
   */
  EmbeddedConfigMigration(File store, String user, Supplier<String> password, DataSource target, String changelog) {
    this.store = store;
    this.user = user;
    this.password = password;
    this.target = target;
    this.changelog = changelog;
  }

  Outcome run() {
    if(!store.exists()) {
      log.info("No embedded configuration database at {}: nothing to migrate", store.getAbsolutePath());
      return Outcome.NOTHING_TO_MIGRATE;
    }
    try(Connection to = target.getConnection()) {
      to.setAutoCommit(false);
      try {
        Outcome outcome = run(to);
        if(outcome == Outcome.MIGRATED) to.commit();
        else to.rollback();
        return outcome;
      } catch(SQLException | RuntimeException e) {
        to.rollback();
        throw e;
      }
    } catch(SQLException e) {
      throw new ConfigDatabaseException("Migrating the embedded configuration database failed, nothing was written: " +
          e.getMessage(), e);
    }
  }

  private Outcome run(Connection to) throws SQLException {
    String product = to.getMetaData().getDatabaseProductName();
    boolean postgres = "PostgreSQL".equalsIgnoreCase(product);
    if(!postgres && !"H2".equalsIgnoreCase(product)) {
      throw new ConfigDatabaseException(
          "Migrating the embedded configuration database is supported to PostgreSQL only, not to " + product);
    }

    List<String> tables = configurationTables(to);
    for(String table : tables) {
      long rows = count(to, table);
      if(rows > 0) {
        log.info("The configuration database at {} already holds a configuration ({} rows in {}): " +
                "the embedded one is not migrated. Remove config.datasource.migrateEmbedded from opal-config.properties.",
            to.getMetaData().getURL(), rows, table);
        return Outcome.SKIPPED;
      }
    }

    Path copy = copyStore();
    try(Connection from = DriverManager.getConnection(urlOf(copy), user, password.get())) {
      from.setAutoCommit(false);
      checkSameChangelog(from, to);
      migrate(from, to, tables, postgres);
      from.rollback();
    } finally {
      deleteQuietly(copy);
    }
    return Outcome.MIGRATED;
  }

  private void migrate(Connection from, Connection to, List<String> tables, boolean postgres) throws SQLException {
    log.info("Migrating the embedded configuration database to {}", to.getMetaData().getURL());
    log.info("  from: {}", store.getAbsolutePath());
    long started = System.currentTimeMillis();

    Map<String, TableDigest> copied = new LinkedHashMap<>();
    for(String table : tables) {
      copied.put(table, copy(from, to, table));
    }
    for(String table : tables) {
      verify(to, table, copied.get(table));
    }
    resetIdentities(to, tables, postgres);

    log.info("Configuration migration complete, in {} ms", System.currentTimeMillis() - started);
    copied.forEach((table, digest) -> {
      if(digest.rows > 0) log.info("  {}: {} rows", table, digest.rows);
    });
    log.info("The embedded configuration database has been left untouched at {}.", store.getParentFile());
    log.info("Remove config.datasource.migrateEmbedded from opal-config.properties, and that folder once this " +
        "installation has been verified; until then it is the way back.");
  }

  //
  // Copy and verification
  //

  private TableDigest copy(Connection from, Connection to, String table) throws SQLException {
    List<Column> columns = columns(to, table);
    String sourceTable = sourceName(from, table);
    List<String> sourceColumns = new ArrayList<>();
    for(Column column : columns) {
      sourceColumns.add(sourceColumnName(from, sourceTable, column.name));
    }

    String select = "SELECT " + join(from, sourceColumns) + " FROM " + quote(from, sourceTable);
    String insert = "INSERT INTO " + quote(to, table) + " (" + join(to, names(columns)) + ") VALUES (" +
        "?, ".repeat(columns.size() - 1) + "?)";

    TableDigest digest = new TableDigest();
    try(Statement read = from.createStatement(); ResultSet rs = read.executeQuery(select);
        PreparedStatement write = to.prepareStatement(insert)) {
      int pending = 0;
      while(rs.next()) {
        Object[] row = readRow(rs, columns);
        digest.add(row);
        checkStorable(table, columns, row, digest.rows);
        for(int i = 0; i < row.length; i++) {
          if(row[i] == null) write.setNull(i + 1, columns.get(i).type);
          else write.setObject(i + 1, row[i]);
        }
        write.addBatch();
        if(++pending == BATCH_SIZE) {
          write.executeBatch();
          pending = 0;
        }
      }
      if(pending > 0) write.executeBatch();
    }
    return digest;
  }

  /**
   * Read back what was written, within the same transaction, and compare it with what was read: the row count and an
   * order-independent digest of the rows, so that neither database's ordering of the keys matters.
   */
  private void verify(Connection to, String table, TableDigest expected) throws SQLException {
    List<Column> columns = columns(to, table);
    TableDigest actual = new TableDigest();
    try(Statement read = to.createStatement();
        ResultSet rs = read.executeQuery("SELECT " + join(to, names(columns)) + " FROM " + quote(to, table))) {
      while(rs.next()) actual.add(readRow(rs, columns));
    }
    if(actual.rows != expected.rows || !actual.sum.equals(expected.sum)) {
      throw new ConfigDatabaseException("Migrating the embedded configuration database failed on table " + table +
          ": " + expected.rows + " rows read, " + actual.rows + " rows found once written" +
          (actual.rows == expected.rows ? ", with different contents" : "") + ". Nothing was written.");
    }
  }

  /**
   * Values are read by the type of the target column, which is what they are written as: large objects as strings and
   * bytes rather than locators, which are only valid while the source row is current.
   */
  private Object[] readRow(ResultSet rs, List<Column> columns) throws SQLException {
    Object[] row = new Object[columns.size()];
    for(int i = 0; i < row.length; i++) {
      int index = i + 1;
      Object value = switch(columns.get(i).type) {
        case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> rs.getBytes(index);
        case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR, Types.CLOB, Types.NCHAR, Types.NVARCHAR,
             Types.LONGNVARCHAR, Types.NCLOB -> rs.getString(index);
        case Types.BOOLEAN, Types.BIT -> rs.getBoolean(index);
        case Types.TINYINT, Types.SMALLINT, Types.INTEGER, Types.BIGINT -> rs.getLong(index);
        case Types.TIMESTAMP -> rs.getTimestamp(index);
        default -> rs.getObject(index);
      };
      row[i] = rs.wasNull() ? null : value;
    }
    return row;
  }

  /**
   * PostgreSQL rejects a NUL character in text, with an error that names neither the table nor the row. H2 accepts it.
   */
  private void checkStorable(String table, List<Column> columns, Object[] row, long rowNumber) {
    for(int i = 0; i < row.length; i++) {
      if(row[i] instanceof String value && value.indexOf('\u0000') >= 0) {
        throw new ConfigDatabaseException("Migrating the embedded configuration database failed: table " + table +
            ", column " + columns.get(i).name + ", row " + rowNumber + " holds a NUL character, which PostgreSQL " +
            "cannot store. Nothing was written.");
      }
    }
  }

  /**
   * Inserting explicit ids leaves the sequences behind identity columns where they were: the first row Opal creates
   * afterwards would collide with a copied one.
   */
  private void resetIdentities(Connection to, List<String> tables, boolean postgres) throws SQLException {
    List<String[]> identities = new ArrayList<>();
    try(PreparedStatement ps = to.prepareStatement("SELECT table_name, column_name FROM information_schema.columns " +
        "WHERE table_schema = ? AND (is_identity = 'YES' OR column_default LIKE 'nextval(%')")) {
      ps.setString(1, to.getSchema());
      try(ResultSet rs = ps.executeQuery()) {
        while(rs.next()) {
          if(tables.contains(rs.getString(1))) identities.add(new String[] { rs.getString(1), rs.getString(2) });
        }
      }
    }
    for(String[] identity : identities) {
      String table = quote(to, identity[0]);
      String column = quote(to, identity[1]);
      long next = 1;
      try(Statement st = to.createStatement(); ResultSet rs = st.executeQuery("SELECT MAX(" + column + ") FROM " + table)) {
        if(rs.next()) next = rs.getLong(1) + 1;
      }
      if(postgres) {
        try(PreparedStatement ps = to.prepareStatement("SELECT setval(pg_get_serial_sequence(?, ?), ?, false)")) {
          ps.setString(1, table);
          ps.setString(2, identity[1]);
          ps.setLong(3, next);
          ps.execute();
        }
      } else {
        try(Statement st = to.createStatement()) {
          st.execute("ALTER TABLE " + table + " ALTER COLUMN " + column + " RESTART WITH " + next);
        }
      }
    }
  }

  //
  // Preconditions
  //

  /**
   * The copy maps the target schema onto the store column by column. A changeset that ran on one and not the other
   * would make that mapping wrong, and may have transformed data the copy would not.
   */
  private void checkSameChangelog(Connection from, Connection to) throws SQLException {
    Set<String> source = changesets(from);
    Set<String> target = changesets(to);
    if(source.equals(target)) return;
    Set<String> missing = new TreeSet<>(target);
    missing.removeAll(source);
    Set<String> extra = new TreeSet<>(source);
    extra.removeAll(target);
    throw new ConfigDatabaseException("The embedded configuration database is not at the same schema version as " +
        "this Opal (" + (missing.isEmpty() ? "" : "not applied there: " + missing + " ") +
        (extra.isEmpty() ? "" : "unknown here: " + extra) + "). Start this version of Opal once on the embedded " +
        "database, without config.datasource.* in opal-config.properties, then migrate. Nothing was written.");
  }

  private Set<String> changesets(Connection connection) throws SQLException {
    Set<String> ids = new HashSet<>();
    try(Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, author FROM opal_changelog")) {
      while(rs.next()) ids.add(rs.getString(1) + ":" + rs.getString(2));
    }
    return ids;
  }

  //
  // Metadata
  //

  /**
   * The tables of the target that the configuration changelog created, as the target spells them.
   */
  private List<String> configurationTables(Connection to) throws SQLException {
    Set<String> created = changelogTables();
    List<String> tables = new ArrayList<>();
    for(String name : tables(to)) {
      if(created.contains(name.toLowerCase(Locale.ROOT))) tables.add(name);
    }
    if(tables.size() != created.size()) {
      throw new ConfigDatabaseException("The configuration database at " + to.getMetaData().getURL() + " has " +
          tables.size() + " of the " + created.size() + " tables its changelog creates. Nothing was written.");
    }
    return tables;
  }

  private Set<String> changelogTables() {
    ResourceAccessor resources = new ClassLoaderResourceAccessor(getClass().getClassLoader());
    try {
      Set<String> names = new HashSet<>();
      for(ChangeSet changeSet : ChangeLogParserFactory.getInstance().getParser(changelog, resources)
          .parse(changelog, new ChangeLogParameters(), resources).getChangeSets()) {
        for(Change change : changeSet.getChanges()) {
          if(change instanceof CreateTableChange createTable) {
            names.add(createTable.getTableName().toLowerCase(Locale.ROOT));
          }
        }
      }
      return names;
    } catch(LiquibaseException e) {
      throw new ConfigDatabaseException("Cannot read the configuration changelog " + changelog, e);
    }
  }

  private List<String> tables(Connection connection) throws SQLException {
    List<String> tables = new ArrayList<>();
    try(ResultSet rs = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), "%", null)) {
      while(rs.next()) {
        String type = rs.getString("TABLE_TYPE");
        if("TABLE".equals(type) || "BASE TABLE".equals(type)) tables.add(rs.getString("TABLE_NAME"));
      }
    }
    return tables;
  }

  private List<Column> columns(Connection connection, String table) throws SQLException {
    List<Column> columns = new ArrayList<>();
    try(ResultSet rs = connection.getMetaData()
        .getColumns(connection.getCatalog(), connection.getSchema(), table, null)) {
      while(rs.next()) columns.add(new Column(rs.getString("COLUMN_NAME"), rs.getInt("DATA_TYPE")));
    }
    return columns;
  }

  /**
   * The same changelog names its tables and columns the same way on both databases; each folds the case its own way.
   */
  private String sourceName(Connection from, String table) throws SQLException {
    for(String name : tables(from)) {
      if(name.equalsIgnoreCase(table)) return name;
    }
    throw new ConfigDatabaseException("Table " + table + " is missing from the embedded configuration database");
  }

  private String sourceColumnName(Connection from, String table, String column) throws SQLException {
    for(Column candidate : columns(from, table)) {
      if(candidate.name.equalsIgnoreCase(column)) return candidate.name;
    }
    throw new ConfigDatabaseException(
        "Column " + table + "." + column + " is missing from the embedded configuration database");
  }

  private long count(Connection connection, String table) throws SQLException {
    try(Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + quote(connection, table))) {
      rs.next();
      return rs.getLong(1);
    }
  }

  private static String quote(Connection connection, String identifier) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    String q = metaData.getIdentifierQuoteString().trim();
    return q + identifier.replace(q, q + q) + q;
  }

  private static String join(Connection connection, List<String> identifiers) throws SQLException {
    List<String> quoted = new ArrayList<>();
    for(String identifier : identifiers) quoted.add(quote(connection, identifier));
    return String.join(", ", quoted);
  }

  private static List<String> names(List<Column> columns) {
    return columns.stream().map(c -> c.name).toList();
  }

  //
  // The store copy
  //

  /**
   * Opening an H2 store can write to it - recovering after an unclean stop, compacting - and the original has to stay
   * exactly as it was. The copy lives next to it, on the same file system.
   */
  private Path copyStore() {
    try {
      Path folder = Files.createTempDirectory(store.getParentFile().getParentFile().toPath(), "config-migration-");
      Files.copy(store.toPath(), folder.resolve(store.getName()), StandardCopyOption.COPY_ATTRIBUTES);
      return folder;
    } catch(IOException e) {
      throw new ConfigDatabaseException("Cannot copy the embedded configuration database " + store + ": " +
          e.getMessage(), e);
    }
  }

  private String urlOf(Path copy) {
    String name = store.getName().substring(0, store.getName().length() - ".mv.db".length());
    return "jdbc:h2:file:" + copy.resolve(name).toAbsolutePath();
  }

  private void deleteQuietly(Path folder) {
    try {
      FileUtil.delete(folder.toFile());
    } catch(IOException e) {
      log.warn("Cannot delete the copy of the embedded configuration database at {}: {}", folder, e.getMessage());
    }
  }

  private record Column(String name, int type) {}

  /**
   * Row count and the sum of the row digests: equal for the same rows in any order.
   */
  private static class TableDigest {

    private static final BigInteger MODULUS = BigInteger.ONE.shiftLeft(256);

    private long rows;

    private BigInteger sum = BigInteger.ZERO;

    private void add(Object[] row) {
      MessageDigest digest = sha256();
      for(Object value : row) {
        byte[] bytes;
        if(value == null) bytes = new byte[] { 0 };
        else if(value instanceof byte[] b) bytes = b;
        else if(value instanceof Timestamp t) bytes = (t.getTime() + "." + t.getNanos()).getBytes(StandardCharsets.UTF_8);
        else bytes = value.toString().getBytes(StandardCharsets.UTF_8);
        digest.update(ByteBuffer.allocate(4).putInt(value == null ? -1 : bytes.length).array());
        digest.update(bytes);
      }
      sum = sum.add(new BigInteger(1, digest.digest())).mod(MODULUS);
      rows++;
    }

    private static MessageDigest sha256() {
      try {
        return MessageDigest.getInstance("SHA-256");
      } catch(NoSuchAlgorithmException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
