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
import org.obiba.opal.web.gwt.user.cellview.client.DateTimeColumn;
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
  interface JobDetailsViewUiBinder extends UiBinder<DockLayoutPanel, JobDetailsView> {
  }

  private static JobDetailsViewUiBinder uiBinder = GWT.create(JobDetailsViewUiBinder.class);

  private Translations translations = GWT.create(Translations.class);

  private final DialogBox dialog;

  @UiField
  CellTable<Message> table;

  @UiField
  Button close;

  @Inject
  public JobDetailsView(EventBus eventBus) {
    super(eventBus);
    dialog = new DialogBox();
    dialog.setModal(true);
    dialog.setGlassEnabled(true);

    DockLayoutPanel content = uiBinder.createAndBindUi(this);
    content.setHeight(DIALOG_HEIGHT);
    content.setWidth(DIALOG_WIDTH);
    dialog.add(content);

    initTable();

    addDialogCloseHandler();
  }

  @Override
  public void setJob(CommandStateDto commandStateDto) {
    dialog.setText(translations.jobLabel() + " #" + commandStateDto.getId());

    JsArray<Message> jobMessages = JsArrays.toSafeArray(commandStateDto.getMessagesArray());

    table.setRowData(0, JsArrays.toList(jobMessages));
    table.setRowCount(jobMessages.length(), true);
  }

  @Override
  public Widget asWidget() {
    return dialog;
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
    close.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });
  }
}
