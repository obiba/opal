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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

/**
 *
 */
public class ConfirmationPresenter extends WidgetPresenter<ConfirmationPresenter.Display> {
  //
  // Instance Variables
  //

  private Object confirmationRequiredSource;

  //
  // Constructors
  //

  @Inject
  public ConfirmationPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  protected void onBind() {
    addEventHandlers();
  }

  protected void onUnbind() {
  }

  public void refreshDisplay() {
    // TODO Auto-generated method stub

  }

  public void revealDisplay() {
    getDisplay().showDialog();
  }

  public Place getPlace() {
    return null;
  }

  protected void onPlaceRequest(PlaceRequest request) {
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
    super.registerHandler(eventBus.addHandler(ConfirmationRequiredEvent.getType(), new ConfirmationRequiredHandler()));
  }

  private void addYesButtonHandler() {
    super.registerHandler(getDisplay().addYesButtonHandler(new YesButtonHandler()));
  }

  private void addNoButtonHandler() {
    super.registerHandler(getDisplay().addNoButtonHandler(new NoButtonHandler()));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setConfirmationTitle(String title);

    void setConfirmationMessage(String message);

    void showDialog();

    void hideDialog();

    HandlerRegistration addYesButtonHandler(ClickHandler clickHandler);

    HandlerRegistration addNoButtonHandler(ClickHandler clickHandler);
  }

  class ConfirmationRequiredHandler implements ConfirmationRequiredEvent.Handler {

    public void onConfirmationRequired(ConfirmationRequiredEvent event) {
      confirmationRequiredSource = event.getSource();
      getDisplay().setConfirmationTitle(event.getTitle());
      getDisplay().setConfirmationMessage(event.getMessage());
      revealDisplay();
    }
  }

  class YesButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new ConfirmationEvent(confirmationRequiredSource, true));
      getDisplay().hideDialog();
    }
  }

  class NoButtonHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new ConfirmationEvent(confirmationRequiredSource, false));
      getDisplay().hideDialog();
    }
  }
}
