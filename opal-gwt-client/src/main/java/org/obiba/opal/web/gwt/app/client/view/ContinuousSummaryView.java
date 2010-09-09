/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.plot.client.JqPlot;
import org.obiba.opal.web.gwt.plot.client.HistogramPlot;
import org.obiba.opal.web.gwt.plot.client.NormalProbabilityPlot;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.DescriptiveStatsDto;
import org.obiba.opal.web.model.client.math.IntervalFrequencyDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ContinuousSummaryView extends Composite {

  @UiTemplate("ContinuousSummaryView.ui.xml")
  interface ContinuousSummaryViewUiBinder extends UiBinder<Widget, ContinuousSummaryView> {
  }

  private static ContinuousSummaryViewUiBinder uiBinder = GWT.create(ContinuousSummaryViewUiBinder.class);

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

  final Widget widget;

  final JqPlot histogram;

  final JqPlot qqPlot;

  public ContinuousSummaryView(ContinuousSummaryDto continuous, boolean isInteger) {
    widget = uiBinder.createAndBindUi(this);
    initWidget(widget);
    obs.setText("" + continuous.getSummary().getN());
    max.setText("" + continuous.getSummary().getMax());
    min.setText("" + continuous.getSummary().getMin());
    mean.setText("" + continuous.getSummary().getMean());
    median.setText("" + continuous.getSummary().getMedian());
    stdDev.setText("" + continuous.getSummary().getStdDev());
    variance.setText("" + continuous.getSummary().getVariance());
    skewness.setText("" + continuous.getSummary().getSkewness());
    kurtosis.setText("" + continuous.getSummary().getKurtosis());
    sum.setText("" + continuous.getSummary().getSum());
    sumsq.setText("" + continuous.getSummary().getSumsq());

    DescriptiveStatsDto ds = continuous.getSummary();
    if(ds.getVariance() > 0) {
      HistogramPlot plot = new HistogramPlot("histogram-plot", ds.getMin(), ds.getMax(), isInteger);
      if(continuous.getIntervalFrequencyArray() != null) {
        for(int i = 0; i < continuous.getIntervalFrequencyArray().length(); i++) {
          IntervalFrequencyDto value = continuous.getIntervalFrequencyArray().get(i);
          plot.push(value.getLower(), value.getUpper(), value.getDensity());
        }
      }
      histogram = plot;
      NormalProbabilityPlot qqplot = new NormalProbabilityPlot("normal-probability-plot", ds.getMin(), ds.getMax());
      qqplot.push(ds.getPercentilesArray(), continuous.getDistributionPercentilesArray());
      this.qqPlot = qqplot;
    } else {
      this.histogram = this.qqPlot = null;
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
