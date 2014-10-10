/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.celltable.DateTimeColumn;
import org.obiba.opal.web.model.client.opal.Message;

import java.util.Date;

public abstract class BaseMessagesView extends ModalPopupViewWithUiHandlers<ModalUiHandlers> {

  private static int DIALOG_HEIGHT = 400;

  private static final int DIALOG_WIDTH = 480;

  protected final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  public Modal dialogBox;

  @UiField
  public CellTable<Message> table;

  @Inject
  public BaseMessagesView(EventBus eventBus) {
    super(eventBus);
    widget =  createWidget();
    dialogBox.setMinHeight(DIALOG_HEIGHT);
    dialogBox.setMinWidth(DIALOG_WIDTH);

    initTable();
  }

  protected abstract Widget createWidget();

  protected void setData(JsArray<Message> messages) {
      JsArray<Message> msgs = JsArrays.toSafeArray(messages);
      table.setRowData(0, JsArrays.toList(msgs));
      table.setRowCount(msgs.length(), true);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  private void initTable() {
    addTableColumns();
  }

  private void addTableColumns() {
    table.addColumn(new DateTimeColumn<Message>() {
      @Override
      public Date getValue(Message object) {
        return new Date((long) object.getTimestamp());
      }
    }, translations.dateLabel());

    table.addColumn(new TextColumn<Message>() {
      @Override
      public String getValue(Message object) {
        return object.getMsg();
      }
    }, translations.messageLabel());
  }

  @UiHandler("closeButton")
  public void onCloseButton(ClickEvent event) {
    dialogBox.hide();
  }
}
