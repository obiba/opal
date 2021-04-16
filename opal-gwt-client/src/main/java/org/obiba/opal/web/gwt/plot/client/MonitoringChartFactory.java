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

import java.util.Date;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.DataLabels;
import org.moxieapps.gwt.highcharts.client.labels.Labels;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaSplinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.SplinePlotOptions;

import com.google.gwt.i18n.client.DateTimeFormat;

public class MonitoringChartFactory {

  private Chart chart;

  private int duration;

  public void createAreaSplineChart(String title, String yTitle, String[] seriesName, int duration) {
    this.duration = duration;
    chart = new Chart().setType(Series.Type.AREA_SPLINE)//
        .setChartTitleText(title)//
        .setAreaSplinePlotOptions(new AreaSplinePlotOptions()//
            .setDataLabels(new DataLabels().setEnabled(false))//
            .setEnableMouseTracking(false)//
            .setMarker(new Marker().setEnabled(false))//
        );

    setDefaultChartOptions(yTitle, seriesName, false);
  }

  public void createSplineChart(String title, String yTitle, String[] seriesName, int duration) {
    this.duration = duration;
    chart = new Chart();

    initSplineChart(title);
    setDefaultChartOptions(yTitle, seriesName, false);
  }

  public void createSplineChart(String title, String yTitle, String yOppositeTitle, String[] seriesName, int duration) {
    this.duration = duration;
    chart = new Chart();

    initSplineChart(title);
    setDefaultChartOptions(yTitle, yOppositeTitle, seriesName);
  }

  public void updateChart(int serie, double x, double y) {
    updateChart(0, serie, x, y);
  }

  public void updateChart(int yAxis, int serie, double x, double y) {
    chart.getXAxis().setExtremes(x - duration, x);
    chart.getSeries()[serie].setYAxis(yAxis).addPoint(x, y, true, false, true);
  }

  public Chart getChart() {
    return chart;
  }

  private void initSplineChart(String title) {
    chart.setType(Series.Type.SPLINE)//
        .setChartTitleText(title)//
        .setSplinePlotOptions(new SplinePlotOptions()//
            .setDataLabels(new DataLabels().setEnabled(false))//
            .setEnableMouseTracking(false)//
            .setMarker(new Marker().setEnabled(false))//
        );
  }

  private void setDefaultChartOptions(String yTitle, String[] seriesName, boolean hasOppositeTitle) {
    chart.setToolTip(new ToolTip().setEnabled(false))//
        .setLegend(new Legend().setEnabled(true))//
        .setCredits(new Credits().setEnabled(false)//
        );

    chart.getYAxis(0).setAxisTitle(new AxisTitle().setText(yTitle).setAlign(AxisTitle.Align.MIDDLE))
        .setAllowDecimals(false);

    chart.getXAxis()//
        .setType(Axis.Type.DATE_TIME)//
        .setLabels(new XAxisLabels()//
            .setRotation(-45)//
            .setAlign(Labels.Align.RIGHT).setFormatter(new AxisLabelsFormatter() {
              @Override
              public String format(AxisLabelsData axisLabelsData) {
                if(axisLabelsData.getValueAsDouble() < 0) {
                  return "";
                }
                return DateTimeFormat.getFormat("mm:ss").format(new Date(axisLabelsData.getValueAsLong()));
              }
            })//
        );

    for(int i = 0; i < seriesName.length; i++) {
      if(hasOppositeTitle) {
        chart.addSeries(chart.createSeries().setName(seriesName[i]).setYAxis(i));
      } else {
        chart.addSeries(chart.createSeries().setName(seriesName[i]));
      }
    }
  }

  private void setDefaultChartOptions(String yTitle, String yOppositeTitle, String[] seriesName) {
    setDefaultChartOptions(yTitle, seriesName, true);

    chart.getYAxis(1).setAxisTitle(new AxisTitle()//
        .setText(yOppositeTitle)//
        .setAlign(AxisTitle.Align.MIDDLE))//
        .setOpposite(true)//
        .setAllowDecimals(false)//
        .setMin(0);
  }
}
