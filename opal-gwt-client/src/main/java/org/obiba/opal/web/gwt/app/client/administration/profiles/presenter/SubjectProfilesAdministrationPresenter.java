/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.profiles.presenter;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.SubjectProfileDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class SubjectProfilesAdministrationPresenter extends
    ItemAdministrationPresenter<SubjectProfilesAdministrationPresenter.Display, SubjectProfilesAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.PROFILES)
  public interface Proxy extends ProxyPlace<SubjectProfilesAdministrationPresenter> {}

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public SubjectProfilesAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.breadcrumbsHelper = breadcrumbsHelper;
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.PROFILES.create().build()) //
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListProfilesAuthorization())) //
        .get().send();
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {

  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageProfilesTitle();
  }

  private void refreshProfiles() {
    // Fetch all groups
    ResourceRequestBuilderFactory.<JsArray<SubjectProfileDto>>newBuilder() //
        .forResource(UriBuilders.PROFILES.create().build()) //
        .withCallback(new ResourceCallback<JsArray<SubjectProfileDto>>() {
          @Override
          public void onResource(Response response, JsArray<SubjectProfileDto> resource) {
            getView().renderProfiles(JsArrays.toList(resource));
          }
        }) //
        .get().send();
  }

  private final class ListProfilesAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      refreshProfiles();
    }

    @Override
    public void unauthorized() {
    }
  }

  public interface Display extends View, HasBreadcrumbs {

    void renderProfiles(List<SubjectProfileDto> rows);

    void clear();
  }
}
