package org.obiba.opal.web.gwt.app.client.administration.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.inject.Inject;

public class AdministrationPresenter extends WidgetPresenter<AdministrationPresenter.Display> {

  //
  // Instance Variables
  //

  //
  // Constructors
  //

  @Inject
  public AdministrationPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
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
    // ResourceRequestBuilderFactory.newBuilder().forResource("/datashield/methods").get().withCallback(200, new
    // ResponseCodeCallback() {
    //
    // @Override
    // public void onResponseCode(Request request, Response response) {
    //
    // }
    // }).send();

  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

  }

}
