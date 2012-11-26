/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.datasource.presenter;

import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.HibernateDatasourceFactoryDto;
import org.obiba.opal.web.model.client.opal.JdbcDataSourceDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class HibernateDatasourceFormPresenter extends
    PresenterWidget<HibernateDatasourceFormPresenter.Display> implements DatasourceFormPresenter {

  public static class Subscriber extends DatasourceFormPresenterSubscriber {

    @Inject
    public Subscriber(EventBus eventBus, HibernateDatasourceFormPresenter presenter) {
      super(eventBus, presenter);
    }

  }

  @Inject
  public HibernateDatasourceFormPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  public PresenterWidget<? extends Display> getPresenter() {
    return this;
  }

  @Override
  public DatasourceFactoryDto getDatasourceFactory() {
    HibernateDatasourceFactoryDto extensionDto = HibernateDatasourceFactoryDto.create();

    String database = getView().getSelectedDatabase();
    if(database != null) {
      extensionDto.setDatabase(database);
    }

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(HibernateDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

    return dto;
  }

  @Override
  public boolean isForType(String type) {
    return type.equalsIgnoreCase("hibernate");
  }

  @Override
  public boolean validateFormData() {
    return true;
  }

  @Override
  public void clearForm() {
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    ResourceRequestBuilderFactory.<JsArray<JdbcDataSourceDto>>newBuilder().forResource("/jdbc/databases")
        .withCallback(new ResourceCallback<JsArray<JdbcDataSourceDto>>() {

          @Override
          public void onResource(Response response, JsArray<JdbcDataSourceDto> resource) {
            getView().setDatabases(resource);
          }
        }).get().send();
  }

  public interface Display extends DatasourceFormPresenter.Display {

    String getSelectedDatabase();

    void setDatabases(JsArray<JdbcDataSourceDto> databases);
  }

}
