/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.project.view;

import org.obiba.opal.web.gwt.app.client.support.TabPanelHelper;
import org.obiba.opal.web.gwt.app.client.workbench.view.DefaultFlexTable;
import org.obiba.opal.web.gwt.plot.client.HistogramPlot;
import org.obiba.opal.web.gwt.plot.client.NormalProbabilityPlot;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.DescriptiveStatsDto;
import org.obiba.opal.web.model.client.math.IntervalFrequencyDto;

import com.github.gwtbootstrap.client.ui.TabPanel;
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

  interface ContinuousSummaryViewUiBinder extends UiBinder<Widget, ContinuousSummaryView> {}

  private static final ContinuousSummaryViewUiBinder uiBinder = GWT.create(ContinuousSummaryViewUiBinder.class);

  @UiField
  TabPanel tabPanel;

  @UiField
  DefaultFlexTable grid;

  @UiField
  DivElement histogramElement;

  @UiField
  DivElement qqPlotElement;

  HistogramPlot histogram;

  NormalProbabilityPlot qqPlot;

  public ContinuousSummaryView(ContinuousSummaryDto continuous) {
    initWidget(uiBinder.createAndBindUi(this));

    // TODO translation
    TabPanelHelper.setTabTitle(tabPanel, 0, "Plot");
    TabPanelHelper.setTabTitle(tabPanel, 1, "Statistics");

    histogramElement.setId(HTMLPanel.createUniqueId());
    qqPlotElement.setId(HTMLPanel.createUniqueId());

    DescriptiveStatsDto descriptiveStats = continuous.getSummary();

    // TODO translation
    grid.clear();
    grid.setHeader(0, "Descriptive Statistic");
    grid.setHeader(1, "Value");
    int row = 0;
    grid.setWidget(row, 0, new Label("N"));
    grid.setWidget(row++, 1, new Label("" + Math.round(descriptiveStats.getN())));
    grid.setWidget(row, 0, new Label("Min"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getMin()));
    grid.setWidget(row, 0, new Label("Max"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getMax()));
    grid.setWidget(row, 0, new Label("Mean"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getMean()));
    grid.setWidget(row, 0, new Label("Median"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getMedian()));
    grid.setWidget(row, 0, new Label("Standard Deviation"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getStdDev()));
    grid.setWidget(row, 0, new Label("Variance"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getVariance()));
    grid.setWidget(row, 0, new Label("Skewness"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getSkewness()));
    grid.setWidget(row, 0, new Label("Kurtosis"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getKurtosis()));
    grid.setWidget(row, 0, new Label("Sum"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getSum()));
    grid.setWidget(row, 0, new Label("Sum of squares"));
    grid.setWidget(row++, 1, new Label("" + descriptiveStats.getSumsq()));


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
