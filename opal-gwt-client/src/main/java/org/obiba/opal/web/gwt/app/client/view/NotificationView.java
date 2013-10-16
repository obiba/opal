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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.ui.ListItem;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
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

  @UiField
  Panel alertPanel;

  private final Translations translations;

  @Inject
  public NotificationView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public void setNotification(NotificationEvent event) {
    setNotification(event.getNotificationType(), event.getMessages(), event.getMessageArgs(), event.getTitle(),
        event.isSticky(), null);
  }

  @Override
  public void setNotification(NotificationType type, List<String> messages, @Nullable List<String> messageArgs,
      @Nullable String title, boolean isSticky, @Nullable ClosedHandler handler) {
    Alert alert = createAlert(type, title, handler);
    addMessages(alert, messages, messageArgs == null ? new ArrayList<String>() : messageArgs);
    alertPanel.add(alert);
    if(!isSticky) runSticky(alert);
  }

  private Alert createAlert(NotificationType type, @Nullable String title, @Nullable ClosedHandler handler) {
    Alert alert = new Alert();
    alert.setAnimation(true);
    alert.setClose(true);
    if(handler != null) alert.addClosedHandler(handler);
    if(title != null) alert.setHeading(title);

    switch(type) {
      case ERROR:
        alert.setType(AlertType.ERROR);
        break;
      case WARNING:
        alert.setType(AlertType.WARNING);
        break;
      case INFO:
        alert.setType(AlertType.INFO);
        break;
    }
    return alert;
  }

  @Override
  public void close() {
//    for (Iterator<Widget> iterator = alertPanel.iterator(); iterator.hasNext();) {
//      Widget widget = iterator.next();
//      if (widget instanceof Alert) {
//        Alert alert = (Alert)widget;
//        alertPanel.remove(alert);
//        alert.close();
//      }
//    }
    alertPanel.clear();
  }

  private void addMessages(Alert alert, Iterable<String> messages, List<String> messageArgs) {
    List<String> translatedMessages = new ArrayList<String>();
    for(String message : messages) {
      if(translations.userMessageMap().containsKey(message)) {
        String msg = TranslationsUtils.replaceArguments(translations.userMessageMap().get(message), messageArgs);
        translatedMessages.add(msg);
      } else {
        translatedMessages.add(message);
      }
    }

    if(translatedMessages.size() == 1) {
      alert.setText(translatedMessages.get(0));
    } else {
      UnorderedList list = new UnorderedList();
      for(String translatedMessage : translatedMessages) {
        list.add(new ListItem(new HTMLPanel(translatedMessage)));
      }
      alert.add(list);
    }
  }

  private void runSticky(final Alert alert) {
    Timer nonStickyTimer = new Timer() {

      @Override
      public void run() {
        alert.close();
      }
    };
    nonStickyTimer.schedule(5000);
  }

}
