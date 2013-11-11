/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.createview.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.createview.presenter.CreateViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.createview.presenter.CreateViewModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.TableChooser;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class CreateViewModalView extends ModalPopupViewWithUiHandlers<CreateViewModalUiHandlers>
    implements CreateViewModalPresenter.Display {

  interface Binder extends UiBinder<Widget, CreateViewModalView> {}

  @UiField
  Modal dialog;

  @UiField(provided = true)
  TableChooser tableChooser;

  @UiField
  TextBox viewNameTextBox;

  @UiField
  SimplePanel fileSelectionPanel;

  private final Translations translations;

  private FileSelectionPresenter.Display fileSelection;

  private ValidationHandler selectTypeValidator;

  private ValidationHandler tablesValidator;

  @Inject
  public CreateViewModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    tableChooser = new TableChooser(true);
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.addViewLabel());

//    ValueChangeHandler<Boolean> handler = new ValueChangeHandler<Boolean>() {
//      @Override
//      public void onValueChange(ValueChangeEvent<Boolean> event) {
//        Widget w = fileSelection.asWidget();
//        excelFileSelectionPanel.setVisible(useAnExcelFile.getValue());
//        if(useAnExcelFile.getValue()) {
//          excelFileSelectionPanel.setWidget(w);
//        } else {
//          fileSelectionPanel.setVisible(true);
//          fileSelectionPanel.setWidget(w);
//        }
//        fileSelection.setEnabled(useAnExistingView.getValue() || useAnExcelFile.getValue());
//      }
//    };
//    addingVariablesOneByOneRadioButton.addValueChangeHandler(handler);
//    useAnExistingView.addValueChangeHandler(handler);
//    useAnExcelFile.addValueChangeHandler(handler);
  }

  @Override
  protected Modal asModal() {
    return dialog;
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
    selectTypeValidator = validator;
  }

  @Override
  public void setTablesValidator(ValidationHandler validator) {
    tablesValidator = validator;
  }
//
//  @Override
//  public HandlerRegistration addFinishClickHandler(final ClickHandler handler) {
//    return dialog.addFinishClickHandler(new ClickHandler() {
//
//      @Override
//      public void onClick(ClickEvent evt) {
//        if(tablesValidator.validate()) {
//          handler.onClick(evt);
//        }
//      }
//    });
//  }

  @Override
  public void addTableSelections(JsArray<TableDto> tables) {
    tableChooser.addTableSelections(tables);
  }

  @Override
  public List<TableDto> getSelectedTables() {
    return tableChooser.getSelectedTables();
  }

  @Override
  public void closeDialog() {
    dialog.hide();
  }

  @UiHandler("cancelButton")
  public void onCancelButtonClicked(ClickEvent event) {
    closeDialog();
  }

  @UiHandler("saveButton")
  public void onCreateButtonClicked(ClickEvent event) {
    getUiHandlers().createView();
  }

//
//  @Override
//  public HasValue<Boolean> getFileViewOption() {
//    return useAnExistingView;
//  }
//
//  @Override
//  public HasValue<Boolean> getExcelFileOption() {
//    return useAnExcelFile;
//  }


}
