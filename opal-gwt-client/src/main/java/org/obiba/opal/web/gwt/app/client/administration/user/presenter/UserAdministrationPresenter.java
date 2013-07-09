/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.user.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.BreadcrumbDisplay;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.GroupDto;
import org.obiba.opal.web.model.client.opal.UserDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class UserAdministrationPresenter
    extends ItemAdministrationPresenter<UserAdministrationPresenter.Display, UserAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.usersGroups)
  public interface Proxy extends ProxyPlace<UserAdministrationPresenter> {}

  private static final Translations translations = GWT.create(Translations.class);

  public interface Display extends View, BreadcrumbDisplay {

//    String INDEX_ACTION = "Index now";
//
//    String CLEAR_ACTION = "Clear";
//
//    String SCHEDULE = "Schedule indexing";

    void renderUserRows(JsArray<UserDto> rows);

    void renderGroupRows(JsArray<GroupDto> rows);

    void clear();
//    DropdownButton getActionsDropdown();

//    HasActionHandler<TableIndexStatusDto> getActions();

    HasData<UserDto> getUsersTable();

  }

//  @SuppressWarnings("FieldCanBeLocal")
//  private final AuthorizationPresenter authorizationPresenter;

  @SuppressWarnings("UnusedDeclaration")
  private Command confirmedCommand;

  @Inject
  public UserAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy) {
    super(eventBus, display, proxy);
//    this.authorizationPresenter = authorizationPresenter.get();
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/users").get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListUsersAuthorization())).send();
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, PageContainerPresenter.CONTENT, this);
  }

  @Override
  public String getName() {
    return translations.indicesLabel();
  }

  @Override
  protected void onReveal() {
    // stop start search service
//    ResourceRequestBuilderFactory.<UserDto>newBuilder().forResource("/users").get()
//        .withCallback(new ResourceCallback<UserDto>() {
//
//          }
//        }).send();

    getView().getUsersTable().setVisibleRange(0, 10);
//    refresh();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
//    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(Resources.indices()).get().authorize(authorizer)
//        .send();
  }

  @Override
  public String getTitle() {
    return translations.pageUsersAndGroupsTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  private final class ListUsersAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {

      // Fetch all users
      ResourceRequestBuilderFactory.<JsArray<UserDto>>newBuilder()//
          .forResource("/users").withCallback(new ResourceCallback<JsArray<UserDto>>() {

        @Override
        public void onResource(Response response, JsArray<UserDto> resource) {
          getView().renderUserRows(resource);
        }
      }).get().send();

      // Fetch all groups
      ResourceRequestBuilderFactory.<JsArray<GroupDto>>newBuilder()//
          .forResource("/groups").withCallback(new ResourceCallback<JsArray<GroupDto>>() {

        @Override
        public void onResource(Response response, JsArray<GroupDto> resource) {
          getView().renderGroupRows(resource);
        }
      }).get().send();

    }

    @Override
    public void unauthorized() {
    }
  }
}
