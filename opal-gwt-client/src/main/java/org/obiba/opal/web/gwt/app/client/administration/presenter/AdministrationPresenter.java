package org.obiba.opal.web.gwt.app.client.administration.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.inject.Inject;

public class AdministrationPresenter extends WidgetPresenter<AdministrationPresenter.Display> {

  public static final String DELETE_ACTION = "Delete";

  public static final String EDIT_ACTION = "Edit";

  //
  // Instance Variables
  //

  @Inject
  private DataShieldAdministrationPresenter dataShieldAdministrationPresenter;

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
    dataShieldAdministrationPresenter.bind();
    getDisplay().setDataShieldAdministrationDisplay(dataShieldAdministrationPresenter.getDisplay());
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    dataShieldAdministrationPresenter.unbind();
  }

  @Override
  public void refreshDisplay() {
    dataShieldAdministrationPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    dataShieldAdministrationPresenter.revealDisplay();
  }

  //
  //
  //

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setDataShieldAdministrationDisplay(DataShieldAdministrationPresenter.Display display);

  }

}
