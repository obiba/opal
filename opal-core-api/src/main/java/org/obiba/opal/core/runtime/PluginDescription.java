/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.runtime;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.runtime.Version;

import java.util.Objects;

/**
 * Plugin description as returned by a plugin repository.
 */
public class PluginDescription {

  private final String name;

  private final String type;

  private final String title;

  private final String description;

  private final Version version;

  private final Version opalVersion;

  private final String file;

  private final String repo;

  public PluginDescription(JSONObject pluginObject, String repo) throws JSONException {
    this.name = pluginObject.getString("name");
    this.type = pluginObject.getString("type");
    this.title = pluginObject.getString("title");
    this.description = pluginObject.getString("description");
    this.version = new Version(pluginObject.getString("version"));
    this.opalVersion = new Version(pluginObject.getString("opalVersion"));
    this.file = pluginObject.getString("file");
    this.repo = repo;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Version getVersion() {
    return version;
  }

  public Version getOpalVersion() {
    return opalVersion;
  }

  public String getFile() {
    return file;
  }

  public String getRepo() {
    return repo;
  }

  public String getFileLocation() {
    return repo + "/" + file;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PluginDescription that = (PluginDescription) o;
    return Objects.equals(type, that.type) && Objects.equals(name, that.name) && Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(file);
  }
}
