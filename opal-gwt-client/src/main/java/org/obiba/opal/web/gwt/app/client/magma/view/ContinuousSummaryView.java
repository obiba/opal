/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.DefaultFlexTable;
import org.obiba.opal.web.gwt.plot.client.HistogramChartFactory;
import org.obiba.opal.web.gwt.plot.client.NormalProbabilityChartFactory;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.DescriptiveStatsDto;
import org.obiba.opal.web.model.client.math.IntervalFrequencyDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ContinuousSummaryView extends Composite {

  interface ContinuousSummaryViewUiBinder extends UiBinder<Widget, ContinuousSummaryView> {}

  private static final ContinuousSummaryViewUiBinder uiBinder = GWT.create(ContinuousSummaryViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  DefaultFlexTable grid;

  @UiField
  SimplePanel histogramPanel;

  @UiField
  SimplePanel normalProbability;

  private HistogramChartFactory histogram;

  private NormalProbabilityChartFactory qqPlot;

  public ContinuousSummaryView(ContinuousSummaryDto continuous) {
    initWidget(uiBinder.createAndBindUi(this));

    DescriptiveStatsDto descriptiveStats = continuous.getSummary();

    grid.clear();
    grid.setHeader(0, translations.descriptiveStatistics());
    grid.setHeader(1, translations.value());
    int row = 0;
    grid.setWidget(row, 0, new Label(translations.NLabel()));
    grid.setWidget(row++, 1, new Label("" + Math.round(descriptiveStats.getN())));
    grid.setWidget(row, 0, new Label(translations.min()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getMin()));
    grid.setWidget(row, 0, new Label(translations.max()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getMax()));
    grid.setWidget(row, 0, new Label(translations.meanLabel()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getMean()));
    grid.setWidget(row, 0, new Label(translations.median()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getMedian()));
    grid.setWidget(row, 0, new Label(translations.standardDeviationLabel()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getStdDev()));
    grid.setWidget(row, 0, new Label(translations.variance()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getVariance()));
    grid.setWidget(row, 0, new Label(translations.skewness()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getSkewness()));
    grid.setWidget(row, 0, new Label(translations.kurtosis()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getKurtosis()));
    grid.setWidget(row, 0, new Label(translations.sum()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getSum()));
    grid.setWidget(row, 0, new Label(translations.sumOfSquares()));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getSumsq()));

    if(descriptiveStats.getVariance() > 0) {
      histogram = new HistogramChartFactory();

//      histogram = new HistogramPlot(histogramElement.getId(), descriptiveStats.getMin(), descriptiveStats.getMax());
      JsArray<IntervalFrequencyDto> frequencyArray = continuous.getIntervalFrequencyArray();
      if(frequencyArray != null) {
        int length = frequencyArray.length();
        for(int i = 0; i < length; i++) {
          IntervalFrequencyDto value = frequencyArray.get(i);
          histogram.push(value.getDensity(), value.getLower(), value.getUpper());
        }
      }
      qqPlot = new NormalProbabilityChartFactory(descriptiveStats.getMin(), descriptiveStats.getMax());
      qqPlot.push(descriptiveStats.getPercentilesArray(), continuous.getDistributionPercentilesArray());
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if(histogram != null) {
      histogramPanel.clear();
      histogramPanel.add(histogram.createChart(translations.histogram(), translations.density()));
    }
    if(qqPlot != null) {
      normalProbability.clear();
      normalProbability.add(qqPlot.createChart(translations.normalProbability(), translations.theoreticalQuantiles(),
          translations.sampleQuantiles()));
    }
  }

}
