/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import java.util.Collection;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.DefaultFlexTable;
import org.obiba.opal.web.gwt.app.client.ui.PolygonMap;
import org.obiba.opal.web.gwt.app.client.ui.SummaryFlexTable;
import org.obiba.opal.web.model.client.math.FrequencyDto;
import org.obiba.opal.web.model.client.math.GeoSummaryDto;
import org.obiba.opal.web.model.client.math.PointDto;

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
public class GeoSummaryView extends Composite {

  interface GeoSummaryViewUiBinder extends UiBinder<Widget, GeoSummaryView> {}

  private static final GeoSummaryViewUiBinder uiBinder = GWT.create(GeoSummaryViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  SummaryFlexTable stats;

  @UiField
  PolygonMap map;

  @UiField
  DefaultFlexTable grid;

  public GeoSummaryView(GeoSummaryDto summaryDto, Collection<FrequencyDto> frequenciesNonMissing,
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

    for(PointDto pointDto : JsArrays.toIterable(summaryDto.getPointsArray())) {
      map.addPoint(pointDto.getLon(), pointDto.getLat());
    }

    map.drawPolygon();
    addDescriptiveStatistics();
  }

  private void addDescriptiveStatistics() {
    grid.clear();
    grid.setHeader(0, translations.descriptiveStatistics());
    grid.setHeader(1, translations.value());

    int row = 0;
    NumberFormat nf = NumberFormat.getFormat("#.##");

    grid.setWidget(row, 0, new Label(translations.approxArea()));
    grid.setWidget(row++, 1, new Label(String.valueOf(nf.format(map.getApproxArea()))));
  }

}
