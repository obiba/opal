/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.presenter.IdentifiersMapPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

public class IdentifiersMapView extends PopupViewImpl implements IdentifiersMapPresenter.Display {

  @UiTemplate("IdentifiersMapView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, IdentifiersMapView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep formatStep;

  @UiField
  WizardStep unitStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  CsvOptionsView csvOptions;

  @UiField
  ListBox unitListBox;

  @UiField
  SimplePanel conclusionPanel;

  private WizardStepChain stepChain;

  private ValidationHandler fileSelectionValidator;

  @Inject
  public IdentifiersMapView(EventBus eventBus) {
    super(eventBus);
    this.widget = uiBinder.createAndBindUi(this);
    initWidgets();
    initWizardDialog();
  }

  private void initWidgets() {
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//

    .append(formatStep)//
    .title(translations.identifiersMapFileStep())//

    .append(unitStep)//
    .title(translations.identifiersMapUnitStep())//

    .append(conclusionStep)//
    .conclusion()//

    .onPrevious().build();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
  }

  @Override
  public void show() {
    stepChain.reset();
    super.show();
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
    return dialog.addCloseClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFileSelectedClickHandler(final ClickHandler handler) {
    return dialog.addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(formatStep.isVisible()) {
          if(fileSelectionValidator.validate()) {
            handler.onClick(evt);
            dialog.setProgress(true);
            dialog.setNextEnabled(false);
            dialog.setCancelEnabled(false);
          }
        } else
          stepChain.onNext();
      }
    });
  }

  @Override
  public void setCsvOptionsFileSelectorWidgetDisplay(Display display) {
    csvOptions.setCsvFileSelectorWidgetDisplay(display);
  }

  @Override
  public void renderPendingConclusion() {
    conclusionStep.setStepTitle(translations.identifierMapPendingConclusion());
    conclusionPanel.clear();
    dialog.setProgress(true);
    stepChain.onNext();
    dialog.setProgress(true);
    dialog.setCloseEnabled(false);
    dialog.setCancelEnabled(false);
  }

  @Override
  public void renderCompletedConclusion(String count) {
    dialog.setProgress(false);
    conclusionStep.setStepTitle(translations.identifierMapCompletedConclusion());
    Label countLabel = new Label(translations.identifierMapUpdateCount() + ": " + count);
    countLabel.addStyleName("indent");
    conclusionPanel.add(countLabel);
    dialog.setCloseEnabled(true);
    dialog.setProgress(false);
  }

  @Override
  public void renderFailedConclusion() {
    dialog.setProgress(false);
    conclusionStep.setStepTitle(translations.identifierMapFailedConclusion());
    dialog.setCancelEnabled(true);
    dialog.setProgress(false);
  }

  @Override
  public CsvOptionsView getCsvOptions() {
    return csvOptions;
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    csvOptions.setDefaultCharset(defaultCharset);
  }

  @Override
  public void renderMappedUnits(JsArray<FunctionalUnitDto> units) {
    unitListBox.clear();
    for(FunctionalUnitDto unit : JsArrays.toIterable(units)) {
      unitListBox.addItem(unit.getName());
    }
    dialog.setProgress(false);
    dialog.setCancelEnabled(true);
    stepChain.onNext();
  }

  @Override
  public void renderMappedUnitsFailed() {
    dialog.setProgress(false);
    dialog.setNextEnabled(true);
    dialog.setCancelEnabled(true);
  }

  @Override
  public void setFileSelectionValidator(ValidationHandler handler) {
    fileSelectionValidator = handler;
  }

  @Override
  public String getSelectedUnitName() {
    return unitListBox.getItemText(unitListBox.getSelectedIndex());
  }

}
