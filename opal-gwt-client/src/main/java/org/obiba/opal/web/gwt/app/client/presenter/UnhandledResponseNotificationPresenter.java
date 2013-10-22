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

import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class UnhandledResponseNotificationPresenter
    extends PresenterWidget<UnhandledResponseNotificationPresenter.Display> {

  public interface Display extends View {
    void setErrorMessage(String msg);
  }

  @Inject
  public UnhandledResponseNotificationPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  public UnhandledResponseNotificationPresenter withResponseEvent(UnhandledResponseEvent event) {

    String message = event.getShortMessage();

    if(!event.getResponse().getText().isEmpty()) {
      message += ": " + event.getResponse().getText();
    }

    getView().setErrorMessage(message);

    return this;
  }

}

