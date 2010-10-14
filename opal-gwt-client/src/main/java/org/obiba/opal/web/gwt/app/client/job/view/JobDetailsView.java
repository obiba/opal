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
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 *
 */
public class JobDetailsView extends DialogBox implements Display {

  private static final String DIALOG_HEIGHT = "30em";

  private static final String DIALOG_WIDTH = "40em";

  @UiTemplate("JobDetailsView.ui.xml")
  interface JobDetailsViewUiBinder extends UiBinder<DockLayoutPanel, JobDetailsView> {
  }

  private static JobDetailsViewUiBinder uiBinder = GWT.create(JobDetailsViewUiBinder.class);

  @UiField
  CellTable<Message> table;

  @UiField
  Button close;

  SelectionModel<Message> selectionModel = new SingleSelectionModel<Message>();

  private Translations translations = GWT.create(Translations.class);

  //
  // Constructors
  //

  public JobDetailsView() {
    setHeight(DIALOG_HEIGHT);
    setWidth(DIALOG_WIDTH);

    DockLayoutPanel content = uiBinder.createAndBindUi(this);
    content.setHeight(DIALOG_HEIGHT);
    content.setWidth(DIALOG_WIDTH);
    add(content);

    initTable();

    addDialogCloseHandler();
  }

  //
  // JobListPresenter.Display Methods
  //

  public HasCloseHandlers<PopupPanel> getDialogBox() {
    return this;
  }

  @SuppressWarnings("unchecked")
  public void showDialog(CommandStateDto commandStateDto) {
    setText(translations.jobLabel() + " #" + commandStateDto.getId());

    JsArray<Message> jobMessages = commandStateDto.getMessagesArray();
    if(jobMessages == null) {
      jobMessages = (JsArray<Message>) JsArray.createArray();
    }

    table.setData(0, jobMessages.length(), JsArrays.toList(jobMessages));
    table.setDataSize(jobMessages.length(), true);

    center();
    show();
  }

  public void hideDialog() {
    hide();
  }

  public Widget asWidget() {
    return null;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  //
  // Methods
  //

  private void initTable() {
    table.setSelectionEnabled(false);
    table.setSelectionModel(selectionModel);

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
        hideDialog();
      }
    });
  }
}
