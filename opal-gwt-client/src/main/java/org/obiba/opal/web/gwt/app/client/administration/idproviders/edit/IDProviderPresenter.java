/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.idproviders.edit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.administration.idproviders.event.IDProvidersRefreshEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.validator.*;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.IDProviderDto;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_OK;

public class IDProviderPresenter extends ModalPresenterWidget<IDProviderPresenter.Display>
    implements IDProviderUiHandlers {

  private final static Translations translations = GWT.create(Translations.class);

  private final TranslationMessages translationMessages;

  protected ValidationHandler validationHandler;

  private Mode dialogMode;


  public enum Mode {
    UPDATE, CREATE
  }

  @Inject
  public IDProviderPresenter(Display display, EventBus eventBus, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    getView().setUiHandlers(this);
  }

  @Override
  public void onBind() {
    validationHandler = new IDProviderValidationHandler();
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void save(IDProviderDto provider) {

    getView().clearErrors();

    if (validationHandler.validate()) {

      ResponseCodeCallback successCallback = new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          getEventBus().fireEvent(new IDProvidersRefreshEvent());
          getView().hideDialog();
        }
      };

      switch (dialogMode) {
        case CREATE:
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource(UriBuilders.ID_PROVIDERS.create().build()) //
              .withResourceBody(IDProviderDto.stringify(provider)) //
              .withCallback(SC_CREATED, successCallback) //
              .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
              .post().send();
          break;
        case UPDATE:
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource(UriBuilders.ID_PROVIDER.create().build(provider.getName())) //
              .withResourceBody(IDProviderDto.stringify(provider)) //
              .withCallback(SC_OK, successCallback) //
              .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
              .put().send();
          break;
      }
    }
  }

  public void setDialogMode(Mode mode) {
    dialogMode = mode;
    if (Mode.CREATE.equals(mode)) {
      getView().setTitle(translations.addIDProvider());
    }
  }

  public void setIDProvider(IDProviderDto provider) {
    if (Mode.UPDATE.equals(dialogMode)) {
      getView().setTitle(translationMessages.editIDProviderLabel(provider.getName()));
    }
    getView().setIDProvider(provider, dialogMode);
  }

  private class IDProviderValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if (validators != null) {
        return validators;
      }

      validators = new LinkedHashSet<FieldValidator>();
      if (dialogMode == Mode.CREATE) {
        validators.add(new RequiredTextValidator(getView().getName(), "IDProviderNameIsRequired",
            Display.FormField.NAME.name()));
      }
      validators.add(new RequiredTextValidator(getView().getClientId(), "IDProviderClientIdIsRequired", Display.FormField.CLIENT_ID.name()));
      validators.add(new RequiredTextValidator(getView().getSecret(), "IDProviderClientSecretIsRequired", Display.FormField.SECRET.name()));
      validators.add(new RequiredTextValidator(getView().getDiscoveryUri(), "IDProviderDiscoveryUriIsRequired", Display.FormField.DISCOVERY_URI.name()));
      validators.add(new RegExValidator(getView().getDiscoveryUri(), "^http[s]*://","IDProviderDiscoveryUriIsUri", Display.FormField.DISCOVERY_URI.name()));
      validators.add(new RegExValidator(getView().getProviderUrl(), "(^$)|(^http[s]*://)","IDProviderProviderUrlIsUri", Display.FormField.PROVIDER_URL.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  public interface Display extends PopupView, HasUiHandlers<IDProviderUiHandlers> {

    enum FormField {
      NAME, CLIENT_ID, SECRET, DISCOVERY_URI, PROVIDER_URL
    }

    void hideDialog();

    void setTitle(String title);

    void setIDProvider(IDProviderDto provider, Mode dialogMode);

    HasText getName();

    HasText getClientId();

    HasText getSecret();

    HasText getDiscoveryUri();

    HasText getProviderUrl();

    void showError(@Nullable FormField formField, String message);

    void clearErrors();

  }

}
