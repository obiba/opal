/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.opal.core.service.ResourceProvidersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

class RResourceProvider implements ResourceProvidersService.ResourceProvider {

  private static final Logger log = LoggerFactory.getLogger(RResourceProvider.class);

  private final String name;

  private final JSONObject formsConfig;

  private final String script;

  private ScriptEngine engine;

  RResourceProvider(String name, JSONObject formsConfig, String script) {
    this.name = name;
    this.formsConfig = formsConfig;
    this.script = script;
    try {
      ScriptEngineManager manager = new ScriptEngineManager();
      this.engine = manager.getEngineByName("JavaScript");
      engine.eval(script);
    } catch (ScriptException e) {
      log.error("Resource javascript evaluation failed for package {}", name, e);
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getTitle() {
    return formsConfig.optString("title");
  }

  @Override
  public String getDescription() {
    return formsConfig.optString("description");
  }

  @Override
  public List<ResourceProvidersService.Tag> getTags() {
    JSONArray tags = formsConfig.optJSONArray("tags");
    List<ResourceProvidersService.Tag> rTags = Lists.newArrayList();
    if (tags != null) {
      for (int i = 0; i < tags.length(); i++) {
        rTags.add(new RTag(tags.getJSONObject(i)));
      }
    }
    return rTags;
  }

  @Override
  public ResourceProvidersService.Tag getTag(String name) {
    if (Strings.isNullOrEmpty(name)) return null;
    JSONArray tags = formsConfig.optJSONArray("tags");
    if (tags != null) {
      for (int i = 0; i < tags.length(); i++) {
        if (name.equals(tags.getJSONObject(i).optString("name")))
          return new RTag(tags.getJSONObject(i));
      }
    }
    return null;
  }

  @Override
  public List<ResourceProvidersService.ResourceFactory> getFactories() {
    JSONArray forms = formsConfig.optJSONArray("forms");
    List<ResourceProvidersService.ResourceFactory> factories = Lists.newArrayList();
    if (forms != null) {
      for (int i = 0; i < forms.length(); i++) {
        factories.add(new RResourceFactory(name, forms.getJSONObject(i), engine));
      }
    }
    return factories;
  }
}
