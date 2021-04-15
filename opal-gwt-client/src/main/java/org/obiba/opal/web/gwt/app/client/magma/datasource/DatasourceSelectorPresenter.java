/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.datasource;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Widget for selecting an Opal datasource.
 */
public class DatasourceSelectorPresenter extends PresenterWidget<DatasourceSelectorPresenter.Display> {
  //
  // Instance Variables
  //

  private DatasourcesRefreshedCallback datasourcesRefreshedCallback;

  //
  // Constructors
  //

  @Inject
  public DatasourceSelectorPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @Override
  public void onReset() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {

          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            JsArray<DatasourceDto> datasources = resource != null
                ? resource
                : (JsArray<DatasourceDto>) JsArray.createArray();
            getView().setDatasources(datasources);

            if(datasourcesRefreshedCallback != null) {
              datasourcesRefreshedCallback.onDatasourcesRefreshed();
            }
          }
        }).send();
  }

  //
  // Methods
  //

  public String getSelection() {
    return getView().getSelection();
  }

  public DatasourceDto getSelectionDto() {
    return getView().getSelectionDto();
  }

  public void setSelection(String datasourceName) {
    getView().setSelection(datasourceName);
  }

  public void setDatasourcesRefreshedCallback(DatasourcesRefreshedCallback datasourcesRefreshedCallback) {
    this.datasourcesRefreshedCallback = datasourcesRefreshedCallback;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    void setEnabled(boolean enabled);

    void setDatasources(JsArray<DatasourceDto> datasources);

    void selectFirst();

    void setSelection(String datasourceName);

    String getSelection();

    DatasourceDto getSelectionDto();
  }

  public interface DatasourcesRefreshedCallback {

    void onDatasourcesRefreshed();
  }
}