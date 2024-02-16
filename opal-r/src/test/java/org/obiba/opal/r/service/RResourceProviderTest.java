/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import org.json.JSONObject;
import org.junit.Test;

import jakarta.script.Bindings;
import jakarta.script.ScriptEngine;
import jakarta.script.ScriptEngineManager;
import jakarta.script.ScriptException;

public class RResourceProviderTest {

  @Test
  public void jsEngineTest() throws ScriptException {
    ScriptEngine engine = new ScriptEngineManager(null).getEngineByName("nashorn"); //Creates a ScriptEngine
    engine.eval("var pkg = { settings: { 'title': 'Title', 'types': [ { 'name': 'aaa' }, { 'name': 'bbb' } ] }, asResource: function(url, format) { return { 'url': url, 'format': format }; } }");
    Bindings pkgObj = (Bindings) engine.get("pkg");
    JSONObject pkg = new JSONObject(pkgObj);
    System.out.println(pkg.toString(2));
    JSONObject settings = new JSONObject(engine.eval("JSON.stringify(pkg.settings)").toString());
    System.out.println(settings.toString(2));
    JSONObject resource = new JSONObject(engine.eval("JSON.stringify(pkg.asResource('http://toto.example.org', 'csv'))").toString());
    System.out.println(resource.toString(2));
  }
}
