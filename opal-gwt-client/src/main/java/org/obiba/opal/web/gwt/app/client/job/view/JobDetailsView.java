/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.job.view;

import java.util.Date;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.job.presenter.JobDetailsPresenter.Display;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.DateTimeColumn;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;
import org.obiba.opal.web.model.client.opal.CommandStateDto;
import org.obiba.opal.web.model.client.opal.Message;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

public class JobDetailsView extends PopupViewImpl implements Display {

  private static final String DIALOG_HEIGHT = "30em";

  private static final String DIALOG_WIDTH = "40em";

  @UiTemplate("JobDetailsView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, JobDetailsView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  DialogBox dialogBox;

  @UiField
  DockLayoutPanel content;

  @UiField
  Button closeButton;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  CellTable<Message> table;

  @Inject
  public JobDetailsView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    resizeHandle.makeResizable(content);
    content.setHeight(DIALOG_HEIGHT);
    content.setWidth(DIALOG_WIDTH);

    initTable();

    addDialogCloseHandler();
  }

  @Override
  public void setJob(CommandStateDto commandStateDto) {
    dialogBox.setText(translations.jobLabel() + " #" + commandStateDto.getId());

    JsArray<Message> jobMessages = JsArrays.toSafeArray(commandStateDto.getMessagesArray());

    table.setRowData(0, JsArrays.toList(jobMessages));
    table.setRowCount(jobMessages.length(), true);
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

  private void addDialogCloseHandler() {
    closeButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });
  }
}
