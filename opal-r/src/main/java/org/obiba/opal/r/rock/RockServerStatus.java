
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
