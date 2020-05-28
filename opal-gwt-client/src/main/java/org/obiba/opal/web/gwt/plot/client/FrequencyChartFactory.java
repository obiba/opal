/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.plot.client;

import java.util.List;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.labels.DataLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.BarPlotOptions;

import com.google.common.collect.Lists;

public class FrequencyChartFactory {

  private final List<Number> values = Lists.newArrayList();

  private final List<Number> percentages = Lists.newArrayList();

  private final List<String> categories = Lists.newArrayList();

  public void push(String category, double value, double pct) {
    if(value > 0) {
      categories.add(category);
      values.add(value);
      percentages.add(pct);
    }
  }

  private Chart createChart(String title, String yaxisTitle) {
    Chart chart = new Chart().setType(Series.Type.BAR).setChartTitleText(title)
        .setBarPlotOptions(new BarPlotOptions().setAnimation(false).setDataLabels(new DataLabels().setEnabled(true)))
        .setLegend(new Legend().setEnabled(false)).setCredits(new Credits().setEnabled(false))
        .setHeight(categories.size() * 30 + 100);

    chart.getXAxis().setCategories(categories.toArray(new String[categories.size()]));

    chart.getYAxis().setAxisTitle(new AxisTitle().setText(yaxisTitle).setAlign(AxisTitle.Align.HIGH));

    chart.setAnimation(false);

    return chart;
  }

  public Chart createValueChart(String title) {
    Chart chart = createChart(title, "Count");
    if(!values.isEmpty())
      chart.addSeries(chart.createSeries().setName("Count").setPoints(values.toArray(new Number[values.size()]))

      );
    return chart;
  }

  public Chart createPercentageChart(String title) {
    Chart chart = createChart(title, "%");
    if(!values.isEmpty()) {
      chart.addSeries(chart.createSeries().setName("%").setPoints(percentages.toArray(new Number[percentages.size()])));
    }

    return chart;
  }
}
