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
import org.obiba.opal.web.gwt.app.client.ui.SummaryFlexTable;
import org.obiba.opal.web.gwt.plot.client.HistogramChartFactory;
import org.obiba.opal.web.gwt.plot.client.NormalProbabilityChartFactory;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.DescriptiveStatsDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.IntervalFrequencyDto;

import com.google.common.collect.ImmutableList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.NumberFormat;
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

  @UiField
  SummaryFlexTable stats;

  private HistogramChartFactory histogram;

  private NormalProbabilityChartFactory qqPlot;

  public ContinuousSummaryView(ContinuousSummaryDto continuous, ImmutableList<FrequencyDto> frequenciesNonMissing,
      ImmutableList<FrequencyDto> frequenciesMissing, double totalNonMissing, double totalMissing) {
    initWidget(uiBinder.createAndBindUi(this));

    initDescriptivestats(continuous);

    stats.drawHeader();
    double total = totalNonMissing + totalMissing;
    stats.drawValuesFrequencies(frequenciesNonMissing, translations.nonMissing(), translations.notEmpty(),
        totalNonMissing, total);
    stats.drawValuesFrequencies(frequenciesMissing, translations.missingLabel(), translations.naLabel(), totalMissing,
        total);
    stats.drawTotal(total);
  }

  private void initDescriptivestats(ContinuousSummaryDto continuous) {
    DescriptiveStatsDto descriptiveStats = continuous.getSummary();
    addDescriptiveStatistics(descriptiveStats);

    if(descriptiveStats.getVariance() > 0) {
      histogram = new HistogramChartFactory();
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

  private void addDescriptiveStatistics(DescriptiveStatsDto descriptiveStats) {
    grid.clear();
    grid.setHeader(0, translations.descriptiveStatistics());
    grid.setHeader(1, translations.value());
    int row = 0;

    addGridStat(translations.NLabel(), Math.round(descriptiveStats.getN()), row++);
    addGridStat(translations.min(), descriptiveStats.getMin(), row++);
    addGridStat(translations.max(), descriptiveStats.getMax(), row++);
    addGridStat(translations.meanLabel(), descriptiveStats.getMean(), row++);
    addGridStat(translations.geometricMeanLabel(), descriptiveStats.getGeometricMean(), row++);
    addGridStat(translations.median(), descriptiveStats.getMedian(), row++);
    addGridStat(translations.standardDeviationLabel(), descriptiveStats.getStdDev(), row++);
    addGridStat(translations.variance(), descriptiveStats.getVariance(), row++);
    addGridStat(translations.skewness(), descriptiveStats.getSkewness(), row++);
    addGridStat(translations.kurtosis(), descriptiveStats.getKurtosis(), row++);
    addGridStat(translations.sum(), descriptiveStats.getSum(), row++);
    addGridStat(translations.sumOfSquares(), descriptiveStats.getSumsq(), row++);
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

  private void addGridStat(String title, double number, int row) {
    NumberFormat nf = NumberFormat.getFormat("#.####");

    grid.setWidget(row, 0, new Label(title));
    grid.setWidget(row, 1, new Label(String.valueOf(nf.format(number))));
  }
}
