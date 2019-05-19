/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.AbstractTabPanel;
import org.obiba.opal.web.gwt.app.client.ui.SummaryFlexTable;
import org.obiba.opal.web.gwt.plot.client.FrequencyChartFactory;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class CategoricalSummaryView extends Composite {

  interface CategoricalSummaryViewUiBinder extends UiBinder<Widget, CategoricalSummaryView> {}

  private static final CategoricalSummaryViewUiBinder uiBinder = GWT.create(CategoricalSummaryViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  AbstractTabPanel chartsPanel;

  @UiField
  FlowPanel freqPanel;

  @UiField
  FlowPanel pctPanel;

  @UiField
  SummaryFlexTable stats;

  private FrequencyChartFactory chartFactory = null;

  public CategoricalSummaryView(final String title, CategoricalSummaryDto categorical,
      Collection<FrequencyDto> categoriesNonMissing, Collection<FrequencyDto> categoriesMissing, double totalNonMissing,
      double totalMissing, double totalOther, VariableDto variableDto) {
    initWidget(uiBinder.createAndBindUi(this));

    chartsPanel.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if(event.getSelectedItem() == 1 && chartFactory != null && pctPanel.getWidgetCount() == 0) {
          pctPanel.add(chartFactory.createPercentageChart(title));
        }
      }
    });

    stats.clear();
    stats.drawHeader();

    freqPanel.clear();
    pctPanel.clear();
    if(categorical.getFrequenciesArray() != null) {

      double total = totalNonMissing + totalMissing + totalOther;
      stats.setVariable(variableDto);
      stats.drawValuesFrequencies(categoriesNonMissing, translations.nonMissing(), translations.notEmpty(),
          totalNonMissing + totalOther, totalOther, total);
      stats.drawValuesFrequencies(categoriesMissing, translations.missingLabel(), translations.naLabel(), totalMissing,
          total);
      stats.drawTotal(total);

      // Populate chart
      chartFactory = new FrequencyChartFactory();
      for(FrequencyDto frequency : JsArrays.toIterable(categorical.getFrequenciesArray())) {
        if(frequency.hasValue()) {
          chartFactory.push(frequency.getValue(), frequency.getFreq(),
              new BigDecimal(frequency.getPct() * 100).setScale(2, RoundingMode.HALF_UP).doubleValue());
        }
      }
      freqPanel.add(chartFactory.createValueChart(title));
    }
  }

}
