/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.ohs.client;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.query.client.Properties;

public abstract class JqPlot {

  private final String id;

  private JavaScriptObject plot;

  public JqPlot(String id) {
    this.id = id;
  }

  protected void plot(JsArray data, Properties options) {
    $('#' + id).children().remove();
    innerplot(data, options);
  }

  public native final void redraw()
  /*-{
    this.@org.obiba.opal.web.gwt.ohs.client.JqPlot::plot.redraw();
  }-*/;

  public native final void innerplot(JsArray data, Properties options)
  /*-{
     this.@org.obiba.opal.web.gwt.ohs.client.JqPlot::plot = $wnd.jQuery.jqplot(this.@org.obiba.opal.web.gwt.ohs.client.JqPlot::id, data, options);
   }-*/;
}
