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
import org.obiba.opal.web.gwt.app.client.workbench.view.NumericTextBox;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.ContinuousSummaryDto;
import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class SummaryTabView implements SummaryTabPresenter.Display {

  @UiTemplate("SummaryTabView.ui.xml")
  interface SummaryTabViewUiBinder extends UiBinder<Widget, SummaryTabView> {}

  private static final SummaryTabViewUiBinder uiBinder = GWT.create(SummaryTabViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  @UiField
  Alert previewSummary;

  @UiField
  Label previewSummaryText;

  @UiField
  Label previewSummaryTextSuffix;

  @UiField
  NumericTextBox limitTextBox;

  @UiField
  Anchor refreshSummaryLink;

  @UiField
  Anchor fullSummaryLink;

  @UiField
  Anchor cancelSummaryLink;

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
      ContinuousSummaryDto continuous = dto.getExtension(ContinuousSummaryDto.SummaryStatisticsDtoExtensions.continuous)
          .cast();
      summary.add(new ContinuousSummaryView(continuous));
    } else if(dto.getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical) != null) {
      CategoricalSummaryDto categorical = dto
          .getExtension(CategoricalSummaryDto.SummaryStatisticsDtoExtensions.categorical).cast();
      summary.add(new CategoricalSummaryView(categorical));
    } else {
      renderNoSummary();
    }
  }

  @Override
  public void renderNoSummary() {
    summary.clear();
    summary.add(new Label(translations.noSummaryDataAvailableLabel()));
  }

  @Override
  public void requestingSummary(int limit, int max) {
    summary.clear();
    summary.add(new Image("image/loading.gif"));

    limitTextBox.setMaxConstrained(true);
    limitTextBox.setMaxConstrained(false);
    limitTextBox.setMin(1);
    limitTextBox.setValue(String.valueOf(limit));
    if(limit < max) {
      limitTextBox.setVisible(true);
      previewSummaryTextSuffix.setVisible(true);
      fullSummaryLink.setVisible(false);
      refreshSummaryLink.setVisible(false);
      previewSummaryText.setText("Summary preview pending on");
      previewSummaryTextSuffix.setText("/" + max + " entities");
    } else {
      limitTextBox.setVisible(false);
      previewSummaryTextSuffix.setVisible(false);
      fullSummaryLink.setVisible(false);
      refreshSummaryLink.setVisible(false);
      previewSummaryText.setText("Full summary pending...");
    }
    previewSummary.setVisible(true);
    cancelSummaryLink.setVisible(true);
  }

  @Override
  public void renderSummaryLimit(int limit, int max) {
    if(limit < max) {
      limitTextBox.setValue(String.valueOf(limit));

      previewSummary.setVisible(true);
      cancelSummaryLink.setVisible(false);
      refreshSummaryLink.setVisible(true);
      fullSummaryLink.setVisible(true);
      previewSummaryText.setText("This is a summary preview on");
      previewSummaryTextSuffix.setText("/" + max + " entities");
    } else {
      previewSummary.setVisible(false);
    }
  }

  @Override
  public void renderCancelSummaryLimit(int limit, int max) {
    summary.clear();
    limitTextBox.setValue(String.valueOf(limit));
    limitTextBox.setVisible(true);

    previewSummary.setVisible(true);
    cancelSummaryLink.setVisible(false);
    previewSummaryTextSuffix.setVisible(true);
    fullSummaryLink.setVisible(true);
    refreshSummaryLink.setVisible(true);
    previewSummaryText.setText("Fetch summary preview on");
    previewSummaryTextSuffix.setText("/" + max + " entities");
  }

  @Override
  public HasClickHandlers getFullSummary() {
    return fullSummaryLink;
  }

  @Override
  public HasClickHandlers getCancelSummary() {
    return cancelSummaryLink;
  }

  @Override
  public HasClickHandlers getRefreshSummary() {
    return refreshSummaryLink;
  }

  @Override
  public Number getLimit() {
    return limitTextBox.getNumberValue();
  }
}
