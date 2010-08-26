/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DestinationSelectionStepPresenter.CsvValidationError;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ValidationReportStepPresenter;
import org.obiba.opal.web.model.client.ws.DatasourceParsingErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.ListView.Delegate;

public class ValidationReportStepView extends Composite implements ValidationReportStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  Button cancelButton;

  @UiField
  CellTable<DatasourceParsingErrorDto> parsingTable;

  @UiField
  CellTable<CsvValidationError> validationTable;

  //
  // Constructors
  //

  public ValidationReportStepView() {
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  //
  // UploadVariablesStepPresenter.Display Methods
  //

  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return cancelButton.addClickHandler(handler);
  }

  public void setParsingErrors(final List<DatasourceParsingErrorDto> errors) {
    validationTable.setVisible(false);
    parsingTable.setVisible(true);
    parsingTable.setDelegate(new Delegate<DatasourceParsingErrorDto>() {

      public void onRangeChanged(ListView<DatasourceParsingErrorDto> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, errors);
      }
    });

    parsingTable.setData(0, parsingTable.getPageSize(), errors);
    parsingTable.setDataSize(errors.size(), true);
    parsingTable.redraw();
  }

  public void setValidationErrors(final List<CsvValidationError> errors) {
    parsingTable.setVisible(false);
    validationTable.setVisible(true);
    validationTable.setDelegate(new Delegate<CsvValidationError>() {

      @Override
      public void onRangeChanged(ListView<CsvValidationError> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, errors);
      }
    });

    validationTable.setData(0, validationTable.getPageSize(), errors);
    validationTable.setDataSize(errors.size(), true);
    validationTable.redraw();
  }

  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  //
  // Methods
  //

  private void initTable() {
    parsingTable.setSelectionEnabled(false);
    addParsingTableColumns();
    validationTable.setSelectionEnabled(false);
    addValidationColumns();
  }

  private void addParsingTableColumns() {

    parsingTable.addColumn(new TextColumn<DatasourceParsingErrorDto>() {
      @Override
      public String getValue(DatasourceParsingErrorDto dto) {
        return translations.datasourceParsingErrorMap().get(dto.getKey());
      }
    }, translations.errorLabel());
  }

  private void addValidationColumns() {
    validationTable.addColumn(new TextColumn<CsvValidationError>() {

      @Override
      public String getValue(CsvValidationError error) {
        return error.getColumn();
      }

    }, "variable");

    validationTable.addColumn(new TextColumn<CsvValidationError>() {

      @Override
      public String getValue(CsvValidationError error) {
        return error.getErrorMessageKey();
      }

    }, translations.errorLabel());
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("ValidationReportStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ValidationReportStepView> {
  }
}
