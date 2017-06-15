/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.rql.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public class RQLEntryPoint implements EntryPoint {

  /**
   * ClientBundle for JavaScripts.
   */
  public interface Scripts extends ClientBundle {

    Scripts INSTANCE = GWT.create(Scripts.class);

    @Source("js/rql.min.js")
    TextResource scriptRQL();
  }

  @Override
  public void onModuleLoad() {
    ScriptInjector.fromString(Scripts.INSTANCE.scriptRQL().getText()).setWindow(ScriptInjector.TOP_WINDOW).inject();
  }

}
