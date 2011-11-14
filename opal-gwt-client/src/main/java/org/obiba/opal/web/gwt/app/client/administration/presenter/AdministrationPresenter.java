package org.obiba.opal.web.gwt.app.client.administration.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.presenter.RAdministrationPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AdministrationPresenter extends WidgetPresenter<AdministrationPresenter.Display> {

  private static final int ADMINISTRATION_ITEMS_COUNT = 2;

  //
  // Instance Variables
  //

  @Inject
  private DataShieldConfigPresenter dataShieldAdministrationPresenter;

  @Inject
  private RAdministrationPresenter rAdministrationPresenter;

  private List<ItemAdministrationPresenter<?>> administrationPresenters = new ArrayList<ItemAdministrationPresenter<?>>();

  private int authorizedCount = 0;

  private int unAuthorizedCount = 0;

  //
  // Constructors
  //

  @Inject
  public AdministrationPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);

  }

  private void registerAdministrationPresenters() {
    administrationPresenters.clear();
    authorize(dataShieldAdministrationPresenter);
    authorize(rAdministrationPresenter);
  }

  public void authorize(ItemAdministrationPresenter<?> admin) {
    admin.authorize(new ItemAdministrationAuthorizer(admin));
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
    getDisplay().clearAdministrationDisplays();
    registerAdministrationPresenters();
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    for(ItemAdministrationPresenter<?> admin : administrationPresenters) {
      admin.unbind();
    }
    administrationPresenters.clear();
  }

  @Override
  public void refreshDisplay() {
    for(ItemAdministrationPresenter<?> admin : administrationPresenters) {
      admin.refreshDisplay();
    }
  }

  @Override
  public void revealDisplay() {
    for(ItemAdministrationPresenter<?> admin : administrationPresenters) {
      admin.revealDisplay();
    }
  }

  //
  //
  //

  public void authorize(final HasAuthorization authorizer) {
    authorizer.beforeAuthorization();
    dataShieldAdministrationPresenter.authorize(new AdministrationAuthorizer(authorizer));
    rAdministrationPresenter.authorize(new AdministrationAuthorizer(authorizer));
  }

  //
  // Inner Classes / Interfaces
  //

  private final class AdministrationAuthorizer implements HasAuthorization {
    private final HasAuthorization authorizer;

    private AdministrationAuthorizer(HasAuthorization authorizer) {
      this.authorizer = authorizer;
    }

    @Override
    public void unauthorized() {
      unAuthorizedCount++;
      // all administration items are unauthorized
      if(unAuthorizedCount == ADMINISTRATION_ITEMS_COUNT) {
        authorizer.unauthorized();
      }
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      authorizedCount++;
      // at least one administration item is authorized
      if(authorizedCount == 1) {
        authorizer.authorized();
      }
    }
  }

  private final class ItemAdministrationAuthorizer implements HasAuthorization {

    private final ItemAdministrationPresenter<?> admin;

    private ItemAdministrationAuthorizer(ItemAdministrationPresenter<?> admin) {
      this.admin = admin;
    }

    @Override
    public void unauthorized() {
    }

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      administrationPresenters.add(admin);
      admin.bind();
      getDisplay().addAdministrationDisplay(admin.getName(), admin.getWidget());
    }

  }

  public interface Display extends WidgetDisplay {

    void addAdministrationDisplay(String name, Widget w);

    void clearAdministrationDisplays();

  }

}
