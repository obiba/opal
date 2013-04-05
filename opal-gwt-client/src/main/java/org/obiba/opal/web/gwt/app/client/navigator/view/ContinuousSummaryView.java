/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import org.obiba.opal.web.gwt.plot.client.HistogramPlot;
import org.obiba.opal.web.gwt.plot.client.NormalProbabilityPlot;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.DescriptiveStatsDto;
import org.obiba.opal.web.model.client.math.IntervalFrequencyDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ContinuousSummaryView extends Composite {

  @UiTemplate("ContinuousSummaryView.ui.xml")
  interface ContinuousSummaryViewUiBinder extends UiBinder<Widget, ContinuousSummaryView> {}

  private static final ContinuousSummaryViewUiBinder uiBinder = GWT.create(ContinuousSummaryViewUiBinder.class);

  @UiField
  Label obs;

  @UiField
  Label min;

  @UiField
  Label max;

  @UiField
  Label mean;

  @UiField
  Label median;

  @UiField
  Label stdDev;

  @UiField
  Label variance;

  @UiField
  Label skewness;

  @UiField
  Label kurtosis;

  @UiField
  Label sum;

  @UiField
  Label sumsq;

  @UiField
  DivElement histogramElement;

  @UiField
  DivElement qqPlotElement;

  final Widget widget;

  HistogramPlot histogram;

  NormalProbabilityPlot qqPlot;

  public ContinuousSummaryView(ContinuousSummaryDto continuous) {
    widget = uiBinder.createAndBindUi(this);
    initWidget(widget);
    histogramElement.setId(HTMLPanel.createUniqueId());
    qqPlotElement.setId(HTMLPanel.createUniqueId());

    DescriptiveStatsDto descriptiveStats = continuous.getSummary();

    obs.setText("" + Math.round(descriptiveStats.getN()));
    max.setText("" + descriptiveStats.getMax());
    min.setText("" + descriptiveStats.getMin());
    mean.setText("" + descriptiveStats.getMean());
    median.setText("" + descriptiveStats.getMedian());
    stdDev.setText("" + descriptiveStats.getStdDev());
    variance.setText("" + descriptiveStats.getVariance());
    skewness.setText("" + descriptiveStats.getSkewness());
    kurtosis.setText("" + descriptiveStats.getKurtosis());
    sum.setText("" + descriptiveStats.getSum());
    sumsq.setText("" + descriptiveStats.getSumsq());

    if(descriptiveStats.getVariance() > 0) {
      histogram = new HistogramPlot(histogramElement.getId(), descriptiveStats.getMin(), descriptiveStats.getMax());
      JsArray<IntervalFrequencyDto> frequencyArray = continuous.getIntervalFrequencyArray();
      if(frequencyArray != null) {
        int length = frequencyArray.length();
        for(int i = 0; i < length; i++) {
          IntervalFrequencyDto value = frequencyArray.get(i);
          histogram.push(value.getLower(), value.getUpper(), value.getDensity());
        }
      }
      qqPlot = new NormalProbabilityPlot(qqPlotElement.getId(), descriptiveStats.getMin(), descriptiveStats.getMax());
      qqPlot.push(descriptiveStats.getPercentilesArray(), continuous.getDistributionPercentilesArray());
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if(histogram != null) {
      histogram.plotOrRedraw();
    }
    if(qqPlot != null) {
      qqPlot.plotOrRedraw();
    }
  }

}
