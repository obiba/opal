/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.ace.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public class AceEntryPoint implements EntryPoint {

  /**
   * ClientBundle for JavaScripts.
   */
  public interface Scripts extends ClientBundle {

    Scripts INSTANCE = GWT.create(Scripts.class);

    @Source("js/min-noconflict/ace.js")
    TextResource scriptAce();

    @Source("js/min-noconflict/theme-textmate.js")
    TextResource scriptThemeTextMate();

    @Source("js/min-noconflict/mode-text.js")
    TextResource scriptModeText();

    @Source("js/min-noconflict/mode-markdown.js")
    TextResource scriptModeMarkdown();

    @Source("js/min-noconflict/mode-javascript.js")
    TextResource scriptModeJavascript();

    @Source("js/min-noconflict/worker-javascript.js")
    TextResource scriptWorkerJavascript();

    @Source("js/beautify.js")
    TextResource scriptBeautify();
  }

  @Override
  public void onModuleLoad() {
    ScriptInjector.fromString(Scripts.INSTANCE.scriptAce().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
    ScriptInjector.fromString(Scripts.INSTANCE.scriptThemeTextMate().getText()).setWindow(ScriptInjector.TOP_WINDOW)
        .inject();
    ScriptInjector.fromString(Scripts.INSTANCE.scriptModeText().getText()).setWindow(ScriptInjector.TOP_WINDOW)
        .inject();
    ScriptInjector.fromString(Scripts.INSTANCE.scriptModeMarkdown().getText()).setWindow(ScriptInjector.TOP_WINDOW)
        .inject();
    ScriptInjector.fromString(Scripts.INSTANCE.scriptModeJavascript().getText()).setWindow(ScriptInjector.TOP_WINDOW)
        .inject();
    ScriptInjector.fromString(Scripts.INSTANCE.scriptWorkerJavascript().getText()).setWindow(ScriptInjector.TOP_WINDOW)
        .inject();
    ScriptInjector.fromString(Scripts.INSTANCE.scriptBeautify().getText()).setWindow(ScriptInjector.TOP_WINDOW)
        .inject();
  }

}
