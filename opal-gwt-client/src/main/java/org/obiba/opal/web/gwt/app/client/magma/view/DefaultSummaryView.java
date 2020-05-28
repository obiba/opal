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
import org.obiba.opal.web.gwt.app.client.ui.SummaryFlexTable;
import org.obiba.opal.web.model.client.math.DefaultSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DefaultSummaryView extends Composite {

  interface DefaultSummaryViewUiBinder extends UiBinder<Widget, DefaultSummaryView> {}

  private static final DefaultSummaryViewUiBinder uiBinder = GWT.create(DefaultSummaryViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  SummaryFlexTable stats;

  public DefaultSummaryView(DefaultSummaryDto summaryDto, Collection<FrequencyDto> frequenciesNonMissing,
      Collection<FrequencyDto> frequenciesMissing, double totalNonMissing, double totalMissing) {
    initWidget(uiBinder.createAndBindUi(this));

    stats.clear();

    if(summaryDto.getFrequenciesArray() != null) {
      double total = totalNonMissing + totalMissing;
      stats.drawHeader();
      stats.drawValuesFrequencies(frequenciesNonMissing, translations.nonMissing(), translations.notEmpty(),
          totalNonMissing, total);
      stats.drawValuesFrequencies(frequenciesMissing, translations.missingLabel(), translations.naLabel(), totalMissing,
          total);
      stats.drawTotal(total);
    }
  }
}
