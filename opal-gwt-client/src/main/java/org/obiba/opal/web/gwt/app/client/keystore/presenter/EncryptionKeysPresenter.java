/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.presenter;

import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.KeyDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class EncryptionKeysPresenter extends PresenterWidget<EncryptionKeysPresenter.Display>
    implements EncryptionKeysUiHandlers {

  private final ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider;

  private ProjectDto project;

  @Inject
  public EncryptionKeysPresenter(Display display, EventBus eventBus,
      ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider) {
    super(eventBus, display);
    this.createKeyPairModalProvider = createKeyPairModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  public void initialize(ProjectDto projectDto) {
    project = projectDto;
    retrieveKeyPairs();
  }

  private void retrieveKeyPairs() {
    UriBuilder ub = UriBuilder.create().segment("project", project.getName(), "keystore");
    ResourceRequestBuilderFactory.<JsArray<KeyDto>>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResourceCallback<JsArray<KeyDto>>() {
          @Override
          public void onResource(Response response, JsArray<KeyDto> resource) {
            getView().setData(JsArrays.toList(resource));
          }
        }).send();

  }

  public interface Display extends View, HasUiHandlers<EncryptionKeysUiHandlers> {

    public static final String DOWNLOAD_CERTIFICATE_ACTION = "DOWNLOAD_CERTIFICATE_ACTION";

    HasActionHandler<KeyDto> getActions();

    void setData(@Nonnull List<KeyDto> keyPairs);
  }
}
