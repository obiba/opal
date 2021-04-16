/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDatasourceService implements DatasourceService {

  public static final Logger log = LoggerFactory.getLogger(AbstractDatasourceService.class);

  protected Properties properties;

  protected boolean running;

  private Collection<DatasourceUsage> usages;

  private DatasourceGroup group;

  private OpalFileSystemPathResolver pathResolver;

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
    group = initGroup();
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
  public DatasourceGroup getGroup() {
    return group;
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

  protected JSONObject processDefaultPropertiesValue(DatasourceUsage usage, JSONObject jsonObject) {
    String format = String.format(DEFAULT_PROPERTY_KEY_FORMAT, usage);

    // general property definition (usage independent)
    getProperties().stringPropertyNames().stream().filter(property -> !property.startsWith(format)).forEach(
        property -> setDefaultValue(property, getProperties().getProperty(property), jsonObject));

    // specific property definition (usage dependent)
    getProperties().stringPropertyNames().stream().filter(property -> property.startsWith(format)).forEach(
        property -> setDefaultValue(property.replace(format, ""), getProperties().getProperty(property), jsonObject));

    return jsonObject;
  }

  protected String readUsageSchema(DatasourceUsage usage) throws IOException {
    Path usageSchemaPath = getUsageSchemaPath(usage).toAbsolutePath();
    String result = "{}";

    log.debug("Reading usage jsonSchema at %s", usageSchemaPath);

    if(hasUsage(usage) && usageSchemaPath.toFile().exists()) {
      String schema = Files.lines(usageSchemaPath).reduce("", String::concat).trim();
      if(!schema.isEmpty()) result = schema;
    }

    return result;
  }

  protected File resolvePath(String virtualPath) {
    return pathResolver == null ? new File(virtualPath) : pathResolver.resolve(virtualPath);
  }

  protected Set<DatasourceUsage> initUsages() {
    String usagesString = getProperties().getProperty("usages", "").trim();
    return usagesString.isEmpty()
        ? new HashSet<>()
        : Stream.of(usagesString.split(",")).map(usage -> DatasourceUsage.valueOf(usage.trim().toUpperCase()))
        .collect(Collectors.toSet());
  }

  protected DatasourceGroup initGroup() {
    String groupStr = getProperties().getProperty("group", "").trim();
    return groupStr.isEmpty() ? DatasourceGroup.FILE : DatasourceGroup.valueOf(groupStr.toUpperCase());
  }

  private Path getUsageSchemaPath(DatasourceUsage usage) {
    return Paths.get(getProperties().getProperty(INSTALL_DIR_PROPERTY), usage.name().toLowerCase() + SCHEMA_FILE_EXT);
  }

  private boolean hasUsage(DatasourceUsage usage) {
    return getUsages().stream().filter(u -> u.equals(usage)).count() == 1;
  }

  private void setDefaultValue(String schemaName, String defaultValue, JSONObject jsonObject) {
    if(defaultValue != null && !defaultValue.isEmpty()) {
      log.debug("setting default value \"{}\" for schema \"{}\"", defaultValue, schemaName);

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
