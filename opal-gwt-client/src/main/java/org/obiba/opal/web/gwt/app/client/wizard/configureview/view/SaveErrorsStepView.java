/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import java.util.Collections;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.SaveErrorsStepPresenter;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;

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
import com.google.gwt.view.client.ListDataProvider;

public class SaveErrorsStepView extends Composite implements SaveErrorsStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  Button backButton;

  @UiField
  CellTable<JavaScriptErrorDto> errorsTable;

  ListDataProvider<JavaScriptErrorDto> dataProvider = new ListDataProvider<JavaScriptErrorDto>();

  //
  // Constructors
  //

  public SaveErrorsStepView() {
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  //
  // SaveErrorsStepPresenter.Display Methods
  //

  public void clear() {
    List<JavaScriptErrorDto> noErrors = Collections.emptyList();
    setErrors(noErrors);
  }

  public void setErrors(final List<JavaScriptErrorDto> errors) {
    dataProvider.setList(errors);
  }

  public HandlerRegistration addBackClickHandler(ClickHandler handler) {
    return backButton.addClickHandler(handler);
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
    addTableColumns();
    dataProvider.addDataDisplay(errorsTable);
  }

  private void addTableColumns() {
    errorsTable.addColumn(new TextColumn<JavaScriptErrorDto>() {
      @Override
      public String getValue(JavaScriptErrorDto dto) {
        return dto.getSourceName();
      }
    }, "Script");// translations.scriptLabel());

    errorsTable.addColumn(new TextColumn<JavaScriptErrorDto>() {
      @Override
      public String getValue(JavaScriptErrorDto dto) {
        return "" + dto.getLineNumber();
      }
    }, "Line");// translations.lineLabel());

    errorsTable.addColumn(new TextColumn<JavaScriptErrorDto>() {
      @Override
      public String getValue(JavaScriptErrorDto dto) {
        return dto.getMessage();
      }
    }, translations.errorLabel());
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("SaveErrorsStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, SaveErrorsStepView> {
  }
}
