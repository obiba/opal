/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.add;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyImportedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.opal.VcsTagsInfoDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

public class TaxonomyGitImportModalPresenter extends ModalPresenterWidget<TaxonomyGitImportModalPresenter.Display>
    implements TaxonomyGitImportModalUiHandlers {

  private static final String MLSTR_USER = "maelstrom-research";

  private static final String MLSTR_REPO = "maelstrom-taxonomies";

  private final Translations translations;

  private ValidationHandler validationHandler;

  @Inject
  public TaxonomyGitImportModalPresenter(EventBus eventBus, Display display, Translations translations) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.translations = translations;
  }

  public TaxonomyGitImportModalPresenter showMaelstromForm() {
    getView().showMaelstromForm(MLSTR_USER, MLSTR_REPO);
    getTags(MLSTR_USER, MLSTR_REPO);
    return this;
  }

  @Override
  public void onBind() {
    validationHandler = new TaxonomyGitImportValidationHandler();
  }

  @Override
  public void onImport(String user, String repository, String reference, String file, boolean override) {
    if (!validationHandler.validate()) return;

    UriBuilder uriBuilder = UriBuilders.SYSTEM_CONF_TAXONOMIES_IMPORT_GITHUB.create().query("user", user)
        .query("repo", repository).query("override", String.valueOf(override));

    if (!Strings.isNullOrEmpty(reference)) uriBuilder.query("ref", reference);
    if (!Strings.isNullOrEmpty(file)) uriBuilder.query("file", file);

    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build())
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            getEventBus().fireEvent(new TaxonomyImportedEvent());
          }
        }, Response.SC_OK, Response.SC_CREATED) //
        .withCallback(new TaxonomyResourceErrorHandler(getView(), translations, "TaxonomyImportFailed"),
            Response.SC_CONFLICT, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR) /**/.post().send();
  }

  private void getTags(String user, String repository) {
    UriBuilder uriBuilder = UriBuilders.SYSTEM_CONF_TAXONOMIES_TAGS_GITHUB.create().query("user", user)
        .query("repo", repository);

    ResourceRequestBuilderFactory.<VcsTagsInfoDto>newBuilder()
        .forResource(uriBuilder.build())
        .withCallback(new ResourceCallback<VcsTagsInfoDto>() {
          @Override
          public void onResource(Response response, VcsTagsInfoDto resource) {
            JsArrayString tags = JsArrays.toSafeArray(resource.getNamesArray());

            if (tags.length() < 1) {
              getView().showError("TaxonomyNoTagsFound");
            } else {
              getView().addTags(tags);
            }

          }
        })
        .withCallback(new TaxonomyResourceErrorHandler(getView(), translations, "TaxonomyTagsImportFailed"), Response.SC_CONFLICT,
            Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR).get().send();
  }

  private static class TaxonomyResourceErrorHandler implements ResponseCodeCallback {

    private final Translations translations;

    private final Display view;

    private final String defaultMessage;

    TaxonomyResourceErrorHandler(Display view, Translations translations, String defaultMessage) {
      this.translations = translations;
      this.view = view;
      this.defaultMessage = defaultMessage;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      String message = response.getText();
      ClientErrorDto errorDto = Strings.isNullOrEmpty(message) ? null : (ClientErrorDto) JsonUtils.unsafeEval(message);

      if (errorDto != null) {
        view.showError(null, TranslationsUtils.replaceArguments(
            translations.userMessageMap().get(errorDto.getStatus()),
            JsArrays.toList(errorDto.getArgumentsArray())));
      } else if (!Strings.isNullOrEmpty(message)) {
        view.showError(null, message);
      } else {
        view.showError(defaultMessage);
      }
    }
  }

  public interface Display extends PopupView, HasUiHandlers<TaxonomyGitImportModalUiHandlers> {

    HasText getUser();

    HasText getRepository();

    void addTags(JsArrayString tagNames);

    void showMaelstromForm(String user, String repo);

    enum FormField {
      USER, REPOSITORY
    }

    void hideDialog();

    void showError(String messageKey);

    void showError(@Nullable FormField formField, String message);

    void clearErrors();
  }

  private class TaxonomyGitImportValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if (validators != null) {
        return validators;
      }

      validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getView().getUser(), "TaxonomyGitUserRequired",
          Display.FormField.USER.name()));
      validators.add(new RequiredTextValidator(getView().getRepository(), "TaxonomyGitRepositoryRequired", Display.FormField.REPOSITORY.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }

  }
}
