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

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;

public abstract class JqPlot {

  private static final String JQPLOT_INSTANCE = "jqplot_instance";

  private JavaScriptObject plot;

  public JqPlot() {
  }

  public abstract void plot(String id);

  protected void plot(String id, JsArray data, Properties options) {
    innerplot(id, data, options);
    // Save this instance as there's no other way to get it back using jqplot
    GQuery.$('#' + id).data(JQPLOT_INSTANCE, this);
  }

  public static native String stringify(JavaScriptObject obj)
  /*-{
    return $wnd.JSON.stringify(obj);
  }-*/;

  public static final void redraw(GQuery plots) {
    plots.each(new Function() {
      @Override
      public void f(Element e) {
        GQuery.$(e).data(JQPLOT_INSTANCE, JqPlot.class).redraw();
      }
    });
  }

  public native final void redraw()
  /*-{
    this.@org.obiba.opal.web.gwt.ohs.client.JqPlot::plot.redraw();
  }-*/;

  public native final void innerplot(String id, JsArray data, Properties options)
  /*-{
     this.@org.obiba.opal.web.gwt.ohs.client.JqPlot::plot = $wnd.jQuery.jqplot(id, data, options);
   }-*/;
}
