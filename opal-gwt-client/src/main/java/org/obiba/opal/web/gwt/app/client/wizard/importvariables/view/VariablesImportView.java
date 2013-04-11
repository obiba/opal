/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.LanguageLocale;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.widgets.view.CharacterSetView;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.WidgetProvider;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.VariablesImportPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.DatasourceParsingErrorPanel;
import org.obiba.opal.web.gwt.app.client.workbench.view.DropdownSuggestBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

public class VariablesImportView extends PopupViewImpl implements VariablesImportPresenter.Display {

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep fileSelectionStep;

  @UiField
  WizardStep compareStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  SimplePanel fileSelectionPanel;

  @UiField
  HTMLPanel fileSelectionHelp;

  @UiField
  Button downloadExcelTemplateButton;

  @UiField
  Label failed;

  @UiField
  DatasourceParsingErrorPanel datasourceParsingErrors;

  @UiField
  ListBox datasources;

  @UiField
  Label destinationLabel;

  @UiField
  FlowPanel spssPanel;

  @UiField
  CharacterSetView charsetView;

  @UiField
  TextBox spssEntityType;

  @UiField
  DropdownSuggestBox localeNameBox;

  private FileSelectionPresenter.Display fileSelection;

  private WizardStepChain stepChain;

  private DatasourceCreatedCallback datasourceCreatedCallback;

  private ComparedDatasourcesReportStepPresenter.Display compareDisplay;

  private ValidationHandler fileSelectionValidator;

  private ValidationHandler importableValidator;

  private ValidationHandler localeValidator;

  @Inject
  public VariablesImportView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWizardDialog();
    initializeLocales();
  }

  private void initWizardDialog() {
    spssPanel.setVisible(false);

    stepChain = WizardStepChain.Builder.create(dialog)//
        .append(fileSelectionStep, fileSelectionHelp)//
        .title(translations.variablesImportFileSelectionStep())//
        .onReset(new ResetHandler() {

          @Override
          public void onReset() {
            fileSelection.clearFile();
            datasources.setSelectedIndex(0);
            hideErrors();
          }
        })//

        .append(compareStep)//
        .title(translations.variablesImportCompareStep())//
        .help(new WidgetProvider() {

          @Override
          public Widget getWidget() {
            return compareDisplay.getStepHelp();
          }
        })//

        .append(conclusionStep)//
        .title(translations.variablesImportPending())//
        .conclusion()//

        .onPrevious().onCancel().onClose().build();
  }

  private void showErrors(ClientErrorDto errorDto) {
    if(errorDto != null && errorDto.getExtension(ClientErrorDtoExtensions.errors) != null) {
      failed.setVisible(true);
      datasourceParsingErrors.setErrors(errorDto);
      datasourceParsingErrors.setVisible(true);
    }
  }

  private void initializeLocales() {
    localeNameBox.getSuggestOracle().clear();

    for(String locale : LanguageLocale.getAllLocales()) {
      localeNameBox.getSuggestOracle().add(locale);
    }

    localeNameBox.setText(LanguageLocale.EN.getName());
  }

  @Override
  public void setSelectedDatasource(String dsName) {
    for(int i = 0; i < datasources.getItemCount(); i++) {
      if(datasources.getValue(i).equals(dsName)) {
        datasources.setSelectedIndex(i);
        break;
      }
    }
  }

  @Override
  public String getSelectedDatasource() {
    return datasources.getValue(datasources.getSelectedIndex());
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    this.datasources.clear();
    for(int i = 0; i < datasources.length(); i++) {
      this.datasources.addItem(datasources.get(i).getName());
    }
    this.datasources.setVisible(datasources.length() > 1);
    destinationLabel.setVisible(datasources.length() > 1);
  }

  public HandlerRegistration addDownloadExcelTemplateClickHandler(ClickHandler handler) {
    return downloadExcelTemplateButton.addClickHandler(handler);
  }

  @Override
  public void show() {
    stepChain.reset();
    super.show();
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
  public HandlerRegistration addFileSelectedClickHandler(final ClickHandler handler) {
    return dialog.addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(!localeValidator.validate()) return;

        if(fileSelectionStep.isVisible()) {
          if(fileSelectionValidator.validate()) {
            handler.onClick(evt);
            dialog.setProgress(true);
            dialog.setNextEnabled(false);
            dialog.setCancelEnabled(false);
          }
        } else stepChain.onNext();
      }
    });
  }

  @Override
  public HandlerRegistration addFinishClickHandler(final ClickHandler handler) {
    return dialog.addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(importableValidator.validate()) {
          handler.onClick(evt);
          stepChain.onNext();
          dialog.setCancelEnabled(false);
        }
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
  public void setFileSelectionValidator(ValidationHandler handler) {
    this.fileSelectionValidator = handler;
  }

  @Override
  public void setLocaleValidator(ValidationHandler handler) {
    this.localeValidator = handler;
  }

  @Override
  public void setImportableValidator(ValidationHandler handler) {
    this.importableValidator = handler;
  }

  @Override
  public void hideErrors() {
    failed.setVisible(false);
    datasourceParsingErrors.setVisible(false);
  }

  @Override
  public void setComparedDatasourcesReportDisplay(ComparedDatasourcesReportStepPresenter.Display display) {
    compareDisplay = display;
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
  public DatasourceCreatedCallback getDatasourceCreatedCallback() {
    if(datasourceCreatedCallback == null) {
      datasourceCreatedCallback = new DatasourceCreatedCallback() {

        @Override
        public void onSuccess(DatasourceFactoryDto factory, DatasourceDto datasource) {
          dialog.setCancelEnabled(true);
          dialog.setProgress(false);
          stepChain.onNext();
        }

        @Override
        public void onFailure(DatasourceFactoryDto factory, ClientErrorDto error) {
          showErrors(error);
          dialog.setProgress(false);
          dialog.setNextEnabled(true);
          dialog.setCancelEnabled(true);
        }
      };
    }
    return datasourceCreatedCallback;
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
    spssPanel.setVisible(show);
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("VariablesImportView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, VariablesImportView> {}

}
