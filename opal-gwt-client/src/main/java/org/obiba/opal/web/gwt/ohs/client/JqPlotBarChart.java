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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.query.client.Properties;

public class JqPlotBarChart extends JqPlot {

  private final JsArrayNumber data = JsArray.createArray().cast();

  private final JsArrayString labels = JsArrayString.createArray().cast();

  public JqPlotBarChart() {
  }

  public void push(String category, double value, double pct) {
    labels.push(category);
    JsArrayNumber series = JsArrayNumber.createArray().cast();
    series.push(pct);
    // TODO: Make another yaxis to display the actual number of observations
    series.push(value);
    data.push(pct);
  }

  public void plot(String id) {
    JsArray<JsArrayNumber> plotData = JsArray.createArray().cast();
    plotData.push(this.data);
    Properties p = $$("{" + //
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
    plot(id, plotData, p);
  }

}
