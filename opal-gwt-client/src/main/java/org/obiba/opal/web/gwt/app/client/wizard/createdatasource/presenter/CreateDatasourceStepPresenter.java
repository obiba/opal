/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.HibernateDatasourceFactoryDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateDatasourceStepPresenter extends WidgetPresenter<CreateDatasourceStepPresenter.Display> {
  //
  // Instance Variables
  //

  @Inject
  private Provider<ApplicationPresenter> applicationPresenter;

  @Inject
  private Provider<DashboardPresenter> dashboardPresenter;

  //
  // Constructors
  //

  @Inject
  public CreateDatasourceStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getDisplay().addCreateClickHandler(new CreateClickHandler()));
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    String getDatasourceName();

    String getDatasourceType();

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCreateClickHandler(ClickHandler handler);
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(new WorkbenchChangeEvent(dashboardPresenter.get()));
      ApplicationPresenter.Display appDisplay = applicationPresenter.get().getDisplay();
      appDisplay.setCurrentSelection(appDisplay.getDashboardItem());
    }
  }

  class CreateClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      String name = getDisplay().getDatasourceName();

      ResourceCallback<DatasourceDto> callback = new ResourceCallback<DatasourceDto>() {

        public void onResource(Response response, DatasourceDto resource) {
          if(response.getStatusCode() == 201) {
            // eventBus.fireEvent(new WorkbenchChangeEvent(dashboardPresenter.get()));
          }
        }
      };

      ResponseCodeCallback errorCallback = new ResponseCodeCallback() {

        public void onResponseCode(Request request, Response response) {
          final ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          GWT.log(errorDto.getStatus());
        }
      };

      DatasourceFactoryDto dto = createDatasourceFactoryDto();
      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasource/" + name).put().withResourceBody(DatasourceFactoryDto.stringify(dto)).withCallback(callback).withCallback(400, errorCallback).withCallback(500, errorCallback).send();
    }

    private DatasourceFactoryDto createDatasourceFactoryDto() {
      DatasourceFactoryDto dto = createHibernateDatasourceFactoryDto();
      dto.setName(getDisplay().getDatasourceName());
      return dto;
    }

    private DatasourceFactoryDto createHibernateDatasourceFactoryDto() {
      HibernateDatasourceFactoryDto extensionDto = HibernateDatasourceFactoryDto.create();

      DatasourceFactoryDto dto = DatasourceFactoryDto.create();
      dto.setExtension(HibernateDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

      return dto;
    }

    private DatasourceFactoryDto createExcelDatasourceFactoryDto(String filePath) {
      ExcelDatasourceFactoryDto extensionDto = ExcelDatasourceFactoryDto.create();
      extensionDto.setFile(filePath);
      extensionDto.setReadOnly(false);

      DatasourceFactoryDto dto = DatasourceFactoryDto.create();
      dto.setExtension(ExcelDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, extensionDto);

      return dto;
    }
  }
}
