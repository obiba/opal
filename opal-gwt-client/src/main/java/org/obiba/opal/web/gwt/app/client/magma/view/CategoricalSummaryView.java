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

import org.obiba.opal.web.gwt.app.client.support.TabPanelHelper;
import org.obiba.opal.web.gwt.app.client.ui.DefaultFlexTable;
import org.obiba.opal.web.gwt.plot.client.FrequencyPlot;
import org.obiba.opal.web.gwt.plot.client.JqPlot;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class CategoricalSummaryView extends Composite {

  interface CategoricalSummaryViewUiBinder extends UiBinder<Widget, CategoricalSummaryView> {}

  private static final CategoricalSummaryViewUiBinder uiBinder = GWT.create(CategoricalSummaryViewUiBinder.class);

  @UiField
  TabPanel tabPanel;

  @UiField
  DivElement frequencyElement;

  @UiField
  DefaultFlexTable stats;

  @UiField
  DefaultFlexTable frequencies;

  final JqPlot plot;

  public CategoricalSummaryView(CategoricalSummaryDto categorical) {
    initWidget(uiBinder.createAndBindUi(this));

    // TODO translation
    TabPanelHelper.setTabTitle(tabPanel, 0, "Plot");
    TabPanelHelper.setTabTitle(tabPanel, 1, "Statistics");

    frequencyElement.setId(HTMLPanel.createUniqueId());
    frequencyElement.setAttribute("style", "width:400px;");

    // TODO translation
    stats.clear();
    stats.setHeader(0, "Descriptive Statistic");
    stats.setHeader(1, "Value");
    int row = 0;
    stats.setWidget(row, 0, new Label("N"));
    stats.setWidget(row++, 1, new Label("" + Math.round(categorical.getN())));
    stats.setWidget(row, 0, new Label("Mode"));
    stats.setWidget(row++, 1, new Label(categorical.getMode()));

    if(categorical.getFrequenciesArray() != null) {
      int count = categorical.getFrequenciesArray().length();
      int width = 400;
      if(count > 10) {
        width = 400 + 20 * (count - 10);
      }
      frequencyElement.setAttribute("style", "width:" + width + "px;");

      FrequencyPlot freqPlot = new FrequencyPlot(frequencyElement.getId());
      frequencies.clear();
      frequencies.setHeader(0, "Category");
      frequencies.setHeader(1, "Frequency");
      frequencies.setHeader(2, "%");
      for(int i = 0; i < count; i++) {
        FrequencyDto value = categorical.getFrequenciesArray().get(i);
        if(value.hasValue()) {
          freqPlot.push(value.getValue(), value.getFreq(), value.getPct() * 100);
          frequencies.setWidget(i + 1, 0, new Label(value.getValue()));
          frequencies.setWidget(i + 1, 1, new Label("" + Math.round(value.getFreq())));
          frequencies.setWidget(i + 1, 2, new Label("" + value.getPct() * 100));
        }
      }
      plot = freqPlot;

    } else {
      plot = null;
      frequencies.clear();
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if(plot != null) {
      plot.plotOrRedraw();
    }
  }

}
