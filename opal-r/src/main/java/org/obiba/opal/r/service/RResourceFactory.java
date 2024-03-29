/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import com.google.common.collect.Lists;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.opal.core.service.ResourceProvidersService;
import org.obiba.opal.spi.resource.Resource;
import org.obiba.opal.spi.resource.impl.DefaultResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import java.util.List;

class RResourceFactory implements ResourceProvidersService.ResourceFactory {

  private static final Logger log = LoggerFactory.getLogger(RResourceFactory.class);

  private final String provider;

  private final JSONObject factoryObj;

  private final String script;

  RResourceFactory(String provider, JSONObject factoryObj, String script) {
    this.provider = provider;
    this.factoryObj = factoryObj;
    this.script = script;
  }

  @Override
  public String getProvider() {
    return provider;
  }

  @Override
  public String getName() {
    return factoryObj.optString("name");
  }

  @Override
  public String getTitle() {
    return factoryObj.optString("title");
  }

  @Override
  public String getDescription() {
    return factoryObj.optString("description");
  }

  @Override
  public List<String> getTags() {
    JSONArray tags = factoryObj.optJSONArray("tags");
    List<String> tagNames = Lists.newArrayList();
    if (tags != null) {
      for (int i = 0; i < tags.length(); i++) {
        tagNames.add(tags.optString(i));
      }
    }
    return tagNames;
  }

  @Override
  public JSONObject getParametersSchemaForm() {
    return factoryObj.optJSONObject("parameters");
  }

  @Override
  public JSONObject getCredentialsSchemaForm() {
    return factoryObj.optJSONObject("credentials");
  }

  @Override
  public Resource createResource(String name, JSONObject parameters, JSONObject credentials) {
    try {
      JSONObject resource = new JSONObject();
      try (Context context = Context.create()) {
        context.eval("js", script);
        String varName = provider.replaceAll("\\.", "_");
        Value rsrc = context.eval("js", String.format("JSON.stringify(%s.asResource('%s', '%s', %s, %s))", varName, getName(), name,
            parameters == null ? "undefined" : parameters.toString(),
            credentials == null ? "undefined" : credentials.toString()));
        if (rsrc != null)
          resource = new JSONObject(rsrc.toString());
        log.trace("resource = {}", resource.toString(2));
      }

      // credentials
      String identity = resource.optString("identity", null);
      String secret = resource.optString("secret", null);

      return DefaultResource.newResource(name)
          .uri(resource.optString("url"))
          .format(resource.optString("format", null))
          .credentials(identity, secret).build();
    } catch (Exception e) {
      log.error("Unable to create Resource object", e);
    }
    return null;
  }
}
