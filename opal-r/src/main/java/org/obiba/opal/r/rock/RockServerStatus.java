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

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "running",
    "rSessionsCounts"
})
public class RockServerStatus {

  @JsonProperty("running")
  private Boolean running = false;

  @JsonProperty("rSessionsCounts")
  private RockSessionsCounts rSessionsCounts;

  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("running")
  public Boolean getRunning() {
    return running;
  }

  @JsonProperty("running")
  public void setRunning(Boolean running) {
    this.running = running;
  }

  public RockServerStatus withRunning(Boolean running) {
    this.running = running;
    return this;
  }

  @JsonProperty("rSessionsCounts")
  public RockSessionsCounts getRSessionsCounts() {
    return rSessionsCounts;
  }

  @JsonProperty("rSessionsCounts")
  public void setRSessionsCounts(RockSessionsCounts rSessionsCounts) {
    this.rSessionsCounts = rSessionsCounts;
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
