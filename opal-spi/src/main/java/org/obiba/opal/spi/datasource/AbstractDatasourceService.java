package org.obiba.opal.spi.datasource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.opal.spi.support.OpalFileSystemPathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDatasourceService implements DatasourceService {

  public static final Logger log = LoggerFactory.getLogger(AbstractDatasourceService.class);

  public static final String SCHEMA_FILE_EXT = ".json";

  public static final String DEFAULT_PROPERTY_KEY_FORMAT = "usage.%s.";

  protected Properties properties;

  protected boolean running;

  protected Collection<DatasourceUsage> usages;

  protected OpalFileSystemPathResolver pathResolver;

  public Set<DatasourceUsage> initUsages() {
    String usagesString = getProperties().getProperty("usages", "").trim();
    return usagesString.isEmpty()
        ? new HashSet<>()
        : Stream.of(usagesString.split(",")).map(usage -> DatasourceUsage.valueOf(usage.trim().toUpperCase()))
            .collect(Collectors.toSet());
  }

  @Override
  public Properties getProperties() {
    return properties;
  }

  @Override
  public void configure(Properties properties) {
    this.properties = properties;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    usages = initUsages();
    running = true;
  }

  @Override
  public void stop() {
    running = false;
  }

  @Override
  public Collection<DatasourceUsage> getUsages() {
    return usages;
  }

  @Override
  public JSONObject getJSONSchemaForm(@NotNull DatasourceUsage usage) {
    JSONObject jsonObject = new JSONObject();

    try {
      jsonObject = processDefaultPropertiesValue(usage, new JSONObject(readUsageSchema(usage)));
    } catch(IOException e) {
      log.error("Error reading usage jsonSchema: %s", e.getMessage());
    }

    return jsonObject;
  }

  @Override
  public void setOpalFileSystemPathResolver(OpalFileSystemPathResolver resolver) {
    pathResolver = resolver;
  }

  public File resolvePath(String path) {
    return pathResolver == null ? new File(path) : pathResolver.resolve(path);
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

    if(hasUsage(usage) && usageSchemaPath.toFile().exists()) {
      String schema = Files.lines(usageSchemaPath).reduce("", String::concat).trim();
      if(!schema.isEmpty()) result = schema;
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
    if(defaultValue != null && !defaultValue.isEmpty()) {
      log.info("setting default value \"{}\" for schema \"{}\"", defaultValue, schemaName);

      String rootSchemaType = jsonObject.optString("type");

      if("object".equals(rootSchemaType)) {
        JSONObject properties = jsonObject.optJSONObject("properties");

        if(properties != null) {
          JSONObject schema = properties.optJSONObject(schemaName);
          setDefault(schema, defaultValue);
        }
      } else if ("array".equals(rootSchemaType)) {
        JSONArray items = jsonObject.optJSONArray("items");

        if (items != null) {
          IntStream.range(0, items.length()).filter(index -> {
            JSONObject schema = items.optJSONObject(index);
            return schema != null && schemaName.equals(schema.optString("key"));
          }).forEach(index -> setDefault(items.optJSONObject(index), defaultValue));
        }
      }
    }
  }

  private void setDefault(JSONObject schema, String value) {
    if(schema != null) {
      String type = schema.getString("type");
      if("integer".equals(type) || "number".equals(type)) {
        schema.put("default", Double.valueOf(value));
      } else {
        schema.put("default", value);
      }
    }
  }
}
