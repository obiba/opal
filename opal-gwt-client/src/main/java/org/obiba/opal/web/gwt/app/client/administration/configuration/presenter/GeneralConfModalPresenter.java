/*
* Copyright (c) 2012 OBiBa. All rights reserved.
*
* This program and the accompanying materials
* are made available under the terms of the GNU Public License v3.0.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.obiba.opal.web.gwt.app.client.administration.configuration.presenter;

import org.obiba.opal.web.gwt.app.client.administration.configuration.event.GeneralConfigSavedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.GeneralConf;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class GeneralConfModalPresenter extends ModalPresenterWidget<GeneralConfModalPresenter.Display>
    implements GeneralConfModalUiHandlers {

  @Inject
  public GeneralConfModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    GeneralConf dto = GeneralConf.create();
    dto.setName(getView().getName().getText());
    dto.setDefaultCharSet(getView().getDefaultCharSet().getText());
    dto.setLanguagesArray(getView().getLanguages());

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

  public void setGeneralConf(GeneralConf conf) {
    getView().getName().setText(conf.getName());
    getView().setSelectedCharset(conf.getDefaultCharSet());
    getView().setSelectedLanguages(conf.getLanguagesArray());
  }

  public interface Display extends PopupView, HasUiHandlers<GeneralConfModalUiHandlers> {

    HasText getName();

    HasText getDefaultCharSet();

    void setSelectedCharset(String charset);

    JsArrayString getLanguages();

    void setSelectedLanguages(JsArrayString languages);

  }
}
