package org.obiba.opal.web.gwt.app.client.administration.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.presenter.RAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class AdministrationPresenter extends Presenter<AdministrationPresenter.Display, AdministrationPresenter.Proxy> {

  private static final int ADMINISTRATION_ITEMS_COUNT = 2;

  private DataShieldConfigPresenter dataShieldAdministrationPresenter;

  private RAdministrationPresenter rAdministrationPresenter;

  private List<ItemAdministrationPresenter<?>> administrationPresenters = new ArrayList<ItemAdministrationPresenter<?>>();

  private int authorizedCount = 0;

  private int unAuthorizedCount = 0;

  @Inject
  public AdministrationPresenter(final Display display, final EventBus eventBus, final Proxy proxy, DataShieldConfigPresenter dataShieldAdministrationPresenter, RAdministrationPresenter rAdministrationPresenter) {
    super(eventBus, display, proxy);
    this.dataShieldAdministrationPresenter = dataShieldAdministrationPresenter;
    this.rAdministrationPresenter = rAdministrationPresenter;

  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, ApplicationPresenter.WORKBENCH, this);
  }

  private void registerAdministrationPresenters() {
    administrationPresenters.clear();
    authorize(dataShieldAdministrationPresenter);
    authorize(rAdministrationPresenter);
  }

  public void authorize(ItemAdministrationPresenter<?> admin) {
    admin.authorize(new ItemAdministrationAuthorizer(admin));
  }

  @Override
  protected void onBind() {
    getView().clearAdministrationDisplays();
    registerAdministrationPresenters();
  }

  @Override
  protected void onUnbind() {
    for(ItemAdministrationPresenter<?> admin : administrationPresenters) {
      admin.unbind();
    }
    administrationPresenters.clear();
  }

  @Override
  public void onReset() {
    for(ItemAdministrationPresenter<?> admin : administrationPresenters) {
      admin.refreshDisplay();
    }
  }

  @Override
  public void onReveal() {
    for(ItemAdministrationPresenter<?> admin : administrationPresenters) {
      admin.revealDisplay();
    }
  }

  public void authorize(final HasAuthorization authorizer) {
    authorizer.beforeAuthorization();
    dataShieldAdministrationPresenter.authorize(new AdministrationAuthorizer(authorizer));
    rAdministrationPresenter.authorize(new AdministrationAuthorizer(authorizer));
  }

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
      authorizer.authorized();
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
      getView().addAdministrationDisplay(admin.getName(), admin.getWidget());
    }

  }

  public interface Display extends View {

    void addAdministrationDisplay(String name, Widget w);

    void clearAdministrationDisplays();

  }

  @ProxyStandard
  @NameToken(Places.administration)
  public interface Proxy extends ProxyPlace<AdministrationPresenter> {
  }

}
