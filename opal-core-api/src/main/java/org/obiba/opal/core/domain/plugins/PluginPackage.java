/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain.plugins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.obiba.runtime.Version;

/**
 * Plugin description that is get from the update site.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginPackage {
  private final String name;
  private final String type;
  private final String title;
  private final String description;
  private final Version version;
  private final Version opalVersion;
  private final String fileName;

  public PluginPackage(@JsonProperty("name") String name,
                       @JsonProperty("type") String type,
                       @JsonProperty("title") String title,
                       @JsonProperty("description") String description,
                       @JsonProperty("version") String version,
                       @JsonProperty("opalVersion") String opalVersion,
                       @JsonProperty("file") String fileName) {
    this.name = name;
    this.type = type;
    this.title = title;
    this.description = description;
    this.version = new Version(version);
    this.opalVersion = new Version(opalVersion);
    this.fileName = fileName;
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

  public String getFileName() {
    return fileName;
  }

  public boolean isSameAs(String name) {
    return this.name.equals(name);
  }

  public boolean isSameAs(String name, Version version) {
    return this.name.equals(name) && this.version.equals(version);
  }

  public boolean isNewerThan(String name, Version version) {
    return this.name.equals(name) && this.version.compareTo(version)>0;
  }
}
