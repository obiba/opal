/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.*;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import org.obiba.opal.core.domain.HasUniqueProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Component
public class H2OrientDbServiceImpl implements OrientDbService {

  private static final Logger log = LoggerFactory.getLogger(H2OrientDbServiceImpl.class);
  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  @Autowired
  private OrientDbServerFactory serverFactory;

  private final Gson gson = new GsonBuilder()
      .setDateFormat(DATE_PATTERN)
      .registerTypeAdapter(Date.class, new DateDeserializer())
      .create();

  private Connection getConnection() {
    try {
      return serverFactory.getConnection();
    } catch (Exception e) {
      throw new DatabaseException("Failed to get database connection", e);
    }
  }

  @Override
  public <T> T execute(WithinDocumentTxCallback<T> callback) {
    Connection conn = getConnection();
    try {
      return callback.withinDocumentTx(conn);
    } finally {
      closeQuietly(conn);
    }
  }

  @Override
  public <T> Iterable<T> list(Class<T> clazz) {
    String entityType = clazz.getSimpleName();
    List<T> results = new ArrayList<>();
    Connection conn = getConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.prepareStatement("SELECT document FROM documents WHERE entity_type = ?");
      stmt.setString(1, entityType);
      rs = stmt.executeQuery();
      while (rs.next()) {
        results.add(fromJson(rs.getString("document"), clazz));
      }
    } catch (Exception e) {
      throw new DatabaseException("Failed to list entities of type " + entityType, e);
    } finally {
      closeQuietly(rs);
      closeQuietly(stmt);
      closeQuietly(conn);
    }
    return results;
  }

  @Override
  public <T> Iterable<T> list(Class<T> clazz, String sql, Object... params) {
    List<T> allDocs = Lists.newArrayList(list(clazz));
    return filterByParams(allDocs, sql, params);
  }

  private <T> List<T> filterByParams(List<T> docs, String sql, Object[] params) {
    String lowerSql = sql.toLowerCase();
    int whereIndex = lowerSql.indexOf("where");
    if (whereIndex == -1) {
      return docs;
    }

    String whereClause = sql.substring(whereIndex + 5).trim();

    // Remove ORDER BY clause if present
    int orderByIndex = whereClause.toLowerCase().indexOf("order by");
    if (orderByIndex > 0) {
      whereClause = whereClause.substring(0, orderByIndex).trim();
    }

    String[] conditions = whereClause.split("(?i)\\s+and\\s+");

    // Parse conditions into field names and null checks
    List<String> fieldNames = new ArrayList<>();
    List<String> notNullFields = new ArrayList<>();

    for (String condition : conditions) {
      String trimmed = condition.trim();
      String lowerCondition = trimmed.toLowerCase();

      if (lowerCondition.contains("is not null")) {
        String fieldName = trimmed.replaceAll("(?i)\\s+is\\s+not\\s+null", "").trim();
        notNullFields.add(fieldName);
      } else if (lowerCondition.contains("is null")) {
        // Handle IS NULL if needed in future
      } else if (trimmed.contains("=")) {
        String[] parts = trimmed.split("\\s*=\\s*");
        if (parts.length >= 1) {
          fieldNames.add(parts[0].trim());
        }
      }
    }

    if (fieldNames.size() != params.length) {
      log.warn("SQL parsing mismatch: {} fields but {} params in query: {}", fieldNames.size(), params.length, sql);
      return docs;
    }

    List<T> filtered = new ArrayList<>();
    for (T doc : docs) {
      if (matchesConditions(doc, fieldNames, params, notNullFields)) {
        filtered.add(doc);
      }
    }
    return filtered;
  }

  private <T> boolean matchesConditions(T doc, List<String> fieldNames, Object[] params, List<String> notNullFields) {
    JsonObject jsonObj = gson.toJsonTree(doc).getAsJsonObject();

    // Check equality conditions
    for (int i = 0; i < fieldNames.size(); i++) {
      String fieldName = fieldNames.get(i);
      Object expectedValue = params[i];

      JsonElement fieldElement = jsonObj.get(fieldName);
      if (fieldElement == null || fieldElement.isJsonNull()) {
        if (expectedValue != null) {
          return false;
        }
      } else {
        String actualValue = fieldElement.isJsonPrimitive()
            ? fieldElement.getAsString()
            : fieldElement.toString();
        if (!Objects.equals(actualValue, String.valueOf(expectedValue))) {
          return false;
        }
      }
    }

    // Check NOT NULL conditions
    for (String fieldName : notNullFields) {
      JsonElement fieldElement = jsonObj.get(fieldName);
      if (fieldElement == null || fieldElement.isJsonNull()) {
        return false;
      }
    }

    return true;
  }

  @Override
  public <T> long count(Class<T> clazz) {
    String entityType = clazz.getSimpleName();
    Connection conn = getConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.prepareStatement("SELECT COUNT(*) FROM documents WHERE entity_type = ?");
      stmt.setString(1, entityType);
      rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getLong(1);
      }
    } catch (Exception e) {
      throw new DatabaseException("Failed to count entities of type " + entityType, e);
    } finally {
      closeQuietly(rs);
      closeQuietly(stmt);
      closeQuietly(conn);
    }
    return 0;
  }

  @Nullable
  @Override
  public <T> T uniqueResult(Class<T> clazz, String sql, Object... params) {
    Iterable<T> results = list(clazz, sql, params);
    Iterator<T> iterator = results.iterator();
    if (!iterator.hasNext()) {
      return null;
    }
    T result = iterator.next();
    if (iterator.hasNext()) {
      throw new IllegalArgumentException(
          "Non unique result for query '" + sql + "' with args: " + Arrays.asList(params));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends HasUniqueProperties> T findUnique(@NotNull HasUniqueProperties template) {
    String entityType = template.getClass().getSimpleName();
    String uniqueKey = buildUniqueKey(template);
    Connection conn = getConnection();
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.prepareStatement("SELECT document FROM documents WHERE entity_type = ? AND unique_key = ?");
      stmt.setString(1, entityType);
      stmt.setString(2, uniqueKey);
      rs = stmt.executeQuery();
      if (rs.next()) {
        return (T) fromJson(rs.getString("document"), template.getClass());
      }
    } catch (Exception e) {
      throw new DatabaseException("Failed to find unique entity", e);
    } finally {
      closeQuietly(rs);
      closeQuietly(stmt);
      closeQuietly(conn);
    }
    return null;
  }

  @Override
  public void save(@Nullable HasUniqueProperties template, @NotNull HasUniqueProperties hasUniqueProperties)
      throws ConstraintViolationException {
    Preconditions.checkArgument(hasUniqueProperties != null, "hasUniqueProperties cannot be null");
    save(ImmutableMap.of(template == null ? hasUniqueProperties : template, hasUniqueProperties));
  }

  @Override
  public void save(@NotNull Map<HasUniqueProperties, HasUniqueProperties> beansByTemplate)
      throws ConstraintViolationException {
    Preconditions.checkArgument(beansByTemplate != null, "beansByTemplate cannot be null");
    Preconditions.checkArgument(!beansByTemplate.isEmpty(), "beansByTemplate cannot be empty");

    Connection conn = getConnection();
    try {
      doSave(conn, beansByTemplate);
    } finally {
      closeQuietly(conn);
    }
  }

  private void doSave(Connection conn, Map<HasUniqueProperties, HasUniqueProperties> beansByTemplate) {
    try {
      conn.setAutoCommit(false);
      for (Map.Entry<HasUniqueProperties, HasUniqueProperties> entry : beansByTemplate.entrySet()) {
        HasUniqueProperties template = entry.getKey();
        HasUniqueProperties entity = entry.getValue();
        String entityType = entity.getClass().getSimpleName();
        String templateKey = buildUniqueKey(template);
        String entityKey = buildUniqueKey(entity);

        boolean exists = checkExists(conn, entityType, templateKey);
        Date now = new Date();

        String document = toJsonWithTimestamps(entity, now, exists);
        Timestamp timestamp = new Timestamp(now.getTime());

        if (exists) {
          doUpdate(conn, entityKey, document, timestamp, entityType, templateKey);
          log.debug("Updated {} with key {}", entityType, entityKey);
        } else {
          doInsert(conn, entityType, entityKey, document, timestamp);
          log.debug("Inserted {} with key {}", entityType, entityKey);
        }
      }
      conn.commit();
    } catch (Exception e) {
      rollbackQuietly(conn);
      throw new DatabaseException("Failed to save entities", e);
    }
  }

  private String toJsonWithTimestamps(Object entity, Date now, boolean isUpdate) {
    JsonObject jsonObj = gson.toJsonTree(entity).getAsJsonObject();
    String dateStr = new SimpleDateFormat(DATE_PATTERN).format(now);

    jsonObj.addProperty("updated", dateStr);
    if (!isUpdate) {
      jsonObj.addProperty("created", dateStr);
    }

    return gson.toJson(jsonObj);
  }

  private boolean checkExists(Connection conn, String entityType, String uniqueKey) {
    PreparedStatement stmt = null;
    ResultSet rs = null;
    try {
      stmt = conn.prepareStatement("SELECT id FROM documents WHERE entity_type = ? AND unique_key = ?");
      stmt.setString(1, entityType);
      stmt.setString(2, uniqueKey);
      rs = stmt.executeQuery();
      return rs.next();
    } catch (Exception e) {
      throw new DatabaseException("Failed to check existence for " + entityType + "/" + uniqueKey, e);
    } finally {
      closeQuietly(rs);
      closeQuietly(stmt);
    }
  }

  private void doUpdate(Connection conn, String entityKey, String document, Timestamp now,
                        String entityType, String templateKey) {
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(
          "UPDATE documents SET unique_key = ?, document = ?, updated = ? WHERE entity_type = ? AND unique_key = ?");
      stmt.setString(1, entityKey);
      stmt.setString(2, document);
      stmt.setTimestamp(3, now);
      stmt.setString(4, entityType);
      stmt.setString(5, templateKey);
      stmt.executeUpdate();
    } catch (Exception e) {
      throw new DatabaseException("Failed to update " + entityType + "/" + entityKey, e);
    } finally {
      closeQuietly(stmt);
    }
  }

  private void doInsert(Connection conn, String entityType, String entityKey, String document,
                        Timestamp now) {
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(
          "INSERT INTO documents (entity_type, unique_key, document, created, updated) VALUES (?, ?, ?, ?, ?)");
      stmt.setString(1, entityType);
      stmt.setString(2, entityKey);
      stmt.setString(3, document);
      stmt.setTimestamp(4, now);
      stmt.setTimestamp(5, now);
      stmt.executeUpdate();
    } catch (Exception e) {
      throw new DatabaseException("Failed to insert " + entityType + "/" + entityKey, e);
    } finally {
      closeQuietly(stmt);
    }
  }

  @Override
  public void delete(@NotNull HasUniqueProperties... templates) {
    Connection conn = getConnection();
    try {
      doDelete(conn, templates);
    } finally {
      closeQuietly(conn);
    }
  }

  private void doDelete(Connection conn, HasUniqueProperties... templates) {
    PreparedStatement stmt = null;
    try {
      conn.setAutoCommit(false);
      stmt = conn.prepareStatement("DELETE FROM documents WHERE entity_type = ? AND unique_key = ?");
      for (HasUniqueProperties template : templates) {
        stmt.setString(1, template.getClass().getSimpleName());
        stmt.setString(2, buildUniqueKey(template));
        stmt.addBatch();
      }
      stmt.executeBatch();
      conn.commit();
    } catch (Exception e) {
      rollbackQuietly(conn);
      throw new DatabaseException("Failed to delete entities", e);
    } finally {
      closeQuietly(stmt);
    }
  }

  @Override
  public void deleteAll(@NotNull Class<? extends HasUniqueProperties> clazz) {
    Connection conn = getConnection();
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement("DELETE FROM documents WHERE entity_type = ?");
      stmt.setString(1, clazz.getSimpleName());
      int deleted = stmt.executeUpdate();
      log.debug("Deleted {} records of type {}", deleted, clazz.getSimpleName());
    } catch (Exception e) {
      throw new DatabaseException("Failed to delete all entities of type " + clazz.getSimpleName(), e);
    } finally {
      closeQuietly(stmt);
      closeQuietly(conn);
    }
  }

  @Override
  public void createUniqueIndex(@NotNull Class<? extends HasUniqueProperties> clazz) {
    log.debug("Index creation requested for {} - using default unique constraint", clazz.getSimpleName());
  }

  @Override
  public void createIndex(Class<?> clazz, IndexType indexType, FieldType type, @NotNull String... propertyPath) {
    log.debug("Index creation requested for {} on {} - skipping for document store",
        clazz.getSimpleName(), Arrays.toString(propertyPath));
  }

  @Override
  public void exportDatabase(File target) throws IOException {
    Connection conn = getConnection();
    try {
      doExport(conn, target);
    } finally {
      closeQuietly(conn);
    }
  }

  private void doExport(Connection conn, File target) throws IOException {
    Statement stmt = null;
    ResultSet rs = null;
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(target));
      stmt = conn.createStatement();
      rs = stmt.executeQuery("SELECT * FROM documents");
      JsonArray array = new JsonArray();
      while (rs.next()) {
        JsonObject obj = new JsonObject();
        obj.addProperty("entity_type", rs.getString("entity_type"));
        obj.addProperty("unique_key", rs.getString("unique_key"));
        obj.add("document", JsonParser.parseString(rs.getString("document")));
        obj.addProperty("created", rs.getTimestamp("created").getTime());
        obj.addProperty("updated", rs.getTimestamp("updated").getTime());
        array.add(obj);
      }
      writer.write(gson.toJson(array));
      log.info("Exported {} records to {}", array.size(), target.getAbsolutePath());
    } catch (Exception e) {
      throw new IOException("Failed to export database", e);
    } finally {
      closeQuietly(rs);
      closeQuietly(stmt);
      closeQuietly(writer);
    }
  }

  @Override
  public void importDatabase(File source) throws IOException {
    Connection conn = getConnection();
    try {
      doImport(conn, source);
    } finally {
      closeQuietly(conn);
    }
  }

  private void doImport(Connection conn, File source) throws IOException {
    PreparedStatement stmt = null;
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(source));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }

      JsonArray array = JsonParser.parseString(sb.toString()).getAsJsonArray();
      conn.setAutoCommit(false);
      stmt = conn.prepareStatement(
          "MERGE INTO documents (entity_type, unique_key, document, created, updated) " +
              "KEY (entity_type, unique_key) VALUES (?, ?, ?, ?, ?)");

      for (JsonElement elem : array) {
        JsonObject obj = elem.getAsJsonObject();
        stmt.setString(1, obj.get("entity_type").getAsString());
        stmt.setString(2, obj.get("unique_key").getAsString());
        stmt.setString(3, obj.get("document").toString());
        stmt.setTimestamp(4, new Timestamp(obj.get("created").getAsLong()));
        stmt.setTimestamp(5, new Timestamp(obj.get("updated").getAsLong()));
        stmt.addBatch();
      }
      stmt.executeBatch();
      conn.commit();
      log.info("Imported {} records from {}", array.size(), source.getAbsolutePath());
    } catch (Exception e) {
      rollbackQuietly(conn);
      throw new IOException("Failed to import database", e);
    } finally {
      closeQuietly(stmt);
      closeQuietly(reader);
    }
  }

  @Override
  public void dropDatabase() {
    Connection conn = getConnection();
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      stmt.execute("DROP ALL OBJECTS");
      log.info("Dropped all database objects");
    } catch (Exception e) {
      throw new DatabaseException("Failed to drop database", e);
    } finally {
      closeQuietly(stmt);
      closeQuietly(conn);
    }
  }

  @Override
  public String toJson(Object object) {
    return gson.toJson(object);
  }

  @Override
  public <T> T fromJson(String json, Class<T> classOfT) {
    return gson.fromJson(json, classOfT);
  }

  private String buildUniqueKey(HasUniqueProperties entity) {
    List<String> props = entity.getUniqueProperties();
    List<Object> values = entity.getUniqueValues();
    StringBuilder key = new StringBuilder();
    for (int i = 0; i < props.size(); i++) {
      if (i > 0) key.append("|");
      key.append(props.get(i)).append("=").append(values.get(i));
    }
    return key.toString();
  }

  private void closeQuietly(AutoCloseable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        log.debug("Error closing resource", e);
      }
    }
  }

  private void rollbackQuietly(Connection conn) {
    if (conn != null) {
      try {
        conn.rollback();
      } catch (Exception e) {
        log.debug("Error rolling back transaction", e);
      }
    }
  }

  private static class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private static class DateDeserializer implements JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement jsonElement, Type typeOF, JsonDeserializationContext context)
        throws JsonParseException {
      try {
        return new SimpleDateFormat(DATE_PATTERN).parse(jsonElement.getAsString());
      } catch (ParseException e) {
        // Ignore, try as long
      }
      try {
        return new Date(jsonElement.getAsLong());
      } catch (NumberFormatException e) {
        throw new JsonParseException("Unparseable date: " + jsonElement.getAsString());
      }
    }
  }
}
