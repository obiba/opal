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
import com.google.gwt.core.client.JsonUtils;

/**
 * Plots histograms. Note that jqPlot does not support histograms. This class uses a BarRenderer but not a CategoryAxis.
 * This requires setting the {@code barWidth} which may result in a broken plot.
 * <p/>
 * This could be improved by testing the size of the plot and adjusting the barWidth accordingly.
 */
public class HistogramPlot extends JqPlot {

  private final JsArray<JsArrayNumber> data = JsArray.createArray().cast();

  private double min;

  private double max;

  private double binSize;

  public HistogramPlot(String id, double min, double max) {
    super(id);
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

  public void plot() {
    // Nothing to plot
    if(binSize == 0) return;

    JsArray<JsArray<JsArrayNumber>> plotData = JsArray.createArray().cast();
    plotData.push(data);
    JavaScriptObject p = JsonUtils.unsafeEval("{" + //
        "  title:'Histogram'," + //
        "  seriesDefaults:{renderer:$wnd.jQuery.jqplot.BarRenderer,rendererOptions:{barWidth:25},pointLabels:{show:true,hideZeros:false,ypadding:3,edgeTolerance:-5}}," +
        //
        "  axes:{" + //
        "    xaxis:{" + //
        "      ticks:" + stringify(makeTicks()) + //
        "    }," + //
        "    yaxis:{" + //
        "      min:0 " + //
        "    }" + //
        "  } " + //
        "}");

    plot(plotData, p);

  }

  JsArrayNumber makeTicks() {
    JsArrayNumber ticks = JsArray.createArray().cast();
    for(double tick = min - binSize; tick <= max + binSize; tick += binSize) {
      ticks.push(tick);
    }
    return ticks;
  }
}
