/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CreateViewStepView extends Composite implements CreateViewStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep selectTypeStep;

  @UiField
  WizardStep tablesStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  HTMLPanel selectTypeHelp;

  @UiField
  HTMLPanel tablesHelp;

  @UiField
  SimplePanel tableSelectorPanel;

  @UiField
  TextBox viewNameTextBox;

  @UiField
  RadioButton applyingGlobalVariableFilterRadioButton;

  @UiField
  RadioButton addingVariablesOneByOneRadioButton;

  @UiField
  RadioButton useAnExistingView;

  @UiField
  SimplePanel fileSelectionPanel;

  @UiField
  Button configureLink;

  private TableListPresenter.Display tableSelector;

  private FileSelectionPresenter.Display fileSelection;

  private WizardStepChain stepChain;

  private ValidationHandler selectTypeValidator;

  private ValidationHandler tablesValidator;

  //
  // Constructors
  //

  public CreateViewStepView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWizardDialog();

    ValueChangeHandler<Boolean> handler = new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        fileSelection.setEnabled(useAnExistingView.getValue());
      }
    };
    applyingGlobalVariableFilterRadioButton.addValueChangeHandler(handler);
    addingVariablesOneByOneRadioButton.addValueChangeHandler(handler);
    useAnExistingView.addValueChangeHandler(handler);
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//

    .append(selectTypeStep, selectTypeHelp)//
    .title(translations.editViewTypeStep())//
    .onValidate(new ValidationHandler() {

      @Override
      public boolean validate() {
        return selectTypeValidator.validate();
      }
    })//
    .onReset(new ResetHandler() {

      @Override
      public void onReset() {
        viewNameTextBox.setText("");
        applyingGlobalVariableFilterRadioButton.setValue(true);
        ValueChangeEvent.fire(applyingGlobalVariableFilterRadioButton, true);
      }
    })//

    .append(tablesStep, tablesHelp)//
    .title(translations.editViewTablesStep())//
    .onReset(new ResetHandler() {

      @Override
      public void onReset() {
        if(tableSelector != null) tableSelector.clear();
      }
    })//

    .append(conclusionStep)//
    .conclusion()//
    .onReset(new ResetHandler() {

      @Override
      public void onReset() {
        conclusionStep.setStepTitle(translations.addViewPending()); //
        configureLink.setVisible(false);
      }
    })

    .onNext().onPrevious().onClose().build();
  }

  //
  // CreateViewStepPresenter.Display Methods
  //
  @Override
  public void clear() {
    stepChain.reset();
  }

  @Override
  public void setTableSelector(TableListPresenter.Display tableSelector) {
    this.tableSelector = tableSelector;
    tableSelectorPanel.add(tableSelector.asWidget());
  }

  @Override
  public void setFileSelectionDisplay(FileSelectionPresenter.Display display) {
    fileSelectionPanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public HasText getViewName() {
    return viewNameTextBox;
  }

  @Override
  public void setSelectTypeValidator(ValidationHandler validator) {
    this.selectTypeValidator = validator;
  }

  @Override
  public void setTablesValidator(ValidationHandler validator) {
    this.tablesValidator = validator;
  }

  @Override
  public HandlerRegistration addCancelHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCreateHandler(final ClickHandler handler) {
    return dialog.addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(tablesValidator.validate()) {
          handler.onClick(evt);
        }
      }
    });
  }

  @Override
  public HandlerRegistration addConfigureHandler(ClickHandler handler) {
    return configureLink.addClickHandler(handler);
  }

  @Override
  public HasValue<Boolean> getApplyGlobalVariableFilterOption() {
    return applyingGlobalVariableFilterRadioButton;
  }

  @Override
  public HasValue<Boolean> getAddVariablesOneByOneOption() {
    return addingVariablesOneByOneRadioButton;
  }

  @Override
  public HasValue<Boolean> getFileViewOption() {
    return this.useAnExistingView;
  }

  @Override
  public void showDialog() {
    clear();
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("CreateViewStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CreateViewStepView> {
  }

  @Override
  public void renderPendingConclusion() {
    stepChain.onNext();
    conclusionStep.setStepTitle(translations.addViewPending());
    dialog.setCancelEnabled(false);
    dialog.setCloseEnabled(false);
    dialog.setProgress(true);
  }

  @Override
  public void renderCompletedConclusion() {
    dialog.setProgress(false);
    dialog.setCloseEnabled(true);
    conclusionStep.setStepTitle(translations.addViewSuccess());
    configureLink.setVisible(true);
  }

  @Override
  public void renderFailedConclusion(String msg) {
    dialog.setProgress(false);
    dialog.setCancelEnabled(true);
    conclusionStep.setStepTitle(translations.addViewFailed());
  }

  @Override
  public HandlerRegistration addCloseHandler(final ClickHandler handler) {
    return dialog.addCloseClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        handler.onClick(evt);
      }
    });
  }

}
