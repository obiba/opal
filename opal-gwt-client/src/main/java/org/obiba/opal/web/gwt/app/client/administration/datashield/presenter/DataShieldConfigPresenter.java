/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceCallbacks;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.datashield.DataShieldConfigDto;
import org.obiba.opal.web.model.client.datashield.DataShieldConfigDto.Level;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class DataShieldConfigPresenter extends ItemAdministrationPresenter<DataShieldConfigPresenter.Display, DataShieldConfigPresenter.Proxy> {

  @ProxyStandard
  @NameToken("!admin.datashield")
  @TabInfo(container = AdministrationPresenter.class, label = "DataSHIELD", priority = 1)
  public interface Proxy extends TabContentProxyPlace<DataShieldConfigPresenter> {
  }

  public interface Display extends View {

    HasValue<DataShieldConfigDto.Level> levelSelector();

    HasAuthorization getPermissionsAuthorizer();

  }

  public static final Object AggregateEnvironmentSlot = new Object();

  public static final Object AssignEnvironmentSlot = new Object();

  public static final Object PermissionSlot = new Object();

  private final DataShieldAdministrationPresenter aggregatePresenter;

  private final DataShieldAdministrationPresenter assignPresenter;

  private final AuthorizationPresenter authorizationPresenter;

  @Inject
  public DataShieldConfigPresenter(Display display, EventBus eventBus, Proxy proxy, Provider<DataShieldAdministrationPresenter> adminPresenterProvider, AuthorizationPresenter authorizationPresenter) {
    super(eventBus, display, proxy);
    aggregatePresenter = adminPresenterProvider.get();
    assignPresenter = adminPresenterProvider.get();
    aggregatePresenter.setEnvironment("aggregate");
    assignPresenter.setEnvironment("assign");
    this.authorizationPresenter = authorizationPresenter;
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    aggregatePresenter.authorize(event.getHasAuthorization());
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, AdministrationPresenter.TabSlot, this);
  }

  @Override
  public String getName() {
    return "DataSHIELD";
  }

  @Override
  protected void onReveal() {
    ResourceRequestBuilderFactory.<DataShieldConfigDto> newBuilder().forResource("/datashield/cfg").withCallback(new ResourceCallback<DataShieldConfigDto>() {

      @Override
      public void onResource(Response response, DataShieldConfigDto resource) {
        getView().levelSelector().setValue(resource.getLevel(), true);
      }
    }).get().send();

    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder().authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    // TODO: Need to test both environments
    aggregatePresenter.authorize(authorizer);
  }

  @Override
  protected void onBind() {
    authorizationPresenter.setAclRequest("datashield", AclRequest.newBuilder("Use", "/datashield/session", "*:GET/*"), AclRequest.newBuilder("Administrate", "/datashield", "*:GET/*"));

    addToSlot(AggregateEnvironmentSlot, aggregatePresenter);
    addToSlot(AssignEnvironmentSlot, assignPresenter);

    getView().levelSelector().addValueChangeHandler(new ValueChangeHandler<DataShieldConfigDto.Level>() {
      @Override
      public void onValueChange(ValueChangeEvent<Level> event) {
        DataShieldConfigDto dto = DataShieldConfigDto.create();
        dto.setLevel(event.getValue());
        ResourceRequestBuilderFactory.<DataShieldConfigDto> newBuilder().forResource("/datashield/cfg").withResourceBody(DataShieldConfigDto.stringify(dto)).withCallback(ResourceCallbacks.<DataShieldConfigDto> noOp()).put().send();
      }
    });

    getView().levelSelector().setValue(DataShieldConfigDto.Level.RESTRICTED, false);
  }

  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {
      clearSlot(PermissionSlot);
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      setInSlot(PermissionSlot, authorizationPresenter);
    }
  }

}
