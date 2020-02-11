/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource.impl;

import com.google.common.base.Strings;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.json.JSONObject;
import org.obiba.opal.spi.resource.Resource;
import org.obiba.opal.spi.resource.ResourceFactory;
import org.obiba.opal.spi.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;


/**
 * This default implementation of a {@link Resource} factory expects a folder that would contain: a properties file containg
 * the settings (name, title and description), the JSON schema-form file for the parameters, the JSON schema-form file for the
 * credentials and the javascript file that converts the forms data into a resource JSON object (with name, url, format, credentials fields).
 */
public class DefaultResourceFactory implements ResourceFactory {

  private static final Logger log = LoggerFactory.getLogger(DefaultResourceFactory.class);

  private static final String SETTINGS_FILENAME = "settings.properties";

  private static final String PARAMS_FORM_FILENAME = "parameters-form.json";

  private static final String CREDENTIALS_FORM_FILENAME = "credentials-form.json";

  private static final String TORESOURCE_FUNCTION_FILENAME = "toResource.js";

  private static final String REQUIRE_FUNCTION_FILENAME = "require.js";

  private static final String IDENTITY_KEY = "identity";

  private static final String SECRET_KEY = "secret";

  private final File settingsFolder;

  private String name;

  private String title;

  private String description;

  private String group;

  private JSONObject parametersSchemaForm;

  private JSONObject credentialsSchemaForm;

  private File toResourceFile;

  private File requireFile;

  public DefaultResourceFactory(File settingsFolder) {
    this.settingsFolder = settingsFolder;
    initializeProperties();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  @Override
  public JSONObject getParametersSchemaForm() {
    return parametersSchemaForm;
  }

  public void setParametersSchemaForm(JSONObject parametersSchemaForm) {
    this.parametersSchemaForm = parametersSchemaForm;
  }

  @Override
  public JSONObject getCredentialsSchemaForm() {
    return credentialsSchemaForm;
  }

  public void setCredentialsSchemaForm(JSONObject credentialsSchemaForm) {
    this.credentialsSchemaForm = credentialsSchemaForm;
  }

  @Override
  public Resource createResource(String name, JSONObject parameters, JSONObject credentials) {
    try {
      ScriptEngineManager manager = new ScriptEngineManager();
      ScriptEngine engine = manager.getEngineByName("JavaScript");
      engine.eval(getToResourceScriptReader());
      Invocable inv = (Invocable) engine;
      ScriptObjectMirror rval = (ScriptObjectMirror) inv.invokeFunction("toResource", name,
          JSONUtils.toMap(parameters), JSONUtils.toMap(credentials));
      JSONObject resource = new JSONObject(rval);
      log.trace("resource = {}", resource.toString(2));

      // credentials
      String identity = resource.optString(getIdentityKey(), null);
      String secret = resource.optString(getSecretKey(), null);

      return DefaultResource.newResource(name)
          .uri(resource.getString("url"))
          .format(resource.optString("format", null))
          .credentials(identity, secret).build();
    } catch (Exception e) {
      log.error("Unable to create Resource object", e);
    }
    return null;
  }

  @Override
  public String getRequiredPackage(String name, JSONObject parameters, JSONObject credentials) {
    try {
      String require = null;
      Reader requireReader = getRequireScriptReader();
      if (requireReader == null) {
        if (parameters.has("_package"))
          require = parameters.getString("_package");
      } else {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        engine.eval(requireReader);
        Invocable inv = (Invocable) engine;
        Object rval = inv.invokeFunction("require", name, JSONUtils.toMap(parameters), JSONUtils.toMap(credentials));
        require = rval == null ? null : rval.toString();
      }
      log.trace("require = {}", require);
      return require;
    } catch (Exception e) {
      log.error("Unable to extract required Resource package", e);
    }
    return null;
  }

  protected Reader getToResourceScriptReader() throws IOException {
    return Files.newBufferedReader(toResourceFile.toPath(), StandardCharsets.UTF_8);
  }

  protected String getToResourceFunctionFilename() {
    return TORESOURCE_FUNCTION_FILENAME;
  }

  protected Reader getRequireScriptReader() throws IOException {
    if (!requireFile.exists()) return null;
    return Files.newBufferedReader(requireFile.toPath(), StandardCharsets.UTF_8);
  }

  protected String getRequireFunctionFilename() {
    return REQUIRE_FUNCTION_FILENAME;
  }

  protected String getIdentityKey() {
    return IDENTITY_KEY;
  }

  protected String getSecretKey() {
    return SECRET_KEY;
  }

  private void initializeProperties() {
    File settingsFile = new File(settingsFolder, SETTINGS_FILENAME);
    String defaultName = settingsFolder.getName();
    if (settingsFile.exists()) {
      try (FileInputStream in = new FileInputStream(settingsFile)) {
        Properties prop = new Properties();
        prop.load(in);
        this.name = prop.getProperty("name", defaultName);
        this.title = prop.getProperty("title", this.name);
        this.description = prop.getProperty("description");
        this.group = prop.getProperty("group");
      } catch (Exception e) {
        log.warn("Failed reading plugin properties: {}", settingsFile.getAbsolutePath(), e);
      }
    } else {
      this.name = defaultName;
      this.title = this.name;
      this.description = null;
      this.group = null;
    }
    this.toResourceFile = new File(settingsFolder, getToResourceFunctionFilename());
    this.requireFile = new File(settingsFolder, getRequireFunctionFilename());
    this.parametersSchemaForm = readSchemaForm(new File(settingsFolder, PARAMS_FORM_FILENAME));
    this.credentialsSchemaForm = readSchemaForm(new File(settingsFolder, CREDENTIALS_FORM_FILENAME));
  }

  private JSONObject readSchemaForm(File formFile) {
    log.debug("Reading schema-form from: {}", formFile.getAbsolutePath());
    String schemaForm = null;
    if (formFile.exists()) {
      try {
        schemaForm = Files.lines(formFile.toPath()).reduce("", String::concat).trim();
      } catch (IOException e) {
        log.error("Cannot read schema-form from: {}", formFile.getAbsolutePath(), e);
      }
    }
    return new JSONObject(Strings.isNullOrEmpty(schemaForm) ? "{}" : schemaForm);
  }
}
