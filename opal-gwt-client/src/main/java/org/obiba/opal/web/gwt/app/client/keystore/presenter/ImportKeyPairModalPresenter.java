/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.keystore.presenter.commands.ImportKeyPairCommand;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.commands.KeystoreCommand;
import org.obiba.opal.web.gwt.app.client.keystore.support.KeyPairModalResponseCallback;
import org.obiba.opal.web.gwt.app.client.keystore.support.KeystoreType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.KeyType;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class ImportKeyPairModalPresenter extends ModalPresenterWidget<ImportKeyPairModalPresenter.Display>
    implements KeyPairModalUiHandlers {

  private static final String DEFAULT_ALIAS = "https";

  private boolean updateKeyPair = false;

  public enum ImportType {
    KEY_PAIR,
    CERTIFICATE
  }

  private KeystoreType keystoreType;

  private ImportType importType;

  private KeyPairModalSavedHandler savedHandler;

  private String requestUrl;

  @Inject
  public ImportKeyPairModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void save() {
    getView().clearErrors();

    if(new ViewValidator().validate()) {
      KeystoreCommand command = createCommand();
      ResponseCodeCallback callback = new KeyPairModalResponseCallback(getView(), savedHandler);
      command.execute(callback, callback);
    }
  }

  public void initialize(ImportType type, ProjectDto projectDto, KeyPairModalSavedHandler handler) {
    savedHandler = handler;
    keystoreType = KeystoreType.PROJECT;
    importType = type;
    requestUrl = UriBuilders.PROJECT_KEYSTORE.create().build(projectDto.getName());
    getView().setType(keystoreType, type);
  }

  public void initialize(ImportType type, KeyPairModalSavedHandler handler) {
    savedHandler = handler;
    keystoreType = KeystoreType.SYSTEM;
    importType = type;
    requestUrl = UriBuilders.SYSTEM_KEYSTORE.create().build();
    updateKeyPair = true;
    getView().setType(keystoreType, type);
  }

  private KeystoreCommand createCommand() {
    String alias = keystoreType == KeystoreType.PROJECT ? getView().getName().getText() : DEFAULT_ALIAS;

    if(importType == ImportType.KEY_PAIR) {
      return ImportKeyPairCommand.Builder.newBuilder()//
          .setUrl(requestUrl)//
          .setAlias(alias)//
          .setPublicKey(getView().getPublicKey().getText())//
          .setPrivateKey(getView().getPrivateKey().getText())//
          .setKeyType(KeyType.KEY_PAIR)//
          .setUpdate(updateKeyPair)//
          .build();
    }

    return ImportKeyPairCommand.Builder.newBuilder()//
        .setUrl(requestUrl)//
        .setAlias(alias)//
        .setPublicKey(getView().getPublicKey().getText())//
        .setKeyType(KeyType.CERTIFICATE)//
        .setUpdate(updateKeyPair)//
        .build();
  }

  private final class ViewValidator extends ViewValidationHandler {

    private ViewValidator() {}

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<>();

      if(keystoreType == KeystoreType.PROJECT) {
        validators.add(
            new RequiredTextValidator(getView().getName(), "KeyPairAliasIsRequired", Display.FormField.NAME.name()));
      }

      if(importType == ImportType.KEY_PAIR) {
        validators.add(new RequiredTextValidator(getView().getPrivateKey(), "KeyPairPrivateKeyPEMIsRequired",
            Display.FormField.PRIVATE_KEY.name()));
      }

      validators.add(new RequiredTextValidator(getView().getPublicKey(), "KeyPairPublicKeyPEMIsRequired",
          Display.FormField.PUBLIC_KEY.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  public interface Display extends KeyPairDisplay<Display.FormField>, PopupView, HasUiHandlers<KeyPairModalUiHandlers> {

    enum FormField {
      NAME,
      PUBLIC_KEY,
      PRIVATE_KEY
    }

    HasText getName();

    HasText getPublicKey();

    HasText getPrivateKey();

    void setType(KeystoreType kType, ImportType type);
  }
}

