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

public class JqPlotQQ extends JqPlot {

  private final JsArray<JsArrayNumber> data = JsArray.createArray().cast();

  private final double min;

  private final double max;

  public JqPlotQQ(String id, double min, double max) {
    super(id);
    this.min = min;
    this.max = max;
  }

  public void push(JsArrayNumber exp, JsArrayNumber theo) {
    for(int i = 0; i < exp.length(); i++) {
      data.push(point(exp.get(i), theo.get(i)));
    }
  }

  public void plot() {
    JsArray plotData = JsArray.createArray().cast();
    plotData.push(getXequalsY());
    plotData.push(data);
    Properties p = $$("{" + //
    "  title:'Normal Probability Plot'," + //
    "  axesDefaults:{pad:0, min:" + min + ", max:" + max + "}," + //
    "  series:[" + //
    "    {showMarker:false}," + //
    "    {showLine:false, markerOptions:{style:'x'}}" + //
    "  ]" + // 
    "}");

    plot(plotData, p);
  }

  private JsArray<JsArrayNumber> getXequalsY() {
    JsArray<JsArrayNumber> xeqy = JsArray.createArray().cast();
    xeqy.push(point(min, min));
    xeqy.push(point(max, max));
    return xeqy;
  }

  private JsArrayNumber point(double x, double y) {
    JsArrayNumber point = JsArrayNumber.createArray().cast();
    point.push(x);
    point.push(y);
    return point;
  }

  public static native String stringify(JavaScriptObject obj)
  /*-{
    return $wnd.JSON.stringify(obj);
  }-*/;

}
