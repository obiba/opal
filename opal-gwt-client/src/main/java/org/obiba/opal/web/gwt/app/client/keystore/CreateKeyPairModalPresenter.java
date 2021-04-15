/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.keystore.commands.CreateKeyPairCommand;
import org.obiba.opal.web.gwt.app.client.keystore.commands.KeystoreCommand;
import org.obiba.opal.web.gwt.app.client.keystore.support.KeyPairModalResponseCallback;
import org.obiba.opal.web.gwt.app.client.keystore.support.KeystoreType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class CreateKeyPairModalPresenter extends ModalPresenterWidget<CreateKeyPairModalPresenter.Display>
    implements KeyPairModalUiHandlers {

  private static final String DEFAULT_ALIAS = "https";

  private KeystoreType keystoreType;

  private boolean updateKeyPair = false;

  private KeyPairModalSavedHandler savedHandler;

  private String requestUrl;

  @Inject
  public CreateKeyPairModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    getView().clearErrors();
    if(new ViewValidator().validate()) {
      String alias = keystoreType == KeystoreType.PROJECT ? getView().getName().getText() : DEFAULT_ALIAS;
      KeystoreCommand command = CreateKeyPairCommand.Builder.newBuilder().setUrl(requestUrl)//
          .setAlias(alias)//
          .setAlgorithm(getView().getAlgorithm().getText())//
          .setSize(getView().getSize().getText())//
          .setFirstLastName(getView().getFirstLastName().getText())//
          .setOrganization(getView().getOrganization().getText())//
          .setOrganizationalUnit(getView().getOrganizationalUnit().getText())//
          .setLocality(getView().getLocality().getText())//
          .setState(getView().getState().getText())//
          .setCountry(getView().getCountry().getText())//
          .setUpdate(updateKeyPair).build();

      ResponseCodeCallback callback = new KeyPairModalResponseCallback(getView(), savedHandler);
      command.execute(callback, callback);
    }
  }

  public void initialize(@Nonnull ProjectDto projectDto, @Nonnull KeyPairModalSavedHandler handler) {
    savedHandler = handler;
    keystoreType = KeystoreType.PROJECT;
    requestUrl = UriBuilders.PROJECT_KEYSTORE.create().build(projectDto.getName());
    getView().setType(keystoreType);
  }

  public void initialize(KeyPairModalSavedHandler handler) {
    savedHandler = handler;
    keystoreType = KeystoreType.SYSTEM;
    updateKeyPair = true;
    requestUrl = UriBuilders.SYSTEM_KEYSTORE.create().build();
    getView().setType(keystoreType);
  }

  private final class ViewValidator extends ViewValidationHandler {

    private ViewValidator() {}

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      if(keystoreType == KeystoreType.PROJECT) {
        validators.add(
            new RequiredTextValidator(getView().getName(), "KeyPairAliasIsRequired", Display.FormField.NAME.name()));
      }

      validators.add(new RequiredTextValidator(getView().getAlgorithm(), "KeyPairAlgorithmIsRequired",
          Display.FormField.ALGORITHM.name()));
      validators.add(
          new RequiredTextValidator(getView().getSize(), "KeyPairKeySizeIsRequired", Display.FormField.SIZE.name()));
      validators.add(
          new RegExValidator(getView().getSize(), "^\\d+$", "KeyPairKeySizeNumeric", Display.FormField.SIZE.name()));
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
      ALGORITHM,
      SIZE,
    }

    HasText getName();

    HasText getAlgorithm();

    HasText getSize();

    HasText getFirstLastName();

    HasText getOrganization();

    HasText getOrganizationalUnit();

    HasText getLocality();

    HasText getState();

    HasText getCountry();

    void setType(KeystoreType type);
  }

}