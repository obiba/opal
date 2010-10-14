/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationCloseHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * View used to display error, warning and info messages in a dialog box.
 */
public class NotificationView extends Composite implements NotificationPresenter.Display {

  @UiTemplate("NotificationView.ui.xml")
  interface ViewUiBinder extends UiBinder<PopupPanel, NotificationView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  PopupPanel dialog;

  @UiField
  Label caption;

  @UiField
  VerticalPanel messagePanel;

  @UiField
  Anchor okay;

  public NotificationView() {
    uiBinder.createAndBindUi(this);

    dialog.setGlassEnabled(false);

    messagePanel.setSpacing(5);

    okay.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    // Error dialog is initially hidden.
    dialog.hide();
  }

  @Override
  public void showPopup() {
    dialog.setPopupPosition(Window.getClientWidth() - 350, 50);
    dialog.show();
  }

  @Override
  public Widget asWidget() {
    return null;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void setMessages(List<String> errors) {
    messagePanel.clear();
    for(String error : errors) {
      messagePanel.add(new Label(error));
    }
  }

  @Override
  public void setCaption(String txt) {
    caption.setText(txt);
  }

  @Override
  public void setNotificationType(NotificationType type) {
    dialog.addStyleName(type.toString().toLowerCase());
  }

  @Override
  public void addNotificationCloseHandler(final NotificationCloseHandler handler) {
    dialog.addCloseHandler(new CloseHandler<PopupPanel>() {

      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        handler.onClose(event);
      }
    });
  }

}
