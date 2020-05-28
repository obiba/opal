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

import java.util.Collection;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.ui.SummaryFlexTable;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.TextSummaryDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class TextSummaryView extends Composite {

  interface DefaultSummaryViewUiBinder extends UiBinder<Widget, TextSummaryView> {}

  private static final DefaultSummaryViewUiBinder uiBinder = GWT.create(DefaultSummaryViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  SummaryFlexTable stats;

  public TextSummaryView(TextSummaryDto summaryDto, Collection<FrequencyDto> frequenciesNonMissing,
      Collection<FrequencyDto> frequenciesMissing, double totalNonMissing, double totalMissing, double totalOther,
      int maxResults) {
    initWidget(uiBinder.createAndBindUi(this));
    stats.clear();

    if(summaryDto.getFrequenciesArray() != null) {
      double total = totalNonMissing + totalMissing + totalOther;
      stats.drawHeader();
      stats.drawValuesFrequencies(frequenciesNonMissing,
          TranslationsUtils.replaceArguments(translations.nonMissingTopN(), String.valueOf(maxResults)),
          translations.notEmpty(), totalNonMissing + totalOther, totalOther, total);
      stats.drawValuesFrequencies(frequenciesMissing, translations.missingLabel(), translations.naLabel(), totalMissing,
          total);
      stats.drawTotal(total);
    }
  }
}
