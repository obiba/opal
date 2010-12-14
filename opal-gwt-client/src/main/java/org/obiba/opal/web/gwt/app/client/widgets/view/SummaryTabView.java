/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.navigator.view.CategoricalSummaryView;
import org.obiba.opal.web.gwt.app.client.navigator.view.ContinuousSummaryView;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class SummaryTabView implements SummaryTabPresenter.Display {

  @UiTemplate("SummaryTabView.ui.xml")
  interface SummaryTabViewUiBinder extends UiBinder<Widget, SummaryTabView> {
  }

  private static SummaryTabViewUiBinder uiBinder = GWT.create(SummaryTabViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  @UiField
  Panel summary;

  public SummaryTabView() {
    uiWidget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public void startProcessing() {

  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void renderSummary(SummaryStatisticsDto dto) {
    summary.clear();
    if(dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous) != null) {
      ContinuousSummaryDto continuous = dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous).cast();
      summary.add(new ContinuousSummaryView(continuous));
    } else if(dto.getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical) != null) {
      CategoricalSummaryDto categorical = dto.getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
      summary.add(new CategoricalSummaryView(categorical));
    } else {
      summary.add(new Label(translations.noDataAvailableLabel()));
    }
  }

  @Override
  public void requestingSummary() {
    summary.clear();
    summary.add(new Image("image/loading.gif"));
  }
}
