/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.plot.client;

import java.util.ArrayList;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.labels.Labels;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.ColumnPlotOptions;

import com.google.common.collect.Lists;

public class HistogramChartFactory {

  private final List<Point> values = Lists.newArrayList();

  private List<String> categories = new ArrayList<String>();

  public void push(double density, double lower, double upper) {
    categories.add(lower + " - " + upper);
    values.add(new Point(density));
  }

  public Chart createChart(String title, String yaxisTitle) {
    Chart chart = new Chart().setType(Series.Type.COLUMN).setChartTitleText(title)
        .setLegend(new Legend().setEnabled(false))//
        .setCredits(new Credits().setEnabled(false))//
        .setColumnPlotOptions(new ColumnPlotOptions().setEnableMouseTracking(false)//
            .setAnimation(false)//
            .setShadow(false)//
            .setPointPadding(0)//
            .setGroupPadding(0)//
        );

    chart.getXAxis()//
        .setCategories(categories.toArray(new String[categories.size()]))//
        .setLabels(new XAxisLabels()//
            .setRotation(-45)//
            .setAlign(Labels.Align.RIGHT)//
        );
    chart.getYAxis().setAxisTitle(new AxisTitle().setText(yaxisTitle).setAlign(AxisTitle.Align.MIDDLE));

    chart.addSeries(chart.createSeries() //
        .setPoints(values.toArray(new Point[values.size()]))//
    );
    return chart;
  }
}
