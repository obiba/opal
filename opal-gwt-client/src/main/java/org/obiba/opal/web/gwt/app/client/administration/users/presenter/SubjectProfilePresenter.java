package org.obiba.opal.web.gwt.app.client.administration.users.presenter;

import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.SubjectProfileDto;

import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

/**
 * Current subject page.
 */
public class SubjectProfilePresenter extends Presenter<SubjectProfilePresenter.Display, SubjectProfilePresenter.Proxy>
    implements SubjectProfileUiHandlers {

  private SubjectProfileDto profile;

  @ProxyStandard
  @NameToken(Places.PROFILE)
  public interface Proxy extends ProxyPlace<SubjectProfilePresenter> {}

  private final ModalProvider<ChangePasswordModalPresenter> changePasswordModalProvider;

  @Inject
  public SubjectProfilePresenter(EventBus eventBus, Display display, Proxy proxy,
      ModalProvider<ChangePasswordModalPresenter> changePasswordProvider) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    changePasswordModalProvider = changePasswordProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onReveal() {
    ResourceRequestBuilderFactory.<SubjectProfileDto>newBuilder() //
        .forResource(UriBuilders.PROFILE.create().build("_current")) //
        .withCallback(new ResourceCallback<SubjectProfileDto>() {
          @Override
          public void onResource(Response response, SubjectProfileDto resource) {
            if(response.getStatusCode() == Response.SC_OK) {
              profile = resource;
              getView().enableChangePassword("opal-user-realm".equals(resource.getRealm()), resource.getRealm());
            } else {
              getView().enableChangePassword(false, "?");
            }
          }
        }) //
        .get().send();
  }

  @Override
  public void onChangePassword() {
    changePasswordModalProvider.get().setPrincipal(profile.getPrincipal());
  }

  public interface Display extends View, HasUiHandlers<SubjectProfileUiHandlers> {

    void enableChangePassword(boolean enabled, String realm);

  }
}
