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
import org.obiba.opal.web.gwt.app.client.support.TabPanelHelper;
import org.obiba.opal.web.gwt.app.client.ui.AbstractTabPanel;
import org.obiba.opal.web.gwt.app.client.ui.DefaultFlexTable;
import org.obiba.opal.web.gwt.plot.client.FrequencyChartFactory;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.github.gwtbootstrap.client.ui.TabPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class CategoricalSummaryView extends Composite {

  interface CategoricalSummaryViewUiBinder extends UiBinder<Widget, CategoricalSummaryView> {}

  private static final CategoricalSummaryViewUiBinder uiBinder = GWT.create(CategoricalSummaryViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  TabPanel tabPanel;

  @UiField
  AbstractTabPanel chartsPanel;

  @UiField
  SimplePanel freqPanel;

  @UiField
  SimplePanel pctPanel;

  @UiField
  DefaultFlexTable stats;

  @UiField
  DefaultFlexTable frequencies;

  private FrequencyChartFactory chartFactory = null;

  public CategoricalSummaryView(final String title, CategoricalSummaryDto categorical) {
    initWidget(uiBinder.createAndBindUi(this));

    TabPanelHelper.setTabTitle(tabPanel, 0, translations.statsMap().get("PLOT"));
    TabPanelHelper.setTabTitle(tabPanel, 1, translations.statsMap().get("STATISTICS"));

    stats.clear();
    stats.setHeader(0, translations.statsMap().get("DESC_STATISTICS"));
    stats.setHeader(1, translations.statsMap().get("VALUE"));
    int row = 0;
    stats.setWidget(row, 0, new Label(translations.statsMap().get("N")));
    stats.setWidget(row++, 1, new Label("" + Math.round(categorical.getN())));
    stats.setWidget(row, 0, new Label(translations.statsMap().get("MODE")));
    stats.setWidget(row++, 1, new Label(categorical.getMode()));

    chartsPanel.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if(event.getSelectedItem() == 1 && chartFactory != null && pctPanel.getWidget() == null) {
          pctPanel.setWidget(chartFactory.createPercentageChart(title));
        }
      }
    });

    freqPanel.clear();
    pctPanel.clear();
    if(categorical.getFrequenciesArray() != null) {
      int count = categorical.getFrequenciesArray().length();
      chartFactory = new FrequencyChartFactory();
      frequencies.clear();

      frequencies.setHeader(0, translations.statsMap().get("CATEGORY"));
      frequencies.setHeader(1, translations.statsMap().get("FREQUENCY"));
      frequencies.setHeader(2, "%");
      for(int i = 0; i < count; i++) {
        FrequencyDto value = categorical.getFrequenciesArray().get(i);
        if(value.hasValue()) {
          chartFactory.push(value.getValue(), value.getFreq(), value.getPct() * 100);
          frequencies.setWidget(i + 1, 0, new Label(value.getValue()));
          frequencies.setWidget(i + 1, 1, new Label("" + Math.round(value.getFreq())));
          frequencies.setWidget(i + 1, 2, new Label("" + value.getPct() * 100));
        }
      }
      freqPanel.setWidget(chartFactory.createValueChart(title));
    } else {
      frequencies.clear();
    }
  }

}
