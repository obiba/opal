/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Presenter used to display error, warning and info messages in a dialog box.
 */
public class NotificationPresenter extends PresenterWidget<NotificationPresenter.Display> {

  public interface Display extends View {

    void setNotification(NotificationEvent event);

    void close();
  }

  public interface NotificationCloseHandler {
    /**
     * Called when {@link CloseEvent} is fired.
     *
     * @param event the {@link CloseEvent} that was fired
     */
    void onClose(CloseEvent<?> event);
  }

  @Inject
  public NotificationPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  public void setNotification(NotificationEvent event) {
    getView().setNotification(event);
  }

  public void close() {
    getView().close();
  }

  public enum NotificationType {
    ERROR, WARNING, INFO
  }

}
