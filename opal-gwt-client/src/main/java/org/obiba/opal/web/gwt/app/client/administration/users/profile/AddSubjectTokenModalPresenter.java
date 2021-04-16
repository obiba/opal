/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.users.profile;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.SubjectTokenDto;

import java.util.List;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;

public class AddSubjectTokenModalPresenter extends ModalPresenterWidget<AddSubjectTokenModalPresenter.Display>
    implements AddSubjectTokenModalUiHandlers {

  private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final double PASSWORD_CHARACTERS_LENGTH = PASSWORD_CHARACTERS.length();
  private static final int PASSWORD_MX_LENGTH = 32;

  private final Translations translations;

  private List<String> tokenNames;

  @Inject
  public AddSubjectTokenModalPresenter(Display display, EventBus eventBus, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  @Override
  public void onCreateToken(final SubjectTokenDto token) {
    if (token.getName().trim().isEmpty()) {
      getView().showError(translations.userMessageMap().get("NameIsRequired"));
    } else if (tokenNames.contains(token.getName())) {
      getView().showError(translations.userMessageMap().get("NameMustBeUnique"));
    } else {
      ResponseCodeCallback successCallback = new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          getView().hideDialog();
          getEventBus().fireEvent(new SubjectTokensRefreshEvent(token));
        }
      };

      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.CURRENT_SUBJECT_TOKENS.create().build()) //
          .withResourceBody(SubjectTokenDto.stringify(token)) //
          .withCallback(SC_CREATED, successCallback) //
          .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
          .post().send();
    }
  }

  @Override
  public String onGenerateToken() {
    String generated = "";

    for (int i = 0; i < PASSWORD_MX_LENGTH; i++) {
      generated += PASSWORD_CHARACTERS.charAt((int) Math.floor(Math.random() * PASSWORD_CHARACTERS_LENGTH));
    }

    return generated;
  }

  @Override
  protected void onReveal() {
    initProjects();
  }

  private void initProjects() {
    ResourceRequestBuilderFactory.<JsArray<ProjectDto>>newBuilder()
        .forResource(UriBuilders.PROJECTS.create().query("digest", "true").build()).get()
        .withCallback(new ResourceCallback<JsArray<ProjectDto>>() {
          @Override
          public void onResource(Response response, JsArray<ProjectDto> resource) {
            getView().setProjects(JsArrays.toList(resource));
          }
        }).send();
  }

  public void setTokenNames(List<String> tokenNames) {
    this.tokenNames = tokenNames;
  }

  public interface Display extends PopupView, HasUiHandlers<AddSubjectTokenModalUiHandlers> {

    void setProjects(List<ProjectDto> projects);

    void hideDialog();

    void showError(String message);
  }

}
