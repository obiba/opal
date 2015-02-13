/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importvariables.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.ConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.VariablesImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.VariablesImportUiHandlers;
import org.obiba.opal.web.gwt.app.client.support.LanguageLocale;
import org.obiba.opal.web.gwt.app.client.ui.CharacterSetView;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.DatasourceParsingErrorPanel;
import org.obiba.opal.web.gwt.app.client.ui.DropdownSuggestBox;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.WizardModalBox;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController.ResetHandler;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class VariablesImportView extends ModalPopupViewWithUiHandlers<VariablesImportUiHandlers>
    implements VariablesImportPresenter.Display {

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardModalBox dialog;

  @UiField
  WizardStep fileSelectionStep;

  @UiField
  WizardStep compareStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  ControlGroup fileSelectionGroup;

  @UiField
  SimplePanel fileSelectionPanel;

  @UiField
  Paragraph failed;

  @UiField
  DatasourceParsingErrorPanel datasourceParsingErrors;

  @UiField
  CollapsiblePanel options;

  @UiField
  ControlGroup charsetGroup;

  @UiField
  CharacterSetView charsetView;

  @UiField
  TextBox spssEntityType;

  @UiField
  ControlGroup localeGroup;

  @UiField
  DropdownSuggestBox localeNameBox;

  private FileSelectionPresenter.Display fileSelection;

  private WizardStepChain stepChain;

  @Inject
  public VariablesImportView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWizardDialog();
    initializeLocales();
    initDialogEventHandlers();
  }

  private void initDialogEventHandlers() {
    dialog.addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        getUiHandlers().processVariablesFile();
      }
    });
  }

  private void initWizardDialog() {
    options.setVisible(false);

    stepChain = WizardStepChain.Builder.create(dialog)//
        .append(fileSelectionStep)//
        .title(translations.variablesImportFileSelectionStep())//
        .onReset(new ResetHandler() {

          @Override
          public void onReset() {
            fileSelection.clearFile();
            hideErrors();
          }
        })//

        .append(compareStep)//
        .title(translations.variablesImportCompareStep())//
        .append(conclusionStep)//
        .title(translations.variablesImportPending())//
        .conclusion()//

        .onPrevious().onCancel().onClose().build();
  }

  private void initializeLocales() {
    localeNameBox.getSuggestOracle().clear();

    for(String locale : LanguageLocale.getAllLocales()) {
      localeNameBox.getSuggestOracle().add(locale);
    }

    localeNameBox.setText(LanguageLocale.EN.getName());
  }

  @UiHandler("variableTemplatePanel")
  public void onDownloadExcelTemplateClicked(ClickEvent event) {
    getUiHandlers().downExcelTemplate();
  }

  @Override
  public void onShow() {
    stepChain.reset();
  }

  @Override
  public void gotoPreview() {
    clearErrors();
    dialog.setProgress(true);
    dialog.setNextEnabled(false);
    dialog.setCancelEnabled(false);
  }

  @Override
  public void enableCompletion() {
    dialog.setCancelEnabled(true);
    dialog.setProgress(false);
    stepChain.onNext();
  }

  @Override
  public void disableCompletion() {
    dialog.setProgress(false);
    dialog.setNextEnabled(true);
    dialog.setCancelEnabled(true);
  }

  @Override
  public void setFileSelectionDisplay(Display display) {
    fileSelectionPanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public String getSelectedFile() {
    return fileSelection.getFile();
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        getUiHandlers().createTable();
      }
    });
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
  public void clearErrors() {
    dialog.closeAlerts();
  }

  @Override
  public void hideErrors() {
    failed.setVisible(false);
    datasourceParsingErrors.setVisible(false);
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case FILE_SELECTION:
          group = fileSelectionGroup;
          break;
        case LOCALE:
          group = localeGroup;
          break;
        case CHARSET:
          group = charsetGroup;
          break;
      }
    }
    if(group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }

  @Override
  public void setComparedDatasourcesReportDisplay(ComparedDatasourcesReportStepPresenter.Display display) {
    compareStep.removeStepContent();
    compareStep.add(display.asWidget());
  }

  @Override
  public void setConclusionDisplay(ConclusionStepPresenter.Display display) {
    // conclusionDisplay = display;
    conclusionStep.removeStepContent();
    conclusionStep.add(display.asWidget());
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected Modal asModal() {
    return dialog;
  }

  @Override
  public HasText getCharsetText() {
    return charsetView.getCharsetText();
  }

  @Override
  public HasText getSpssEntityType() {
    return spssEntityType;
  }

  @Override
  public String getLocale() {
    return localeNameBox.getText();
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    charsetView.setDefaultCharset(defaultCharset);
  }

  @Override
  public void showSpssSpecificPanel(boolean show) {
    options.setVisible(show);
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("VariablesImportView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, VariablesImportView> {}

}
