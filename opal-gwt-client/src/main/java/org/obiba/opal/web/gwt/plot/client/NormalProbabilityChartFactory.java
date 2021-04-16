/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.plot.client;

import java.util.List;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.XAxis;
import org.moxieapps.gwt.highcharts.client.labels.Labels;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.ScatterPlotOptions;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayNumber;

public class NormalProbabilityChartFactory {

  private final List<Point> values = Lists.newArrayList();

  private final List<Point> normal = Lists.newArrayList();

  private final double min;

  private final double max;

  public NormalProbabilityChartFactory(double min, double max) {
    this.min = min;
    this.max = max;
  }

  public void push(JsArrayNumber exp, JsArrayNumber theo) {
    for(int i = 0; i < exp.length(); i++) {
      values.add(new Point(exp.get(i), theo.get(i)));
    }
    normal.add(new Point(min, min));
    normal.add(new Point(max, max));
  }

  public Chart createChart(String title, String xAxis, String yAxis) {
    Chart chart = new Chart().setChartTitleText(title).setLegend(new Legend().setEnabled(false))//
        .setCredits(new Credits().setEnabled(false));

    chart.getXAxis().setType(Axis.Type.LINEAR)//
        .setMin(min)//
        .setMax(max)//
        .setAllowDecimals(true)//
        .setAxisTitle(new AxisTitle().setAlign(AxisTitle.Align.MIDDLE).setText(xAxis))//
        .setTickmarkPlacement(XAxis.TickmarkPlacement.ON)//
        .setLabels(new XAxisLabels()).setLabels(new XAxisLabels()//
        .setRotation(-45)//
        .setAlign(Labels.Align.RIGHT)//
    );

    chart.getYAxis().setType(Axis.Type.LINEAR)//
        .setMin(min)//
        .setMax(max)//
        .setAxisTitle(new AxisTitle().setAlign(AxisTitle.Align.MIDDLE).setText(yAxis))//
        .setAllowDecimals(true);

    // Scatter line
    chart.addSeries(chart.createSeries() //
        .setType(Series.Type.LINE)//
        .setPoints(normal.toArray(new Point[normal.size()]))//
        .setPlotOptions(new LinePlotOptions().setMarker(new Marker().setEnabled(false))//
            .setAnimation(false)//
            .setEnableMouseTracking(false)//
        ));

    // Scatter serie
    chart.addSeries(chart.createSeries() //
        .setType(Series.Type.SCATTER)//
        .setPlotOptions(new ScatterPlotOptions().setMarker(new Marker().setSymbol(Marker.Symbol.CIRCLE))//
            .setAnimation(false)//
            .setEnableMouseTracking(false)//
        )//
        .setPoints(values.toArray(new Point[values.size()]))//
    );

    return chart;
  }
}
