/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.DefaultFlexTable;
import org.obiba.opal.web.model.client.math.BinarySummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class BinarySummaryView extends Composite {

  interface BinarySummaryViewUiBinder extends UiBinder<Widget, BinarySummaryView> {}

  private static final BinarySummaryViewUiBinder uiBinder = GWT.create(BinarySummaryViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  DefaultFlexTable stats;

  @UiField
  DefaultFlexTable frequencies;

  public BinarySummaryView(BinarySummaryDto summaryDto) {
    initWidget(uiBinder.createAndBindUi(this));
    stats.clear();
    stats.setHeader(0, translations.descriptiveStatistics());
    stats.setHeader(1, translations.value());
    int row = 0;
    stats.setWidget(row, 0, new Label(translations.NLabel()));
    stats.setWidget(row++, 1, new Label("" + Math.round(summaryDto.getN())));

    if(summaryDto.getFrequenciesArray() != null) {
      int count = summaryDto.getFrequenciesArray().length();

      frequencies.setHeader(0, translations.value());
      frequencies.setHeader(1, translations.frequency());
      frequencies.setHeader(2, "%");

      List<FrequencyDto> frequencyDtos = JsArrays.toList(summaryDto.getFrequenciesArray());
      Collections.sort(frequencyDtos, new Comparator<FrequencyDto>() {
        @Override
        public int compare(FrequencyDto o1, FrequencyDto o2) {
          return ComparisonChain.start().compare(o1.getValue(), o2.getValue(), Ordering.natural().reverse()).result();
        }
      });
      // Not empty before N/A,
      for(int i = 0; i < count; i++) {
        FrequencyDto value = frequencyDtos.get(i);
        if(value.hasValue()) {
          frequencies.setWidget(i + 1, 0, new Label(value.getValue().equals("NOT_NULL")
              ? translations.notNullStatistics()
              : value.getValue())); // Translate N/A and NOT_NULL
          frequencies.setWidget(i + 1, 1, new Label("" + Math.round(value.getFreq())));
          frequencies.setWidget(i + 1, 2, new Label("" + formatDecimal(value.getPct() * 100)));
        }
      }
    } else {
      frequencies.clear();
    }
  }

  private String formatDecimal(double number) {
    NumberFormat nf = NumberFormat.getFormat("#.##");
    return nf.format(number);
  }
}
