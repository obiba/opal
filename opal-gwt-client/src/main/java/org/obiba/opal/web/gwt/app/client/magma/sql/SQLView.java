/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.sql;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;

public class SQLView extends ViewWithUiHandlers<SQLUiHandlers> implements SQLPresenter.Display {

  interface Binder extends UiBinder<Widget, SQLView> {
  }

  @UiField
  TextArea query;

  @UiField
  Button execute;

  @UiField
  Button clear;

  @UiField
  Alert errorAlert;

  @UiField
  Label errorMessage;

  @UiField
  Panel resultPanel;

  @UiField
  Image execPending;

  @UiField
  Label execTime;

  private long startTime;

  @Inject
  public SQLView(Binder uiBinder, EventBus eventBus) {
    initWidget(uiBinder.createAndBindUi(this));
    query.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        boolean empty = Strings.isNullOrEmpty(query.getText());
        execute.setEnabled(!empty);
        clear.setEnabled(!empty);
      }
    });
  }

  @Override
  public void clear() {
    query.setText("select * from CNSIM1");
    execute.setEnabled(true);
    clear.setEnabled(false);
    errorAlert.setVisible(false);
    errorMessage.setText("");
    resultPanel.clear();
    execPending.setVisible(false);
    execTime.setText("");
    execTime.setVisible(false);
  }

  @Override
  public void startExecute() {
    resultPanel.clear();
    errorAlert.setVisible(false);
    errorMessage.setText("");
    execute.setEnabled(false);
    clear.setEnabled(false);
    execPending.setVisible(true);
    execTime.setVisible(false);
    startTime = System.currentTimeMillis();
  }

  @Override
  public void showResult(SQLResult result) {
    if (result.hasError()) {
      errorMessage.setText(result.getError());
      errorAlert.setVisible(true);
    } else {
      Table<JsArray<JavaScriptObject>> table = new Table<JsArray<JavaScriptObject>>();
      for (int i = 0; i < result.getColumns().length(); i++) {
        final int pos = i;
        table.addColumn(new TextColumn<JsArray<JavaScriptObject>>() {
          @Override
          public String getValue(JsArray<JavaScriptObject> row) {
            return row.get(pos) != null ? row.get(pos).toString() : "";
          }
        }, result.getColumns().get(pos));
      }

      JsArrayDataProvider<JsArray<JavaScriptObject>> provider = new JsArrayDataProvider<JsArray<JavaScriptObject>>();
      provider.addDataDisplay(table);
      provider.setArray(result.getRows());
      provider.refresh();

      OpalSimplePager pager = new OpalSimplePager(SimplePager.TextLocation.RIGHT);
      table.setPageSize(20);
      pager.setDisplay(table);
      pager.setPagerVisible(provider.getList().size() > pager.getPageSize());

      pager.addStyleName("pull-right");
      table.addStyleName("pull-left small-top-margin");
      resultPanel.add(pager);
      resultPanel.add(table);
    }

  }

  @Override
  public void showError(String text) {
    errorMessage.setText(text);
    errorAlert.setVisible(true);
  }

  @Override
  public void endExecute() {
    execute.setEnabled(true);
    clear.setEnabled(true);
    execPending.setVisible(false);
    if (!errorAlert.isVisible()) {
      execTime.setText("(" + NumberFormat.getDecimalFormat().format(System.currentTimeMillis() - startTime) + " ms)");
      execTime.setVisible(true);
    }
  }

  @UiHandler("execute")
  void onExecute(ClickEvent event) {
    getUiHandlers().execute(query.getText());
  }

  @UiHandler("clear")
  void onClear(ClickEvent event) {
    clear();
  }
}
