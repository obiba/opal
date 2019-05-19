/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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

import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.KeyDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class EncryptionKeysPresenter extends PresenterWidget<EncryptionKeysPresenter.Display>
    implements EncryptionKeysUiHandlers, KeyPairModalSavedHandler {

  private final ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider;

  private final ModalProvider<ImportKeyPairModalPresenter> importKeyPairModalProvider;

  private ProjectDto project;

  @Inject
  public EncryptionKeysPresenter(Display display, EventBus eventBus,
      ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider,
      ModalProvider<ImportKeyPairModalPresenter> importKeyPairModalProvider) {
    super(eventBus, display);
    this.createKeyPairModalProvider = createKeyPairModalProvider.setContainer(this);
    this.importKeyPairModalProvider = importKeyPairModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    getView().getActions().setActionHandler(new ActionHandler<KeyDto>() {

      @Override
      public void doAction(KeyDto keyPair, String actionName) {
        if(Display.DOWNLOAD_CERTIFICATE_ACTION.equals(actionName)) {
          downloadCertificate(keyPair);
        } else if(ActionsColumn.REMOVE_ACTION.equals(actionName)) {
          deleteKey(keyPair);
        }
      }
    });
  }

  private void deleteKey(KeyDto keyPair) {
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        retrieveKeyPairs();
      }
    };

    ResourceRequestBuilderFactory.<JsArray<KeyDto>>newBuilder()
        .forResource(UriBuilders.PROJECT_KEYSTORE_ALIAS.create().build(project.getName(), keyPair.getAlias())).delete()
        .withCallback(callbackHandler, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_NOT_FOUND) //
        .send();

  }

  private void downloadCertificate(KeyDto keyPair) {
    getEventBus().fireEvent(new FileDownloadRequestEvent(
        UriBuilders.PROJECT_KEYSTORE_ALIAS_CERTIFICATE.create().build(project.getName(), keyPair.getAlias())));
  }

  public void initialize(ProjectDto projectDto) {
    project = projectDto;
    retrieveKeyPairs();
  }

  private void retrieveKeyPairs() {
    ResourceRequestBuilderFactory.<JsArray<KeyDto>>newBuilder() //
        .forResource(UriBuilders.PROJECT_KEYSTORE.create().build(project.getName())) //
        .withCallback(new ResourceCallback<JsArray<KeyDto>>() {
          @Override
          public void onResource(Response response, JsArray<KeyDto> resource) {
            getView().setData(JsArrays.toList(resource));
          }
        }) //
        .get().send();

  }

  @Override
  public void createKeyPair() {
    createKeyPairModalProvider.get().initialize(project, this);
  }

  @Override
  public void importKeyPair() {
    importKeyPairModalProvider.get().initialize(ImportKeyPairModalPresenter.ImportType.KEY_PAIR, project, this);
  }

  @Override
  public void importCertificatePair() {
    importKeyPairModalProvider.get().initialize(ImportKeyPairModalPresenter.ImportType.CERTIFICATE, project, this);
  }

  @Override
  public void saved() {
    retrieveKeyPairs();
  }

  public interface Display extends View, HasUiHandlers<EncryptionKeysUiHandlers> {

    String DOWNLOAD_CERTIFICATE_ACTION = "Certificate";

    HasActionHandler<KeyDto> getActions();

    void setData(@Nonnull List<KeyDto> keyPairs);
  }
}
