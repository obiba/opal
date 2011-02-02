package org.obiba.opal.web.gwt.app.client.administration.r.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

/**
 * R related administration.
 */
public class RAdministrationPresenter extends ItemAdministrationPresenter<RAdministrationPresenter.Display> {

  //
  // Instance Variables
  //

  //
  // Constructors
  //

  @Inject
  public RAdministrationPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public String getName() {
    return "R";
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  private void addEventHandlers() {
    registerHandler(getDisplay().addTestRServerHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        // TODO Auto-generated method stub

      }
    }));
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

  }

  //
  // methods
  //

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    HandlerRegistration addTestRServerHandler(ClickHandler handler);

  }

}
