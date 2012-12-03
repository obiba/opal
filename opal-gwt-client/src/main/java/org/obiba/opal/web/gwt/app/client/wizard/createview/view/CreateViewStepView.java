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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;
import com.watopi.chosen.client.gwt.ChosenListBox;

public class CreateViewStepView extends PopupViewImpl implements CreateViewStepPresenter.Display {

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep selectTypeStep;

  @UiField
  WizardStep tablesStep;

  @UiField(provided = true)
  ChosenListBox tableChosen;

  @UiField
  WizardStep conclusionStep;

  @UiField
  HTMLPanel selectTypeHelp;

  @UiField
  HTMLPanel tablesHelp;

  @UiField
  TextBox viewNameTextBox;

  @UiField
  RadioButton addingVariablesOneByOneRadioButton;

  @UiField
  RadioButton useAnExistingView;

  @UiField
  RadioButton useAnExcelFile;

  @UiField
  SimplePanel xmlFileSelectionPanel;

  @UiField
  SimplePanel excelFileSelectionPanel;

  @UiField
  Button configureLink;

  private FileSelectionPresenter.Display fileSelection;

  private WizardStepChain stepChain;

  private ValidationHandler selectTypeValidator;

  private ValidationHandler tablesValidator;

  private Map<String, TableDto> tableDtoMap = new HashMap<String, TableDto>();

  @Inject
  public CreateViewStepView(EventBus eventBus) {
    super(eventBus);
    tableChosen = new ChosenListBox(true);
    this.widget = uiBinder.createAndBindUi(this);
    initWizardDialog();

    ValueChangeHandler<Boolean> handler = new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        Widget w = fileSelection.asWidget();
        excelFileSelectionPanel.setVisible(useAnExcelFile.getValue());
        if(useAnExcelFile.getValue()) {
          excelFileSelectionPanel.setWidget(w);
        } else {
          xmlFileSelectionPanel.setVisible(true);
          xmlFileSelectionPanel.setWidget(w);
        }
        fileSelection.setEnabled(useAnExistingView.getValue() || useAnExcelFile.getValue());
      }
    };
    addingVariablesOneByOneRadioButton.addValueChangeHandler(handler);
    useAnExistingView.addValueChangeHandler(handler);
    useAnExcelFile.addValueChangeHandler(handler);
    tableChosen.setPlaceholderText(translations.selectSomeTables());
    tableChosen.setSearchContains(true);
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
            addingVariablesOneByOneRadioButton.setValue(true, true);
          }
        })//

        .append(tablesStep, tablesHelp)//
        .title(translations.editViewTablesStep())//
        .onReset(new ResetHandler() {

          @Override
          public void onReset() {
            tableChosen.clear();
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
  public void setFileSelectionDisplay(FileSelectionPresenter.Display display) {
    xmlFileSelectionPanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
    fileSelection.setEnabled(false);
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
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(final ClickHandler handler) {
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
  public HandlerRegistration addCloseClickHandler(final ClickHandler handler) {
    return dialog.addCloseClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        handler.onClick(evt);
      }
    });
  }

  @Override
  public HandlerRegistration addConfigureHandler(ClickHandler handler) {
    return configureLink.addClickHandler(handler);
  }

  @Override
  public void addTableSelections(JsArray<TableDto> tables) {
    tableChosen.clear();
    tableDtoMap.clear();
    HashMap<String, List<TableDto>> datasourceMap = new LinkedHashMap<String, List<TableDto>>();
    for(TableDto table : JsArrays.toIterable(tables)) {
      if(datasourceMap.containsKey(table.getDatasourceName()) == false) {
        datasourceMap.put(table.getDatasourceName(), new ArrayList<TableDto>());
      }
      datasourceMap.get(table.getDatasourceName()).add(table);
    }

    for(String ds : datasourceMap.keySet()) {
      tableChosen.addGroup(ds);
      for(TableDto table : datasourceMap.get(ds)) {
        String fullName = table.getDatasourceName() + "." + table.getName();
        tableChosen.addItemToGroup(fullName, fullName);
        tableDtoMap.put(fullName, table);
      }
      tableChosen.update();
    }
  }

  @Override
  public List<TableDto> getSelectedTables() {
    List<TableDto> tables = new ArrayList<TableDto>();
    for(int i = 0; i < tableChosen.getItemCount(); i++) {
      if(tableChosen.isItemSelected(i)) {
        tables.add(tableDtoMap.get(tableChosen.getValue(i)));
      }
    }
    return tables;
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
  public HasValue<Boolean> getExcelFileOption() {
    return this.useAnExcelFile;
  }

  @Override
  public void show() {
    clear();
    super.show();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

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

}
