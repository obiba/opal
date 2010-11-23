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
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ConclusionStepPresenter.TableCompareError;
import org.obiba.opal.web.gwt.app.client.workbench.view.DatasourceParsingErrorTable;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.ListView.Delegate;

public class ValidationReportStepView extends Composite {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  CellTable<TableCompareError> validationTable;

  @UiField
  DatasourceParsingErrorTable parsingErrorTable;

  @UiField
  Label validationLabel;

  //
  // Constructors
  //

  public ValidationReportStepView() {
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  //
  // Methods
  //

  public void showTableCompareErrors(final List<TableCompareError> errors) {
    validationLabel.setVisible(true);
    validationTable.setVisible(true);
    validationTable.setDelegate(new Delegate<TableCompareError>() {

      @Override
      public void onRangeChanged(ListView<TableCompareError> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, errors);
      }
    });

    validationTable.setData(0, validationTable.getPageSize(), errors);
    validationTable.setDataSize(errors.size(), true);
    validationTable.redraw();
  }

  public void showDatasourceParsingErrors(ClientErrorDto errorDto) {
    parsingErrorTable.setVisible(true);
    parsingErrorTable.setErrors(errorDto);
  }

  public void hideErrors() {
    validationLabel.setVisible(false);
    validationTable.setVisible(false);
    parsingErrorTable.setVisible(false);
  }

  private void initTable() {
    validationTable.setSelectionEnabled(false);
    addValidationColumns();
  }

  private void addValidationColumns() {
    validationTable.addColumn(new TextColumn<TableCompareError>() {

      @Override
      public String getValue(TableCompareError error) {
        return error.getColumn();
      }

    }, "variable");

    validationTable.addColumn(new TextColumn<TableCompareError>() {

      @Override
      public String getValue(TableCompareError error) {
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
