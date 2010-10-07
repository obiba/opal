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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter.Result;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListView;
import com.google.gwt.view.client.ListView.Delegate;

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
    // TODO Auto-generated method stub

  }

  @Override
  public void stopProcessing() {
    // TODO Auto-generated method stub

  }

  @Override
  public HandlerRegistration addTestScriptClickHandler(ClickHandler handler) {
    return testScript.addClickHandler(handler);
  }

  @Override
  public String getScript() {
    return scriptArea.getText();
  }

  @Override
  public void addResults(List<Result> results) {
    resultTable.setPageSize(10);
    SimplePager<Result> pager = new SimplePager<Result>(resultTable);
    populateResultsTable(results, resultTable, pager);
    testResults.add(pager);
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

  private <T extends Object> void populateResultsTable(final List<T> results, CellTable<T> table, SimplePager<T> pager) {

    table.setDelegate(new Delegate<T>() {

      @Override
      public void onRangeChanged(ListView<T> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, results.subList(start, length > results.size() ? results.size() : start + length));
      }

    });

    pager.firstPage();
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
    // TODO Get error from Dto and display it
    testResults.add(new Label("Script error!"));
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    scriptArea.setReadOnly(readOnly);
  }

  @Override
  public void setScript(String script) {
    scriptArea.setText(script);
  }
}
