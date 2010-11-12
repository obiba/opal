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
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter.Result;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EvaluateScriptView extends Composite implements EvaluateScriptPresenter.Display {

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  TextArea scriptArea;

  @UiField
  Button testScript;

  @UiField
  VerticalPanel testResults;

  @UiField
  DisclosurePanel resultsPanel;

  @UiField
  Anchor previousPage;

  @UiField
  Anchor nextPage;

  @UiField
  Label pageItemType;

  @UiField
  Label pageLimit;

  @UiField
  HTMLPanel paging;

  CellTable<Result> resultTable;

  public EvaluateScriptView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiTemplate("EvaluateScriptView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, EvaluateScriptView> {
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

  @Override
  public HandlerRegistration addTestScriptClickHandler(ClickHandler handler) {
    return testScript.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addScriptChangeHandler(ChangeHandler handler) {
    return scriptArea.addChangeHandler(handler);
  }

  @Override
  public String getScript() {
    return scriptArea.getText();
  }

  @Override
  public void addResults(List<Result> results) {
    populateResultsTable(results, resultTable);
    testResults.add(resultTable);
  }

  @Override
  public void clearResults() {
    testResults.clear();
  }

  @Override
  public void setTestVariableCount(int count) {
    testResults.add(new Label("Variables: " + count));
  }

  @Override
  public void setTestEntityCount(int count) {
    testResults.add(new Label("Entities: " + count));
  }

  private <T extends Object> void populateResultsTable(final List<T> results, CellTable<T> table) {
    table.setData(0, results.size(), results);
    table.setDataSize(results.size(), true);
    table.redraw();
  }

  @Override
  public String getSelectedScript() {
    return scriptArea.getSelectedText();
  }

  @Override
  public void setSelectedScript(String script) {
    int start = scriptArea.getText().indexOf(script);
    if(start > -1) {
      scriptArea.setSelectionRange(start, script.length());
    }
  }

  @Override
  public void initializeResultTable() {
    resultTable = new CellTable<EvaluateScriptPresenter.Result>();
    resultTable.setWidth("100%");
  }

  @Override
  public void addVariableColumn() {
    resultTable.addColumn(new TextColumn<Result>() {
      @Override
      public String getValue(Result result) {
        return result.getVariable().getName();
      }
    }, translations.variableLabel());
  }

  @Override
  public void addValueColumn() {
    resultTable.addColumn(new TextColumn<Result>() {
      @Override
      public String getValue(Result result) {
        return result.getValue().getValue();
      }
    }, translations.valueLabel());
  }

  @Override
  public void showResults(boolean visible) {
    resultsPanel.setOpen(visible);
  }

  @Override
  public void showErrorMessage(ClientErrorDto errorDto) {
    if(errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
      List<JavaScriptErrorDto> errors = extractJavaScriptErrors(errorDto);
      for(JavaScriptErrorDto error : errors) {
        testResults.add(new Label("Error at line " + error.getLineNumber() + ", column " + error.getColumnNumber() + ": " + error.getMessage()));
      }
    }
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    scriptArea.setReadOnly(readOnly);
  }

  @Override
  public void setScript(String script) {
    scriptArea.setText(script);
  }

  @SuppressWarnings("unchecked")
  private List<JavaScriptErrorDto> extractJavaScriptErrors(ClientErrorDto errorDto) {
    List<JavaScriptErrorDto> javaScriptErrors = new ArrayList<JavaScriptErrorDto>();

    JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors);
    if(errors != null) {
      for(int i = 0; i < errors.length(); i++) {
        javaScriptErrors.add(errors.get(i));
      }
    }

    return javaScriptErrors;
  }

  @Override
  public HandlerRegistration addNextPageClickHandler(ClickHandler handler) {
    return nextPage.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addPreviousPageClickHandler(ClickHandler handler) {
    return previousPage.addClickHandler(handler);
  }

  @Override
  public void showPaging(boolean visible) {
    paging.setVisible(visible);
  }

  @Override
  public void setPaging(int start, int end) {
    pageLimit.setText("(" + start + " " + translations.ofLabel() + " " + end + ")");
  }

  @Override
  public void setItemTypeVariables() {
    pageItemType.setText(translations.variablesLabel());

  }

  @Override
  public void setItemTypeValues() {
    pageItemType.setText(translations.valuesLabel());
  }

  @Override
  public HandlerRegistration addResultsOpenHandler(OpenHandler openHandler) {
    return resultsPanel.addOpenHandler(openHandler);
  }

  @Override
  public HandlerRegistration addResultsCloseHandler(CloseHandler closeHandler) {
    return resultsPanel.addCloseHandler(closeHandler);
  }
}
