/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.sql;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.rest.client.RequestUrlBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

public class SQLPresenter extends PresenterWidget<SQLPresenter.Display> implements SQLUiHandlers {

  private DatasourceDto datasource;

  private final Translations translations;

  private final RequestUrlBuilder urlBuilder;

  @Inject
  public SQLPresenter(EventBus eventBus, Display view, Translations translations, RequestUrlBuilder urlBuilder) {
    super(eventBus, view);
    this.translations = translations;
    this.urlBuilder = urlBuilder;
    getView().setUiHandlers(this);
  }

  public void initialize(DatasourceDto datasource) {
    this.datasource = datasource;
    getView().setDatasource(datasource);
    getView().clear();
  }

  @Override
  public void execute(String query) {
    getView().startExecute();
    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.DATASOURCE_SQL.create().build(datasource.getName()))
        .withBody("text/plain", query)
        .accept("application/json")
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if (response.getStatusCode() == Response.SC_OK) {
              SQLResult result = JsonUtils.<SQLResult>safeEval(response.getText());
              getView().showResult(result);
            } else {
              try {
                ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
                String msg = errorDto.getStatus();
                if(translations.userMessageMap().containsKey(msg)) msg = translations.userMessageMap().get(errorDto.getStatus());
                if (errorDto.getArgumentsArray() != null) {
                  for (int i = 0; i<errorDto.getArgumentsArray().length(); i++) {
                    msg = msg.replace("{" + i + "}", errorDto.getArgumentsArray().get(i));
                  }
                }
                getView().showError(msg);
              } catch(Exception ignored) {
              }
            }
            getView().endExecute();
          }
        }, Response.SC_OK, Response.SC_BAD_REQUEST, Response.SC_FORBIDDEN, Response.SC_NOT_FOUND, Response.SC_INTERNAL_SERVER_ERROR)
        .post().send();
  }

  @Override
  public void download() {
    String url = urlBuilder.buildAbsoluteUrl(UriBuilders.DATASOURCE_SQL.create().build(datasource.getName()));
    GWT.log(url);
    getView().doDownload(url);
  }

  public interface Display extends View, HasUiHandlers<SQLUiHandlers> {

    void clear();

    void startExecute();

    void showResult(SQLResult result);

    void showError(String text);

    void endExecute();

    void setDatasource(DatasourceDto datasource);

    void doDownload(String url);
  }
}
