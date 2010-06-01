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

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;

/**
 * Presenter used to display error, warning and info messages in a dialog box.
 */
public class ErrorDialogPresenter extends WidgetPresenter<ErrorDialogPresenter.Display> {

  public interface Display extends WidgetDisplay {

    public void showPopup();

    public HasClickHandlers getOkay();

    public void setErrors(List<String> errors);

    public void setCaption(String caption);

  }

  @Inject
  public ErrorDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    display.showPopup();
  }

  public void setErrors(List<String> errors) {
    display.setErrors(errors);
  }

  public void setMessageDialogType(MessageDialogType messageDialogType) {
    switch(messageDialogType) {
    case ERROR:
      getDisplay().setCaption("Errors");
      break;
    case WARNING:
      getDisplay().setCaption("Warnings");
      break;
    case INFO:
      getDisplay().setCaption("Information");
      break;
    }
  }

  public enum MessageDialogType {
    ERROR, WARNING, INFO
  }

  public void addOkayClickHandler(ClickHandler clickHandler) {
    registerHandler(display.getOkay().addClickHandler(clickHandler));
  }
}
