/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.*;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;
import org.obiba.opal.web.model.client.datashield.DataShieldROptionDto;

import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class DataShieldROptionsPresenter extends PresenterWidget<DataShieldROptionsPresenter.Display>
    implements DataShieldROptionsUiHandlers {

  private final ModalProvider<DataShieldROptionModalPresenter> modalProvider;

  private final TranslationMessages translationMessages;

  private Runnable removeOptionConfirmation;

  private Runnable removeOptionsConfirmation;

  private DataShieldProfileDto profile;

  public interface Display extends View, HasUiHandlers<DataShieldROptionsUiHandlers> {

    void initialize(List<DataShieldROptionDto> options);

    void setOptionActionHandler(ActionHandler<DataShieldROptionDto> handler);

    HasAuthorization addROptionsAuthorizer();
  }

  @Inject
  public DataShieldROptionsPresenter(Display display, EventBus eventBus,
                                     ModalProvider<DataShieldROptionModalPresenter> provider, TranslationMessages translationMessages) {
    super(eventBus, display);
    modalProvider = provider.setContainer(this);
    this.translationMessages = translationMessages;
    getView().setUiHandlers(this);
  }

  public void setProfile(DataShieldProfileDto profile) {
    this.profile = profile;
  }

  @Override
  protected void onBind() {
    super.onBind();
    getView().setOptionActionHandler(new ActionHandler<DataShieldROptionDto>() {
      @Override
      public void doAction(DataShieldROptionDto optionDto, String actionName) {
        switch (actionName) {
          case REMOVE_ACTION:
            removeOption(optionDto);
            break;
          case EDIT_ACTION:
            editOption(optionDto);
            break;
        }
      }

    });
    addRegisteredHandler(DataShieldPackageCreatedEvent.getType(),
        new DataShieldPackageCreatedEvent.DataShieldPackageCreatedHandler() {
          @Override
          public void onDataShieldPackageCreated(DataShieldPackageCreatedEvent event) {
            if (profile.getCluster().equals(event.getCluster()))
              refresh();
          }
        });
    addRegisteredHandler(DataShieldPackageUpdatedEvent.getType(),
        new DataShieldPackageUpdatedEvent.DataShieldPackageUpdatedHandler() {
          @Override
          public void onDataShieldPackageUpdated(DataShieldPackageUpdatedEvent event) {
            if (profile.getCluster().equals(event.getCluster()))
              refresh();
          }
        });
    addRegisteredHandler(DataShieldPackageRemovedEvent.getType(),
        new DataShieldPackageRemovedEvent.DataShieldPackageRemovedHandler() {

          @Override
          public void onDataShieldPackageRemoved(DataShieldPackageRemovedEvent event) {
            if (profile.getCluster().equals(event.getCluster()))
              refresh();
          }
        });
    addRegisteredHandler(DataShieldROptionCreatedEvent.getType(),
        new DataShieldROptionCreatedEvent.DataShieldROptionCreatedHandler() {
          @Override
          public void onDataShieldROptionCreated(DataShieldROptionCreatedEvent event) {
            if (profile.getName().equals(event.getProfile()))
              refresh();
          }
        });
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if (event.getSource().equals(removeOptionConfirmation) && event.isConfirmed()) {
          removeOptionConfirmation.run();
          removeOptionConfirmation = null;
        } else if (event.getSource().equals(removeOptionsConfirmation) && event.isConfirmed()) {
          removeOptionsConfirmation.run();
          removeOptionsConfirmation = null;
        }
      }
    });
    addRegisteredHandler(DataShieldProfileUpdatedEvent.getType(),
        new DataShieldProfileUpdatedEvent.DataShieldProfileUpdatedHandler() {
          @Override
          public void onDataShieldProfileUpdated(DataShieldProfileUpdatedEvent event) {
            if (profile.getName().equals(event.getProfile().getName()))
              refresh();
          }
        });
  }

  @Override
  public void onAddOption() {
    modalProvider.get().setProfile(profile);
  }

  @Override
  public void onRemoveOptions(final List<DataShieldROptionDto> selectedItems) {
    removeOptionsConfirmation = new Runnable() {

      @Override
      public void run() {
        UriBuilder builder = UriBuilders.DATASHIELD_ROPTIONS.create()
            .query("profile", profile.getName());
        for (DataShieldROptionDto dto : selectedItems)
          builder.query("name", dto.getName());
        ResourceRequestBuilderFactory.newBuilder()
            .forResource(builder.build())
            .withCallback(Response.SC_OK, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                refresh();
              }
            }).delete().send();
      }
    };
    getEventBus().fireEvent(ConfirmationRequiredEvent
        .createWithMessages(removeOptionsConfirmation, translationMessages.removeDataShieldOptions(),
            translationMessages.confirmDeleteDataShieldOptions()));
  }

  private void editOption(DataShieldROptionDto optionDto) {
    DataShieldROptionModalPresenter presenter = modalProvider.get();
    presenter.setProfile(profile);
    presenter.setOption(optionDto);
  }

  private void removeOption(final DataShieldROptionDto optionDto) {
    removeOptionConfirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.newBuilder()
            .forResource(UriBuilders.DATASHIELD_ROPTION.create()
                .query("name", optionDto.getName())
                .query("profile", profile.getName()).build())
            .withCallback(Response.SC_OK, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                refresh();
              }
            }).delete().send();
      }
    };
    getEventBus().fireEvent(ConfirmationRequiredEvent
        .createWithMessages(removeOptionConfirmation, translationMessages.removeDataShieldOption(),
            translationMessages.confirmDeleteDataShieldOption(optionDto.getName())));
  }

  @Override
  protected void onReveal() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.DATASHIELD_ROPTION.create()
            .query("profile", profile.getName()).build())
        .post()
        .authorize(getView().addROptionsAuthorizer()).send();
    refresh();
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<JsArray<DataShieldROptionDto>>newBuilder()
        .forResource(UriBuilders.DATASHIELD_ROPTIONS.create()
            .query("profile", profile.getName()).build())
        .withCallback(new ResourceCallback<JsArray<DataShieldROptionDto>>() {

          @Override
          public void onResource(Response response, JsArray<DataShieldROptionDto> options) {
            getView().initialize(JsArrays.toList(options));
          }
        }).get().send();
  }

}
