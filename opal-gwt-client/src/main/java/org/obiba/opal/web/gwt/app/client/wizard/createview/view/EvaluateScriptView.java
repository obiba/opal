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
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.EvaluateScriptPresenter;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenHandler;
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

  @UiField
  Label testCount;

  public EvaluateScriptView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  public Widget asWidget() {
    return this;
  }

  @UiTemplate("EvaluateScriptView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, EvaluateScriptView> {
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

  public HandlerRegistration addShowResultsPanelHandler(OpenHandler handler) {
    return resultsPanel.addOpenHandler(handler);
  }

  public HandlerRegistration addHideResultsPanelHandler(CloseHandler handler) {
    return resultsPanel.addCloseHandler(handler);
  }

  @Override
  public void showTestCount(boolean show) {
    testCount.setVisible(show);
  }

  @Override
  public String getScript() {
    String script = scriptArea.getSelectedText();
    if(script.equals("")) {
      script = scriptArea.getText();
    }
    return script;
  }

  @Override
  public void addResults(JsArray<VariableDto> variables) {
    CellTable<VariableDto> table = setupColumns();
    SimplePager<VariableDto> pager = new SimplePager<VariableDto>(table);
    populateResultsTable(variables, table, pager);
    testResults.add(pager);
    testResults.add(table);
  }

  @Override
  public void clearResults() {
    testResults.clear();
  }

  @Override
  public void setTestCount(int count) {
    testCount.setText("Count: " + count);
  }

  private <T extends JavaScriptObject> void populateResultsTable(final JsArray<T> results, CellTable<T> table, SimplePager<T> pager) {
    table.setDelegate(new Delegate<T>() {

      @Override
      public void onRangeChanged(ListView<T> listView) {
        int start = listView.getRange().getStart();
        int length = listView.getRange().getLength();
        listView.setData(start, length, JsArrays.toList(results, start, length));
      }

    });

    pager.firstPage();
    table.setPageSize(10);
    table.setPager(pager);
    table.setDataSize(results.length(), true);
    table.setData(0, table.getPageSize(), JsArrays.toList(results, 0, table.getPageSize()));
    table.redraw();
  }

  private CellTable<VariableDto> setupColumns() {
    CellTable<VariableDto> table = new CellTable<VariableDto>();
    table.addColumn(new TextColumn<VariableDto>() {
      @Override
      public String getValue(VariableDto variable) {
        return variable.getName();
      }
    }, translations.variableLabel());

    return table;
  }

}
