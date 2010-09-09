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
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;

/**
 * Frequency plot
 */
public class FrequencyPlot extends JqPlot {

  private final JsArrayNumber data = JsArray.createArray().cast();

  private final JsArrayString labels = JsArrayString.createArray().cast();

  public FrequencyPlot(String id) {
    super(id);
  }

  public void push(String category, double value, double pct) {
    labels.push(category);
    // TODO: Make another yaxis to display the actual number of observations
    data.push(pct);
  }

  public void plot() {
    JsArray<JsArrayNumber> plotData = JsArray.createArray().cast();
    plotData.push(this.data);
    JavaScriptObject p = JsonUtils.unsafeEval("{" + //
    "  title:'Frequency Plot'," + //
    "  seriesDefaults:{renderer:$wnd.jQuery.jqplot.BarRenderer,rendererOptions:{varyBarColor: true},pointLabels:{show:true,hideZeros:true,ypadding:3,edgeTolerance:-5}}," + //
    "  axes:{" + //
    "    xaxis:{" + //
    "      tickRenderer: $wnd.jQuery.jqplot.CanvasAxisTickRenderer," + //
    "      tickOptions: {angle: -30}," + //
    "      renderer:$wnd.jQuery.jqplot.CategoryAxisRenderer," + // 
    "      ticks:" + stringify(labels) + //
    "    }," + //
    "    yaxis:{" + //
    "      min:0,max:100,pad:0," + // 
    "      tickOptions:{formatString:'%d%%'} " + //
    "    }" + //
    "  } " + //
    "}");
    plot(plotData, p);
  }

}
