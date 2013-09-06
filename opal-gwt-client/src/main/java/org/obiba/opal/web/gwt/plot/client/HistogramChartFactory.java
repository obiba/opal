/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.plot.client;

import java.util.HashMap;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.XAxis;
import org.moxieapps.gwt.highcharts.client.labels.Labels;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.common.collect.Lists;

public class HistogramChartFactory {

  private final List<Point> values = Lists.newArrayList();

  private HashMap<Number, String> tooltipsData = new HashMap<Number, String>();

  public void push(double density, double lower, double upper) {
    Number n = upper - (upper - lower) / 2;
    tooltipsData.put(n, "Lower: " + lower +
        "<br/>Upper: " + upper + "<br/>Density: " + density);

    values.add(new Point(n, density));
  }

  public Chart createChart(String title, String yaxisTitle) {
    Chart chart = new Chart().setType(Series.Type.AREA_SPLINE).setChartTitleText(title)
        .setAreaPlotOptions(new AreaPlotOptions().setEnableMouseTracking(true)//
            .setLineWidth(0)//
            .setMarker(new Marker().setEnabled(true).setHoverState(new Marker().setEnabled(true)))//
        )//
        .setLegend(new Legend().setEnabled(false))//
        .setCredits(new Credits().setEnabled(false)//
        );

    chart.getXAxis().setType(Axis.Type.LINEAR)//
        .setAllowDecimals(true)//
        .setAxisTitle(new AxisTitle().setAlign(AxisTitle.Align.MIDDLE).setText(""))//
        .setTickmarkPlacement(XAxis.TickmarkPlacement.ON)//
        .setLabels(new XAxisLabels()//
            .setRotation(-45)//
            .setAlign(Labels.Align.RIGHT)//
        );
    chart.setAnimation(false);
    chart.getYAxis().setAxisTitle(new AxisTitle().setText(yaxisTitle).setAlign(AxisTitle.Align.MIDDLE));

    chart.setToolTip(new ToolTip().setEnabled(true).setFormatter(new ToolTipFormatter() {
      public String format(ToolTipData toolTipData) {
        return tooltipsData.get(toolTipData.getPoint().getX());
      }
    }));

    chart.addSeries(chart.createSeries() //
        .setPoints(values.toArray(new Point[values.size()]))//
    );
    return chart;
  }

//  public Chart createFrequencyChart(String title, String yaxisTitle){
//    Chart chart = createChart(title, yaxisTitle);
//    chart.getYAxis().setAllowDecimals(false);
//
//    return chart;
//  }
}
