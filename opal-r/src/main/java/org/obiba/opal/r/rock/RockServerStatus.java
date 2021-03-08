/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "cluster",
    "version",
    "encoding",
    "tags",
    "running",
    "sessions",
    "system"
})
public class RockServerStatus {

  @JsonProperty("id")
  private String id;

  @JsonProperty("cluster")
  private String cluster;

  @JsonProperty("version")
  private String version;

  @JsonProperty("encoding")
  private String encoding;

  @JsonProperty("tags")
  private List<String> tags = new ArrayList<String>();

  @JsonProperty("running")
  private Boolean running = false;

  @JsonProperty("sessions")
  private RockServerSessionsStatus sessions;

  @JsonProperty("system")
  private RockServerSystemStatus system;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("cluster")
  public String getCluster() {
    return cluster;
  }

  @JsonProperty("cluster")
  public void setCluster(String cluster) {
    this.cluster = cluster;
  }

  @JsonProperty("version")
  public String getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(String version) {
    this.version = version;
  }

  @JsonProperty("encoding")
  public String getEncoding() {
    return encoding;
  }

  @JsonProperty("encoding")
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  @JsonProperty("tags")
  public List<String> getTags() {
    return tags;
  }

  @JsonProperty("tags")
  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  @JsonProperty("running")
  public Boolean getRunning() {
    return running;
  }

  @JsonProperty("running")
  public void setRunning(Boolean running) {
    this.running = running;
  }

  @JsonProperty("sessions")
  public RockServerSessionsStatus getSessions() {
    return sessions;
  }

  @JsonProperty("system")
  public RockServerSystemStatus getSystem() {
    return system;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
