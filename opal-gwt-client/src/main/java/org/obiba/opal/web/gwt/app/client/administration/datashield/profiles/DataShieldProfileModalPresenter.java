/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldProfileAddedEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

import java.util.List;

public class DataShieldProfileModalPresenter extends ModalPresenterWidget<DataShieldProfileModalPresenter.Display>
    implements DataShieldProfileModalUiHandlers {

  @Inject
  public DataShieldProfileModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save(final DataShieldProfileDto profile) {
    getView().clear();
    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        getView().hideDialog();
        if (response.getStatusCode() == Response.SC_CREATED) {
          fireEvent(new DataShieldProfileAddedEvent(profile));
        }
      }
    };
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASHIELD_PROFILES.create().build())
        .post()
        .withResourceBody(DataShieldProfileDto.stringify(profile))
        .withCallback(Response.SC_CREATED, callbackHandler).send();
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  public void initialize(List<RServerClusterDto> clusters) {
    getView().setClusters(clusters);
  }

  //
  // Inner classes and interfaces
  //

  public interface Display extends PopupView, HasUiHandlers<DataShieldProfileModalUiHandlers> {

    void hideDialog();

    void setClusters(List<RServerClusterDto> clusters);

    void clear();

  }

}
