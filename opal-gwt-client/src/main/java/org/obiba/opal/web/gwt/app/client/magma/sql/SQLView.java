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
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

public class SQLView extends ViewWithUiHandlers<SQLUiHandlers> implements SQLPresenter.Display {

  interface Binder extends UiBinder<Widget, SQLView> {
  }

  private final Translations translations;

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

  @UiField
  FlowPanel panel;

  private FormPanel form;

  private NamedFrame frame;

  private TextBox queryInput;

  private long startTime;

  private DatasourceDto datasource;

  @Inject
  public SQLView(Binder uiBinder, EventBus eventBus, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    query.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        boolean empty = Strings.isNullOrEmpty(query.getText().trim());
        execute.setEnabled(!empty);
        clear.setEnabled(!empty);
        if (!empty && keyUpEvent.isControlKeyDown() && keyUpEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          onExecute(null);
        }
      }
    });
    initDownloadWidgets();
  }

  @Override
  public void setDatasource(DatasourceDto datasource) {
    this.datasource = datasource;
    query.setText("");
  }

  @Override
  public void clear() {
    query.setText("");
    if (datasource != null && datasource.getTableArray().length() > 0) {
      String table = datasource.getTableArray().get(0);
      if (table.contains("."))
        table = "`" + table + "`";
      query.setPlaceholder("SELECT * FROM " + table + " LIMIT 10");
    } else {
      query.setPlaceholder("SELECT * FROM ? LIMIT 10");
    }
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
            JavaScriptObject val = row.get(pos);
            String valStr = val + ""; // because 0 is considered == null but concatenate correctly
            return val == null && "null".equals(valStr) ? "" : valStr;
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

      Button download = new Button();
      download.setType(ButtonType.INFO);
      download.setIcon(IconType.DOWNLOAD);
      download.setText(translations.downloadLabel());
      download.addClickHandler(new ClickHandler() {

        @Override
        public void onClick(ClickEvent clickEvent) {
          if (!Strings.isNullOrEmpty(queryInput.getText()))
            getUiHandlers().download();
        }
      });

      download.addStyleName("pull-left");
      resultPanel.add(download);
      pager.addStyleName("pull-right");
      resultPanel.add(pager);
      table.addStyleName("pull-left small-top-margin");
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
      execTime.setText("(" + NumberFormat.getDecimalFormat().format((System.currentTimeMillis() - startTime)) + " ms)");
      execTime.setVisible(true);
    }
  }

  @Override
  public void doDownload(String url) {
    form.setAction(url);
    form.setMethod(FormPanel.METHOD_POST);
    form.submit();
  }

  @UiHandler("execute")
  void onExecute(ClickEvent event) {
    String qStr = query.getText().trim();
    if (Strings.isNullOrEmpty(qStr)) return;
    getUiHandlers().execute(qStr);
    queryInput.setText(qStr);
  }

  @UiHandler("clear")
  void onClear(ClickEvent event) {
    clear();
  }

  private void initDownloadWidgets() {
    frame = new NamedFrame("frame");
    frame.setVisible(false);
    queryInput = new TextBox();
    queryInput.setName("query");
    form = new FormPanel(frame);
    form.add(queryInput);
    form.setVisible(false);
    panel.add(form);
    panel.add(frame);
  }
}
