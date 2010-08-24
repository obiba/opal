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

import static com.google.gwt.query.client.GQuery.$$;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.query.client.Properties;

/**
 *
 */
public class JqPlotHistogram extends JqPlot {

  private final JsArray<JsArrayNumber> data = JsArray.createArray().cast();

  private double min;

  private double max;

  private double binSize;

  public JqPlotHistogram(double min, double max) {
    this.min = min;
    this.max = max;
  }

  public void push(double lower, double upper, double freq) {
    binSize = upper - lower;
    min = Math.min(min, lower);
    max = Math.max(max, upper);
    JsArrayNumber series = JsArrayNumber.createArray().cast();
    series.push(upper - binSize / 2d);
    series.push(freq);
    data.push(series);
  }

  public void plot(String id) {
    JsArray<JsArray<JsArrayNumber>> plotData = JsArray.createArray().cast();
    plotData.push(data);
    Properties p = $$("{" + //
    "  title:'Histogram'," + //
    "  seriesDefaults:{renderer:$wnd.jQuery.jqplot.BarRenderer,rendererOptions:{barMargin:35}}," + //
    "  axes:{" + //
    "    xaxis:{" + //
    // "         min:" + (min) + ",max:" + (max) + //
    "      ticks:" + stringify(makeTicks()) + //
    "    }," + //
    "    yaxis:{" + //
    "      min:0," + // 
    "      tickOptions:{formatString:'%d'} " + //
    "    }" + //
    "  } " + //
    "}");

    plot(id, plotData, p);

  }

  JsArrayNumber makeTicks() {
    JsArrayNumber ticks = JsArray.createArray().cast();
    for(double tick = min - binSize; tick <= max + binSize; tick += binSize) {
      ticks.push(tick);
    }
    return ticks;
  }

  public static native String stringify(JavaScriptObject obj)
  /*-{
    return $wnd.JSON.stringify(obj);
  }-*/;

}
