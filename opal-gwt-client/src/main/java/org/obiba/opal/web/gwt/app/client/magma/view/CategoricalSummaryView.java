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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.AbstractTabPanel;
import org.obiba.opal.web.gwt.app.client.ui.SummaryFlexTable;
import org.obiba.opal.web.gwt.plot.client.FrequencyChartFactory;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
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
      final Map<String, CategoryDto> categoryDtos) {
    initWidget(uiBinder.createAndBindUi(this));

    chartsPanel.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if(event.getSelectedItem() == 1 && chartFactory != null && pctPanel.getWidgetCount() == 0) {
          pctPanel.add(chartFactory.createPercentageChart(title));
        }
      }
    });
    int row = 0;

    stats.clear();
    stats.drawHeader();

    stats.getFlexCellFormatter().setColSpan(row, 0, 4);
    stats.getFlexCellFormatter().addStyleName(row, 0, "table-subheader");
    stats.setWidget(row++, 0, new Label(translations.nonMissing()));

    freqPanel.clear();
    pctPanel.clear();
    if(categorical.getFrequenciesArray() != null) {
      chartFactory = new FrequencyChartFactory();

      final double[] totals = { 0d, 0d };
      ImmutableListMultimap<Boolean, FrequencyDto> categoriesByMissing = Multimaps
          .index(JsArrays.toIterable(categorical.getFrequenciesArray()), new Function<FrequencyDto, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable FrequencyDto input) {
              if(categoryDtos.containsKey(input.getValue()) && !categoryDtos.get(input.getValue()).getIsMissing()) {
                totals[0] += input.getFreq();
                return true;
              }
              totals[1] += input.getFreq();
              return false;
            }
          });

      // non missings
      for(FrequencyDto frequency : categoriesByMissing.get(true)) {
        if(frequency.hasValue()) {
          chartFactory.push(frequency.getValue(), frequency.getFreq(),
              new BigDecimal(frequency.getPct() * 100).setScale(4, RoundingMode.HALF_UP).doubleValue());
          stats.setWidget(row, 0, new Label(frequency.getValue()));
          stats.setWidget(row, 1, new Label("" + Math.round(frequency.getFreq())));
          stats.setWidget(row, 2, new Label(formatDecimal(frequency.getFreq() / totals[0] * 100) + "%"));
          stats.setWidget(row++, 3, new Label(formatDecimal(frequency.getPct() * 100) + "%"));
        }
      }
      double total = totals[0] + totals[1];

      stats.setWidget(row, 0, new Label(translations.subtotal()));
      stats.setWidget(row, 1, new Label(String.valueOf(Math.round(totals[0]))));
      stats.setWidget(row, 2, new Label("100%"));
      stats.setWidget(row++, 3, new Label(formatDecimal(totals[0] / total * 100) + "%"));

      stats.getFlexCellFormatter().setColSpan(row, 0, 4);
      stats.getFlexCellFormatter().addStyleName(row, 0, "table-subheader");
      stats.setWidget(row++, 0, new Label(translations.missingLabel()));
      // the next line should be white
      if(row % 2 == 0) row++;

      // missings
      for(FrequencyDto frequency : categoriesByMissing.get(false)) {
        if(frequency.hasValue()) {
          chartFactory.push(frequency.getValue(), frequency.getFreq(),
              new BigDecimal(frequency.getPct() * 100).setScale(4, RoundingMode.HALF_UP).doubleValue());
          stats.setWidget(row, 0, new Label(frequency.getValue()));
          stats.setWidget(row, 1, new Label("" + Math.round(frequency.getFreq())));
          stats.setWidget(row, 2, new Label(formatDecimal(frequency.getFreq() / totals[1] * 100) + "%"));
          stats.setWidget(row++, 3, new Label(formatDecimal(frequency.getPct() * 100) + "%"));
        }
      }

      stats.setWidget(row, 0, new Label(translations.subtotal()));
      stats.setWidget(row, 1, new Label(String.valueOf(Math.round(totals[1]))));
      stats.setWidget(row, 2, new Label("100%"));
      stats.setWidget(row++, 3, new Label(formatDecimal(totals[1] / total * 100) + "%"));

      stats.getFlexCellFormatter().addStyleName(row, 0, "property-key");
      stats.setWidget(row, 0, new Label(translations.totalLabel()));
      stats.setWidget(row, 1, new Label(String.valueOf(Math.round(total))));
      stats.setWidget(row, 2, new Label(String.valueOf(formatDecimal(totals[1] / total * 100)) + "%"));
      stats.setWidget(row++, 3, new Label("100%"));

      freqPanel.add(chartFactory.createValueChart(title));
    }
  }

  private String formatDecimal(double number) {
    NumberFormat nf = NumberFormat.getFormat("#.##");
    return nf.format(number);
  }
}
