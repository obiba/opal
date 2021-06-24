/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.users.profile.admin;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.SubjectProfileDto;

import java.util.List;

public class SubjectProfilesAdministrationPresenter
    extends ItemAdministrationPresenter<SubjectProfilesAdministrationPresenter.Display, SubjectProfilesAdministrationPresenter.Proxy>
    implements SubjectProfilesAdministrationUiHandlers {

  @ProxyStandard
  @NameToken(Places.PROFILES)
  public interface Proxy extends ProxyPlace<SubjectProfilesAdministrationPresenter> {
  }

  private Runnable removeProfileConfirmation;

  private Runnable removeProfilesConfirmation;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public SubjectProfilesAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
                                                DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.breadcrumbsHelper = breadcrumbsHelper;
    getView().setUiHandlers(this);
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.SUBJECT_PROFILES.create().build()) //
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListProfilesAuthorization())) //
        .get().send();
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  protected void onBind() {
    // Register event handlers
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if (event.getSource().equals(removeProfileConfirmation) && event.isConfirmed()) {
          removeProfileConfirmation.run();
          removeProfileConfirmation = null;
        } else if (event.getSource().equals(removeProfilesConfirmation) && event.isConfirmed()) {
          removeProfilesConfirmation.run();
          removeProfilesConfirmation = null;
        }
      }
    }));

    getView().getActions().setActionHandler(new ActionHandler<SubjectProfileDto>() {
      @Override
      public void doAction(SubjectProfileDto object, String actionName) {
        if (ActionsColumn.REMOVE_ACTION.equals(actionName)) {
          removeProfileConfirmation = new RemoveRunnable(object);
          String title = translations.removeUserProfile();
          String message = translationMessages.confirmRemoveUserProfile(object.getPrincipal());
          fireEvent(ConfirmationRequiredEvent.createWithMessages(removeProfileConfirmation, title, message));
        }
      }
    });
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    refreshProfiles();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {

  }

  @Override
  public void onRemoveProfiles(final List<SubjectProfileDto> profiles) {
    removeProfilesConfirmation = new Runnable() {

      @Override
      public void run() {
        UriBuilder builder = UriBuilders.SUBJECT_PROFILES.create();
        for (SubjectProfileDto profile : profiles) {
          builder.query("p", profile.getPrincipal());
        }
        ResourceRequestBuilderFactory.newBuilder()
            .forResource(builder.build())
            .withCallback(Response.SC_OK, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                refreshProfiles();
              }
            }).delete().send();
      }
    };
    getEventBus().fireEvent(ConfirmationRequiredEvent
        .createWithMessages(removeProfilesConfirmation, translations.removeUserProfiles(),
            translationMessages.confirmRemoveUserProfiles()));
  }


  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageProfilesTitle();
  }

  private void refreshProfiles() {
    // Fetch all profiles
    ResourceRequestBuilderFactory.<JsArray<SubjectProfileDto>>newBuilder() //
        .forResource(UriBuilders.SUBJECT_PROFILES.create().build()) //
        .withCallback(new ResourceCallback<JsArray<SubjectProfileDto>>() {
          @Override
          public void onResource(Response response, JsArray<SubjectProfileDto> resource) {
            getView().renderProfiles(JsArrays.toList(resource));
          }
        }) //
        .get().send();
  }

  private class RemoveRunnable implements Runnable {

    private final SubjectProfileDto profile;

    private RemoveRunnable(SubjectProfileDto profile) {
      this.profile = profile;
    }

    @Override
    public void run() {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.SUBJECT_PROFILE.create().build(profile.getPrincipal())) //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              fireEvent(ConfirmationTerminatedEvent.create());
              refreshProfiles();
            }
          }).delete().send();
    }
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

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<SubjectProfilesAdministrationUiHandlers> {

    void renderProfiles(List<SubjectProfileDto> rows);

    HasActionHandler<SubjectProfileDto> getActions();

    void clear();
  }
}
