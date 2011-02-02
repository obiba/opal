package org.obiba.opal.web.gwt.app.client.administration.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AdministrationPresenter extends WidgetPresenter<AdministrationPresenter.Display> {

  //
  // Instance Variables
  //

  @Inject
  private DataShieldAdministrationPresenter dataShieldAdministrationPresenter;

  @Inject
  private RAdministrationPresenter rAdministrationPresenter;

  private List<ItemAdministrationPresenter> administrationPresenters = new ArrayList<ItemAdministrationPresenter>();

  //
  // Constructors
  //

  @Inject
  public AdministrationPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  private void registerAdministrationPresenters() {
    administrationPresenters.clear();
    administrationPresenters.add(dataShieldAdministrationPresenter);
    administrationPresenters.add(rAdministrationPresenter);
    getDisplay().clearAdministrationDisplays();
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
    registerAdministrationPresenters();
    for(ItemAdministrationPresenter admin : administrationPresenters) {
      admin.bind();
      getDisplay().addAdministrationDisplay(admin.getName(), admin.getWidget());
    }
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    for(ItemAdministrationPresenter admin : administrationPresenters) {
      admin.unbind();
    }
    administrationPresenters.clear();
  }

  @Override
  public void refreshDisplay() {
    for(ItemAdministrationPresenter admin : administrationPresenters) {
      admin.refreshDisplay();
    }
  }

  @Override
  public void revealDisplay() {
    for(ItemAdministrationPresenter admin : administrationPresenters) {
      admin.revealDisplay();
    }
  }

  //
  //
  //

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void addAdministrationDisplay(String name, Widget w);

    void clearAdministrationDisplays();

  }

}
