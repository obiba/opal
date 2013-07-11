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
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationCloseHandler;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.view.FadeAnimation.FadedHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 * View used to display error, warning and info messages in a dialog box.
 */
public class NotificationView extends PopupViewImpl implements NotificationPresenter.Display {

  @UiTemplate("NotificationView.ui.xml")
  interface ViewUiBinder extends UiBinder<PopupPanel, NotificationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  PopupPanel dialog;

  @UiField
  InlineLabel caption;

  @UiField
  VerticalPanel messagePanel;

  @UiField
  Anchor okay;

  @UiField
  ResizeHandle resizeHandleSouth;

  @UiField
  DockLayoutPanel contentLayout;

  private boolean sticky = true;

  private Timer nonStickyTimer;

  @Inject
  public NotificationView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);

    // Error dialog is initially hidden.
    dialog.hide();
    dialog.setGlassEnabled(false);

    messagePanel.setSpacing(5);

    okay.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    resizeHandleSouth.makeResizable(contentLayout, 300, 100);
  }

  @Override
  public void show() {
    // reset the dimensions in the eventuallity of a resize
    contentLayout.setSize("300px", "100px");
    dialog.setPopupPosition(Window.getClientWidth() - 350, 50);
    FadeAnimation.create(dialog.getElement()).from(0).to(0.85).start();
    dialog.show();
    if(sticky) {
      nonStickyTimer = null;
    } else {
      nonStickyTimer = new Timer() {

        @Override
        public void run() {
          if(dialog.isShowing()) {
            FadeAnimation.create(dialog.getElement()).from(0.85).to(0).then(new FadedHandler() {

              @Override
              public void onFaded(Element element) {
                dialog.hide();
              }
            }).start();
          }
        }
      };
      nonStickyTimer.schedule(5000);
    }
  }

  @Override
  public Widget asWidget() {
    return dialog;
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
    // check one is not being shown
    if(dialog.isShowing()) {
      dialog.hide();
    }
    // cancel a running sticky timer
    if(nonStickyTimer != null) {
      nonStickyTimer.cancel();
      nonStickyTimer = null;
    }

    dialog.removeStyleName("alert-" + NotificationType.ERROR.toString().toLowerCase());
    dialog.removeStyleName("alert-" + NotificationType.WARNING.toString().toLowerCase());
    dialog.removeStyleName("alert-" + NotificationType.INFO.toString().toLowerCase());
    dialog.addStyleName("alert-" + type.toString().toLowerCase());
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

  @Override
  public void setSticky(boolean sticky) {
    this.sticky = sticky;
  }

}
