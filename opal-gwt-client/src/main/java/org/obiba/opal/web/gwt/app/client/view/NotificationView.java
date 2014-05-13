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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.support.NotificationAlertTypeMap;
import org.obiba.opal.web.gwt.app.client.support.NotificationMessageBuilder;
import org.obiba.opal.web.gwt.app.client.ui.ListItem;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * View used to display error, warning and info messages in a dialog box.
 */
public class NotificationView extends ViewImpl implements NotificationPresenter.Display {

  interface Binder extends UiBinder<Widget, NotificationView> {}

  private static final int DEFAULT_NOTIFICATION_DELAY = 5000;

  private static final int ERROR_NOTIFICATION_DELAY = 10000;

  @UiField
  Panel alertPanel;

  @Inject
  public NotificationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setNotification(NotificationEvent event) {
    NotificationType type = event.getNotificationType();
    Alert alert = createAlert(type, event.getTitle());
    addMessages(alert, NotificationMessageBuilder.get(event).build());
    alertPanel.add(alert);
    if(!event.isSticky()) runNonStickyTimer(alert,
        (NotificationType.ERROR == type ? ERROR_NOTIFICATION_DELAY : DEFAULT_NOTIFICATION_DELAY));
  }

  private Alert createAlert(NotificationType type, @Nullable String title) {
    Alert alert = new Alert();
    alert.setAnimation(true);
    alert.setClose(true);
    if(title != null) alert.setHeading(title);
    alert.setType(NotificationAlertTypeMap.getAlertType(type));
    return alert;
  }

  @Override
  public void close() {
    alertPanel.clear();
  }

  private void addMessages(Alert alert, List<String> messages) {

    if(messages.size() == 1) {
      alert.setHTML(messages.get(0));
    } else {
      UnorderedList list = new UnorderedList();
      for(String message : messages) {
        list.add(new ListItem(new HTMLPanel(message)));
      }
      alert.add(list);
    }
  }

  private void runNonStickyTimer(final Alert alert, int delayMillis) {
    Timer nonStickyTimer = new Timer() {

      @Override
      public void run() {
        alert.close();
      }
    };
    nonStickyTimer.schedule(delayMillis);
  }

}
