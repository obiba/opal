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

import org.obiba.opal.web.gwt.app.client.presenter.VariablePresenter;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.visualizations.ColumnChart;

/**
 *
 */
public class VariableView extends Composite implements VariablePresenter.Display {

  ColumnChart chart;

  public VariableView() {
    chart = new ColumnChart();
    initWidget(chart);
  }

  @Override
  public void clearChart() {
    chart.setVisible(false);
  }

  @Override
  public void renderData(AbstractDataTable data) {
    ColumnChart.Options options = ColumnChart.Options.create();
    options.setSize(800, 200);
    options.setLegend(LegendPosition.NONE);
    chart.draw(data, options);
    chart.setVisible(true);
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

}
