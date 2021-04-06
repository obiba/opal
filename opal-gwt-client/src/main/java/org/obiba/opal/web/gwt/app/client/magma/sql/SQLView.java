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
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.ui.NavPillsPanel;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HTMLCell;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import java.util.List;

public class SQLView extends ViewWithUiHandlers<SQLUiHandlers> implements SQLPresenter.Display {

  interface Binder extends UiBinder<Widget, SQLView> {
  }

  private final Translations translations;

  @UiField
  TextArea queryText;

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

  @UiField
  Table<SQLHistoryEntry> queryTable;

  @UiField
  TextBoxClearable queryFilter;

  @UiField
  NavPillsPanel queryPanel;

  private ListDataProvider<SQLHistoryEntry> queryProvider = new ListDataProvider<>();

  private FormPanel form;

  private NamedFrame frame;

  private TextBox queryInput;

  private long startTime;

  private DatasourceDto datasource;

  private List<SQLHistoryEntry> queryList = Lists.newArrayList();

  @Inject
  public SQLView(Binder uiBinder, EventBus eventBus, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    queryText.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent keyUpEvent) {
        boolean empty = Strings.isNullOrEmpty(queryText.getText().trim());
        execute.setEnabled(!empty);
        clear.setEnabled(!empty);
        if (!empty && keyUpEvent.isControlKeyDown() && keyUpEvent.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          onExecute(null);
        }
      }
    });
    initDownloadWidgets();
    initQueryHistoryTable();
  }

  @Override
  public void setDatasource(DatasourceDto datasource) {
    this.datasource = datasource;
    queryText.setText("");
    refreshHistory();
    queryFilter.setText("");
    queryPanel.selectTab(0);
  }

  @Override
  public void showQuery(String queryStr) {
    onClear(null);
    queryText.setText(queryStr);
    queryPanel.selectTab(0);
  }

  @Override
  public void clear() {
    queryText.setText("");
    if (datasource != null && datasource.getTableArray().length() > 0) {
      String table = datasource.getTableArray().get(0);
      if (table.contains("."))
        table = "`" + table + "`";
      queryText.setPlaceholder("SELECT * FROM " + table + " LIMIT 10");
    } else {
      queryText.setPlaceholder("SELECT * FROM ? LIMIT 10");
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
    queryText.setEnabled(false);
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
    queryText.setEnabled(true);
    execute.setEnabled(true);
    clear.setEnabled(true);
    execPending.setVisible(false);
    long time = System.currentTimeMillis() - startTime;
    if (!errorAlert.isVisible()) {
      execTime.setText("(" + NumberFormat.getDecimalFormat().format(time) + " ms)");
      execTime.setVisible(true);
    }
    SQLHistoryEntry q = new SQLHistoryEntry((queryProvider.getList().size() + 1) + "", datasource.getName(),
        queryInput.getText(), errorMessage.getText(), time);
    queryList.add(0, q);
    queryFilter.setText("");
    refreshHistory();
  }

  private void refreshHistory() {
    List qList = Lists.newArrayList();
    String filter = queryFilter.getText().trim().toLowerCase();
    for (SQLHistoryEntry sq : queryList) {
      if (sq.getDatasource().equals(datasource.getName()))
        if (Strings.isNullOrEmpty(filter) || sq.getSql().toLowerCase().contains(filter))
          qList.add(sq);
    }
    queryProvider.setList(qList);
    queryProvider.refresh();
  }

  @Override
  public void doDownload(String url) {
    form.setAction(url);
    form.setMethod(FormPanel.METHOD_POST);
    form.submit();
  }

  @UiHandler("execute")
  void onExecute(ClickEvent event) {
    String qStr = queryText.getText().trim();
    if (Strings.isNullOrEmpty(qStr)) return;
    getUiHandlers().execute(qStr);
    queryInput.setText(qStr);
  }

  @UiHandler("clear")
  void onClear(ClickEvent event) {
    clear();
  }

  @UiHandler("queryFilter")
  public void onQueryFilterUpdate(KeyUpEvent event) {
    refreshHistory();
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

  private void initQueryHistoryTable() {
    queryTable.addColumn(new TextColumn<SQLHistoryEntry>() {
      @Override
      public String getValue(SQLHistoryEntry sqlHistoryEntry) {
        return sqlHistoryEntry.getId();
      }
    }, "#");
    queryTable.addColumn(new SQLQueryColumn(), translations.queryLabel());
    queryTable.addColumn(new TextColumn<SQLHistoryEntry>() {
      @Override
      public String getValue(SQLHistoryEntry sqlHistoryEntry) {
        return NumberFormat.getDecimalFormat().format(sqlHistoryEntry.getTime()) + " ms";
      }
    }, translations.timeLabel());
    SQLQueryActionsColumn actionsColumn = new SQLQueryActionsColumn();
    actionsColumn.setActionHandler(new ActionHandler<SQLHistoryEntry>() {
      @Override
      public void doAction(SQLHistoryEntry sqlHistoryEntry, String actionName) {
        if (ActionsColumn.EDIT_ACTION.equals(actionName)) {
          onClear(null);
          queryText.setText(sqlHistoryEntry.getSql());
        } else {
          queryText.setText(sqlHistoryEntry.getSql());
          onExecute(null);
        }
        queryPanel.selectTab(0);
      }
    });
    queryTable.addColumn(actionsColumn, translations.actionsLabel());
    queryTable.setColumnWidth(0, "40px");
    queryProvider.addDataDisplay(queryTable);
  }

  private static class SQLQueryActionsColumn extends ActionsColumn<SQLHistoryEntry> {
    public SQLQueryActionsColumn() {
      super(new ActionsProvider<SQLHistoryEntry>() {
        @Override
        public String[] allActions() {
          return new String[]{ActionsColumn.EDIT_ACTION, "Execute"};
        }

        @Override
        public String[] getActions(SQLHistoryEntry value) {
          if (value.isError())
            return new String[]{ActionsColumn.EDIT_ACTION};
          else
            return allActions();
        }
      });
    }
  }

  private class SQLQueryColumn extends Column<SQLHistoryEntry, String> {

    public SQLQueryColumn() {
      super(new HTMLCell());
    }

    @Override
    public String getValue(SQLHistoryEntry sqlHistoryEntry) {
      if (sqlHistoryEntry.isError())
        return "<span style=\"color: red\" title=\"" + sqlHistoryEntry.getSafeHtmlError() + "\">" + sqlHistoryEntry.getSql() + "</span>";
      else
        return sqlHistoryEntry.getSql();
    }
  }
}
