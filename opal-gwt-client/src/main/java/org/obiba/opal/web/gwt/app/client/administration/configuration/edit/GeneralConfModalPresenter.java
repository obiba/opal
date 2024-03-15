/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.configuration.edit;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.OpalSystemCache;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.client.opal.GeneralConf;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static org.obiba.opal.web.gwt.app.client.administration.configuration.edit.GeneralConfModalPresenter.Display.FormField;

public class GeneralConfModalPresenter extends ModalPresenterWidget<GeneralConfModalPresenter.Display>
    implements GeneralConfModalUiHandlers {

  private final OpalSystemCache opalSystemCache;

  protected ValidationHandler validationHandler;

  @Inject
  public GeneralConfModalPresenter(Display display, EventBus eventBus, OpalSystemCache opalSystemCache) {
    super(eventBus, display);
    this.opalSystemCache = opalSystemCache;
    getView().setUiHandlers(this);
    validationHandler = new GeneralConfValidationHandler();
  }

  @Override
  public void save(String name, String defaultCharSet, JsArrayString languages, String publicUrl, String logoutUrl, boolean enforced2FA) {
    opalSystemCache.clearGeneralConf();
    getView().clearErrors();
    if(validationHandler.validate()) {
      GeneralConf dto = GeneralConf.create();
      dto.setName(name);
      dto.setDefaultCharSet(defaultCharSet);
      dto.setLanguagesArray(languages);
      dto.setPublicURL(publicUrl);
      dto.setLogoutURL(logoutUrl);
      dto.setEnforced2FA(enforced2FA);

      ResourceRequestBuilderFactory.<GeneralConf>newBuilder().forResource("/system/conf/general")
          .withResourceBody(GeneralConf.stringify(dto)).withCallback(new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_OK) {
            getView().hide();
            fireEvent(new GeneralConfigSavedEvent());
          } else {
            fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
          }
        }
      }, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_NOT_FOUND).put().send();
    }
  }

  public void setGeneralConf(GeneralConf conf) {
    getView().setGeneralConf(conf);
  }

  private class GeneralConfValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired", FormField.NAME.name()));
        validators.add(
            new ConditionValidator(languagesSizeCondition(), "LanguageIsRequired", Display.FormField.LANGUAGES.name()));
        validators.add(new RequiredTextValidator(getView().getDefaultCharSet(), "DefaultCharSetIsRequired",
            FormField.DEFAULT_CHARSET.name()));
      }
      return validators;
    }

    private HasValue<Boolean> languagesSizeCondition() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return JsArrays.toSafeArray(getView().getLanguages()).length() > 0;
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(FormField.valueOf(id), message);
    }

  }

  public interface Display extends PopupView, HasUiHandlers<GeneralConfModalUiHandlers> {

    enum FormField {
      NAME,
      DEFAULT_CHARSET,
      LANGUAGES,
    }
    void setGeneralConf(GeneralConf conf);

    HasText getName();

    HasText getDefaultCharSet();

    JsArrayString getLanguages();

    void showError(@Nullable FormField formField, String message);

    void clearErrors();
  }
}
