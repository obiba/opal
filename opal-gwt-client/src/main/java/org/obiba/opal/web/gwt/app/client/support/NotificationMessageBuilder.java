/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;

import com.google.gwt.core.client.GWT;

public final class NotificationMessageBuilder {

  private static final Translations translations = GWT.create(Translations.class);

  private final NotificationEvent notificationEvent;

  private NotificationMessageBuilder(NotificationEvent event) {
    notificationEvent = event;
  }

  public static NotificationMessageBuilder get(NotificationEvent event) {
    return new NotificationMessageBuilder(event);
  }

  public List<String> build() {
    return getMessages();
  }

  private List<String> getMessages() {
    List<String> translatedMessages = new ArrayList<String>();
    for(String message : notificationEvent.getMessages()) {
      if(translations.userMessageMap().containsKey(message)) {
        String msg = TranslationsUtils
            .replaceArguments(translations.userMessageMap().get(message), notificationEvent.getMessageArgs());
        translatedMessages.add(msg);
      } else {
        translatedMessages.add(message);
      }
    }

    return translatedMessages;
  }
}

