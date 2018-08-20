package org.obiba.opal.spi.datasource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDatasourceService implements DatasourceService {

  public static final Logger log = LoggerFactory.getLogger(AbstractDatasourceService.class);

  public static final String SCHEMA_FILE_EXT = ".json";
  public static final String DEFAULT_PROPERTY_KEY_FORMAT = "usage.%s.";

  public Set<DatasourceUsage> initUsages() {
    String usagesString = getProperties().getProperty("usages", "").trim();
    return usagesString.isEmpty() ? new HashSet<>()
        : Stream.of(usagesString.split(",")).map(usage -> DatasourceUsage.valueOf(usage.trim().toUpperCase()))
            .collect(Collectors.toSet());
  }

  public JSONObject processDefaultPropertiesValue(DatasourceUsage usage, JSONObject jsonObject) {
    String format = String.format(DEFAULT_PROPERTY_KEY_FORMAT, usage);
    getProperties().stringPropertyNames().stream().filter(property -> property.startsWith(format)).forEach(
        property -> setDefaultValue(property.replace(format, ""), getProperties().getProperty(property), jsonObject));

    return jsonObject;
  }

  public String readUsageSchema(DatasourceUsage usage) throws IOException {
    Path usageSchemaPath = getUsageSchemaPath(usage).toAbsolutePath();
    String result = "{}";

    log.info("Reading usage jsonSchema at %s", usageSchemaPath);

    if (hasUsage(usage) && usageSchemaPath.toFile().exists()) {
      String schema = Files.lines(usageSchemaPath).reduce("", String::concat).trim();
      if (!schema.isEmpty())
        result = schema;
    }

    return result;
  }

  private Path getUsageSchemaPath(DatasourceUsage usage) {
    return Paths.get(getProperties().getProperty(INSTALL_DIR_PROPERTY), usage.name().toLowerCase() + SCHEMA_FILE_EXT);
  }

  private boolean hasUsage(DatasourceUsage usage) {
    return getUsages().stream().filter(u -> u.equals(usage)).count() == 1;
  }

  private void setDefaultValue(String schemaName, String defaultValue, JSONObject jsonObject) {
    if (defaultValue != null && !defaultValue.isEmpty()) {
      log.info("setting default value \"{}\" for schema \"{}\"", defaultValue, schemaName);

      JSONObject properties = jsonObject.optJSONObject("properties");
      if (properties != null) {
        JSONObject schema = properties.optJSONObject(schemaName);
        if (schema != null) {
          String type = schema.getString("type");
          if ("integer".equals(type) || "number".equals(type)) {
            schema.put("default", Double.valueOf(defaultValue));
          } else if ("boolean".equals(type)) {
            schema.put("default", Boolean.valueOf(defaultValue));
          } else {
            schema.put("default", defaultValue);
          }
        }
      }
    }
  }
}
