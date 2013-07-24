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
import java.util.Iterator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationCloseHandler;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.ui.ListItem;
import org.obiba.opal.web.gwt.app.client.view.FadeAnimation.FadedHandler;
import org.obiba.opal.web.gwt.app.client.ui.ResizeHandle;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
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
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * View used to display error, warning and info messages in a dialog box.
 */
public class NotificationView extends ViewImpl implements NotificationPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, NotificationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  Panel alertPanel;

  private final Translations translations;

  private boolean sticky = false;

  @Inject
  public NotificationView(Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public void setNotification(NotificationEvent event) {
    Alert alert = new Alert();
    alert.setAnimation(true);
    alert.setClose(true);
    switch(event.getNotificationType()) {
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
    if(event.getTitle() != null) {
      alert.setHeading(event.getTitle());
    }

    List<String> translatedMessages = new ArrayList<String>();
    for(String message : event.getMessages()) {
      if(translations.userMessageMap().containsKey(message)) {
        String msg = TranslationsUtils
            .replaceArguments(translations.userMessageMap().get(message), event.getMessageArgs());
        translatedMessages.add(msg);
      } else {
        translatedMessages.add(message);
      }
    }

    if(translatedMessages.size() == 1) {
      alert.setText(translatedMessages.get(0));
    } else {
      UnorderedList list = new UnorderedList();
      Iterator<String> iter = translatedMessages.iterator();
      while(iter.hasNext()) {
        list.add(new ListItem(new HTMLPanel(iter.next())));
      }
      alert.add(list);
    }
    alertPanel.add(alert);
    runSticky(alert);
  }

  private void runSticky(final Alert alert) {
    if(!sticky) {
      Timer nonStickyTimer = new Timer() {

        @Override
        public void run() {
          alert.close();
        }
      };
      nonStickyTimer.schedule(5000);
    }
  }

}
