/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class ConfirmationPresenter extends PresenterWidget<ConfirmationPresenter.Display> {

  private static final Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  private Object confirmationRequiredSource;

  //
  // Constructors
  //

  @Inject
  public ConfirmationPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  //
  // Methods
  //

  private void addEventHandlers() {
    addConfirmationRequiredHandler();
    addYesButtonHandler();
    addNoButtonHandler();
  }

  private void addConfirmationRequiredHandler() {
    registerHandler(getEventBus().addHandler(ConfirmationRequiredEvent.getType(), new ConfirmationRequiredHandler()));
  }

  private void addYesButtonHandler() {
    registerHandler(getView().addYesButtonHandler(new YesButtonHandler()));
  }

  private void addNoButtonHandler() {
    registerHandler(getView().addNoButtonHandler(new NoButtonHandler()));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends PopupView {

    void setConfirmationTitle(String title);

    void setConfirmationMessage(String message);

    void showDialog();

    void hideDialog();

    HandlerRegistration addYesButtonHandler(ClickHandler clickHandler);

    HandlerRegistration addNoButtonHandler(ClickHandler clickHandler);
  }

  class ConfirmationRequiredHandler implements ConfirmationRequiredEvent.Handler {

    @Override
    public void onConfirmationRequired(ConfirmationRequiredEvent event) {
      confirmationRequiredSource = event.getSource();
      getView().setConfirmationTitle(event.getTitleKey() == null
          ? event.getTitle()
          : translations.confirmationTitleMap().get(event.getTitleKey()));
      getView().setConfirmationMessage(event.getMessageKey() == null
          ? event.getMessage()
          : translations.confirmationMessageMap().get(event.getMessageKey()));
      onReveal();
    }
  }

  class YesButtonHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getEventBus().fireEvent(new ConfirmationEvent(confirmationRequiredSource, true));
      getView().hideDialog();
    }
  }

  class NoButtonHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getEventBus().fireEvent(new ConfirmationEvent(confirmationRequiredSource, false));
      getView().hideDialog();
    }
  }
}
