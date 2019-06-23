/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.plugins;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;

import javax.annotation.Nullable;

/**
 *
 */
public class PluginServiceConfigurationModalPresenter
    extends ModalPresenterWidget<PluginServiceConfigurationModalPresenter.Display>
    implements PluginServiceConfigurationModalUiHandlers {

  private String pluginName;

  @Inject
  public PluginServiceConfigurationModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void initialize(String pluginName, String properties) {
    this.pluginName = pluginName;
    getView().setProperties(properties);
  }

  @Override
  public void onSubmit(String properties) {
    getView().setBusy(true);
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.PLUGIN_CONFIG.create().build(pluginName)) //
        .put() //
        .withBody("text/plain", properties) //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setBusy(false);
            if (response.getStatusCode() == Response.SC_OK) {
              getView().hide();
            } else {
              getView().showError(response.getText(), null);
            }
          }
        }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_OK).send();
  }

  public interface Display extends PopupView, HasUiHandlers<PluginServiceConfigurationModalUiHandlers> {

    enum FormField {
      PROPERTIES
    }

    void setProperties(String text);

    void setBusy(boolean busy);

    void showError(String message, @Nullable FormField id);

  }

}
