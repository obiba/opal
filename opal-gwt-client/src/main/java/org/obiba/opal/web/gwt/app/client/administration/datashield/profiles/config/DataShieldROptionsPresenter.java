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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageRemovedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldROptionCreatedEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.datashield.DataShieldROptionDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

import java.util.List;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class DataShieldROptionsPresenter extends PresenterWidget<DataShieldROptionsPresenter.Display>
    implements DataShieldROptionsUiHandlers {

  private final ModalProvider<DataShieldROptionModalPresenter> modalProvider;

  private RServerClusterDto cluster;

  public interface Display extends View, HasUiHandlers<DataShieldROptionsUiHandlers> {

    void initialize(List<DataShieldROptionDto> options);

    void setOptionActionHandler(ActionHandler<DataShieldROptionDto> handler);

    HasAuthorization addROptionsAuthorizer();
  }

  @Inject
  public DataShieldROptionsPresenter(Display display, EventBus eventBus,
                                     ModalProvider<DataShieldROptionModalPresenter> provider) {
    super(eventBus, display);
    modalProvider = provider.setContainer(this);
    getView().setUiHandlers(this);
  }

  public void setCluster(RServerClusterDto cluster) {
    this.cluster = cluster;
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
            if (cluster.getName().equals(event.getProfile()))
              refresh();
          }
        });
    addRegisteredHandler(DataShieldPackageUpdatedEvent.getType(),
        new DataShieldPackageUpdatedEvent.DataShieldPackageUpdatedHandler() {
          @Override
          public void onDataShieldPackageUpdated(DataShieldPackageUpdatedEvent event) {
            if (cluster.getName().equals(event.getProfile()))
              refresh();
          }
        });
    addRegisteredHandler(DataShieldPackageRemovedEvent.getType(),
        new DataShieldPackageRemovedEvent.DataShieldPackageRemovedHandler() {

          @Override
          public void onDataShieldPackageRemoved(DataShieldPackageRemovedEvent event) {
            if (cluster.getName().equals(event.getProfile()))
              refresh();
          }
        });
    addRegisteredHandler(DataShieldROptionCreatedEvent.getType(),
        new DataShieldROptionCreatedEvent.DataShieldROptionCreatedHandler() {
          @Override
          public void onDataShieldROptionCreated(DataShieldROptionCreatedEvent event) {
            if (cluster.getName().equals(event.getProfile()))
              refresh();
          }
        });
  }

  @Override
  public void addOption() {
    modalProvider.get().setCluster(cluster);
  }

  private void editOption(DataShieldROptionDto optionDto) {
    DataShieldROptionModalPresenter presenter = modalProvider.get();
    presenter.setCluster(cluster);
    presenter.setOption(optionDto);
  }

  private void removeOption(DataShieldROptionDto optionDto) {
    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.DATASHIELD_ROPTION.create()
            .query("name", optionDto.getName())
            .query("profile", cluster.getName()).build())
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refresh();
          }
        }).delete().send();
  }

  @Override
  protected void onReveal() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.DATASHIELD_ROPTION.create()
            .query("profile", cluster.getName()).build())
        .post()
        .authorize(getView().addROptionsAuthorizer()).send();
    refresh();
  }

  private void refresh() {
    ResourceRequestBuilderFactory.<JsArray<DataShieldROptionDto>>newBuilder()
        .forResource(UriBuilders.DATASHIELD_ROPTIONS.create()
            .query("profile", cluster.getName()).build())
        .withCallback(new ResourceCallback<JsArray<DataShieldROptionDto>>() {

          @Override
          public void onResource(Response response, JsArray<DataShieldROptionDto> options) {
            getView().initialize(JsArrays.toList(options));
          }
        }).get().send();
  }

}
