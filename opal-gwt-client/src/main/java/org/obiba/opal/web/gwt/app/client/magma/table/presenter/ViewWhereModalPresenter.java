/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.table.presenter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.magma.VariableListViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_OK;

/**
 *
 */
public class ViewWhereModalPresenter extends ModalPresenterWidget<ViewWhereModalPresenter.Display>
    implements ViewWhereModalUiHandlers {

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  private final Translations translations;

  private final PlaceManager placeManager;

  private ViewDto view;

  @Inject
  public ViewWhereModalPresenter(EventBus eventBus, Display display, Translations translations,
      PlaceManager placeManager) {
    super(eventBus, display);
    this.translations = translations;
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  /**
   * Will update the view table.
   *
   * @param view
   */

  public void initialize(ViewDto view) {
    this.view = view;
    getView().renderProperties(view);
  }

  @Override
  public void onSave(String script) {
    compileAndSaveScript(script);
  }

  private void compileAndSaveScript(final String script) {
    String uri = UriBuilder.create() //
        .segment("datasource", "{}", "view", "{}", "variable", "_transient", "_compile") //
        .query("valueType", "boolean", //
            "repeatable", "false") //
        .build(view.getDatasourceName(), view.getName());

    ResourceRequestBuilderFactory.newBuilder().forResource(uri) //
        .withFormBody("script", script) //
        .withCallback(new CompileCallback(script), SC_BAD_REQUEST, SC_OK) //
        .post() //
        .send();
  }

  private void doSave(String script) {
    ViewDto dto = getViewDto(script);

    UriBuilder ub = UriBuilders.DATASOURCE_VIEW.create().query("comment", translations.updateWhereComment());
    ResourceRequestBuilderFactory.newBuilder().put().forResource(ub.build(view.getDatasourceName(), view.getName()))
        .withResourceBody(ViewDto.stringify(dto)).withCallback(new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK) {
          getView().hide();
          placeManager.revealCurrentPlace();
        } else if(response.getStatusCode() == Response.SC_FORBIDDEN) {
          getView().showError(translations.userMessageMap().get("UnauthorizedOperation"), null);
        } else {
          ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
          getView().showError(TranslationsUtils
              .replaceArguments(translations.userMessageMap().get(error.getStatus()), error.getArgumentsArray()), null);
        }
      }
    }, Response.SC_OK, Response.SC_BAD_REQUEST, Response.SC_FORBIDDEN).send();
  }

  private ViewDto getViewDto(String script) {
    ViewDto v = ViewDto.create();
    v.setName(view.getName());
    v.setFromArray(view.getFromArray());
    if(!Strings.isNullOrEmpty(script)) v.setWhere(script);

    v.setExtension(VariableListViewDto.ViewDtoExtensions.view,
        view.getExtension(VariableListViewDto.ViewDtoExtensions.view));

    return v;
  }

  public interface Display extends PopupView, HasUiHandlers<ViewWhereModalUiHandlers> {

    enum FormField {
      NAME,
      TABLES
    }

    void renderProperties(ViewDto view);

    void showError(String message, @Nullable FormField id);
  }

  private class CompileCallback implements ResponseCodeCallback {
    private final String script;

    public CompileCallback(String script) {this.script = script;}

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == SC_OK) {
        doSave(script);
      } else {
        showError(response);
      }
    }

    private void showError(Response response) {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
      if(errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
        List<JavaScriptErrorDto> errors = extractJavaScriptErrors(errorDto);
        for(JavaScriptErrorDto error : errors) {
          getView().showError(
              translationMessages.errorAt(error.getLineNumber(), error.getColumnNumber(), error.getMessage()), null);
        }
      } else {
        getView().showError(TranslationsUtils
            .replaceArguments(translations.userMessageMap().get(errorDto.getStatus()), errorDto.getArgumentsArray()),
            null);
      }
    }

    private List<JavaScriptErrorDto> extractJavaScriptErrors(ClientErrorDto errorDto) {
      List<JavaScriptErrorDto> javaScriptErrors = new ArrayList<JavaScriptErrorDto>();

      JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) errorDto
          .getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors);
      if(errors != null) {
        for(int i = 0; i < errors.length(); i++) {
          javaScriptErrors.add(errors.get(i));
        }
      }
      return javaScriptErrors;
    }
  }
}
