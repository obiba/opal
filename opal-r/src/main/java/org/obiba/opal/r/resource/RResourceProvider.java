/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.resource;

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

  private JSONObject settings;

  private ScriptEngine engine;

  RResourceProvider(String name, String script) {
    this.name = name;
    try {
      ScriptEngineManager manager = new ScriptEngineManager(null);
      this.engine = manager.getEngineByName("nashorn");
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
    return getSettings().optString("title");
  }

  @Override
  public String getDescription() {
    return getSettings().optString("description");
  }

  @Override
  public String getWeb() {
    return getSettings().optString("web");
  }

  @Override
  public List<ResourceProvidersService.Category> getCategories() {
    JSONArray categories = getSettings().optJSONArray("categories");
    List<ResourceProvidersService.Category> rCats = Lists.newArrayList();
    if (categories != null) {
      for (int i = 0; i < categories.length(); i++) {
        rCats.add(new RResourceCategory(categories.getJSONObject(i)));
      }
    }
    return rCats;
  }

  @Override
  public ResourceProvidersService.Category getCategory(String name) {
    if (Strings.isNullOrEmpty(name)) return null;
    JSONArray categories = getSettings().optJSONArray("categories");
    if (categories != null) {
      for (int i = 0; i < categories.length(); i++) {
        if (name.equals(categories.getJSONObject(i).optString("name")))
          return new RResourceCategory(categories.getJSONObject(i));
      }
    }
    return null;
  }

  @Override
  public List<ResourceProvidersService.ResourceFactory> getFactories() {
    JSONArray types = getSettings().optJSONArray("types");
    List<ResourceProvidersService.ResourceFactory> factories = Lists.newArrayList();
    if (types != null) {
      for (int i = 0; i < types.length(); i++) {
        factories.add(new RResourceFactory(name, types.getJSONObject(i), engine));
      }
    }
    return factories;
  }

  public JSONObject getSettings() {
    if (settings == null) {
      if (engine != null) {
        String varName = name.replaceAll("\\.", "_");
        try {
          this.settings = new JSONObject(engine.eval(String.format("JSON.stringify(%s.settings)", varName)).toString());
          log.trace("{} settings = {}", name, settings.toString(2));
        } catch (ScriptException e) {
          log.error("Unable to get resource settings from package: {}", name, e);
          this.settings = new JSONObject();
        }
      }
    }
    return settings;
  }
}
