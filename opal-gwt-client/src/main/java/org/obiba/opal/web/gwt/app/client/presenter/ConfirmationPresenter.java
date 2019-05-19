/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class ConfirmationPresenter extends PresenterWidget<ConfirmationPresenter.Display>
    implements ConfirmationUiHandlers {

  //
  // Instance Variables
  //

  private Object confirmationRequiredSource;

  //
  // Constructors
  //

  @Inject
  public ConfirmationPresenter(Display display, EventBus eventBus, Object confirmationRequiredSource) {
    super(eventBus, display);
    this.confirmationRequiredSource = confirmationRequiredSource;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    registerHandler(getEventBus().addHandler(ConfirmationRequiredEvent.getType(), new ConfirmationRequiredHandler()));
    registerHandler(getEventBus().addHandler(ConfirmationTerminatedEvent.getType(), new ConfirmationTerminatedEvent.Handler() {
      @Override
      public void onConfirmation(ConfirmationTerminatedEvent event) {
        getView().hideDialog();
      }
    }));
  }

  @Override
  public void onReveal() {
    getView().showDialog();
  }

  //
  // Methods
  //

  @Override
  public void onYes() {
    getEventBus().fireEvent(new ConfirmationEvent(confirmationRequiredSource, true));
    getView().disableDialog();
  }

  @Override
  public void onNo() {
    getEventBus().fireEvent(new ConfirmationEvent(confirmationRequiredSource, false));
    getView().hideDialog();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View, HasUiHandlers<ConfirmationUiHandlers> {

    void setConfirmationTitle(String title);

    void setConfirmationMessage(String message);

    void showDialog();

    void hideDialog();

    void disableDialog();
  }

  class ConfirmationRequiredHandler implements ConfirmationRequiredEvent.Handler {

    @Override
    public void onConfirmationRequired(ConfirmationRequiredEvent event) {
      confirmationRequiredSource = event.getSource();
      getView().setConfirmationTitle(event.getTitle());
      getView().setConfirmationMessage(event.getMessage());
      onReveal();
    }
  }
}
