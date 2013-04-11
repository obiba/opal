/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.plot.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;

/**
 * Base class for jqPlots
 */
public abstract class JqPlot {

  private final String id;

  private JavaScriptObject plot;

  protected static native String stringify(JavaScriptObject obj)
  /*-{
      return $wnd.JSON.stringify(obj);
  }-*/;

  protected JqPlot(String id) {
    this.id = id;
  }

  public abstract void plot();

  public void plotOrRedraw() {
    if(plot == null) {
      plot();
    } else {
      redraw();
    }
  }

  public native final void redraw()
  /*-{
      this.@org.obiba.opal.web.gwt.plot.client.JqPlot::plot.redraw();
  }-*/;

  /**
   * Creates a [x,y] coordinate. Useful when ploting points.
   *
   * @param x the x value
   * @param y the y value
   * @return an array as [x,y]
   */
  protected native final JsArrayNumber point(double x, double y)
  /*-{
      return [x, y];
  }-*/;

  protected void plot(JsArray<?> data, JavaScriptObject options) {
    plot = innerplot(id, data, options);
  }

  protected native final JavaScriptObject innerplot(String id, JsArray<?> data, JavaScriptObject options)
  /*-{
      return $wnd.jQuery.jqplot(id, data, options);
  }-*/;
}
