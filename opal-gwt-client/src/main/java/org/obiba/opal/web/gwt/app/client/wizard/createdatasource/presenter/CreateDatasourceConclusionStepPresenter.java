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

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter.ResourceClickHandler;
import org.obiba.opal.web.gwt.app.client.widgets.view.ResourceRequestView;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
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

public class CreateDatasourceConclusionStepPresenter extends WidgetPresenter<CreateDatasourceConclusionStepPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HandlerRegistration addReturnClickHandler(ClickHandler handler);

    void setDatasourceRequestDisplay(ResourceRequestPresenter.Display resourceRequestDisplay);

    void setCompleted();

    void setFailed(ClientErrorDto errorDto);
  }

  @Inject
  private Provider<NavigatorPresenter> navigatorPresenter;

  @Inject
  private Provider<CreateDatasourceStepPresenter> createDatasourceStepPresenter;

  private DatasourceDto datasourceDto;

  private WidgetPresenter<?> returnPresenter;

  @Inject
  public CreateDatasourceConclusionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  public void setDatasourceFactory(DatasourceFactoryDto dto) {

    ResourceRequestPresenter<DatasourceDto> resourceRequestPresenter = new ResourceRequestPresenter<DatasourceDto>(new ResourceRequestView(), eventBus, ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasource/" + dto.getName()).put().withResourceBody(DatasourceFactoryDto.stringify(dto)), new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 201) {
          datasourceDto = (DatasourceDto) JsonUtils.unsafeEval(response.getText());
          getDisplay().setCompleted();
          returnPresenter = createDatasourceStepPresenter.get();
        } else if(response.getText() != null && response.getText().length() != 0) {
          GWT.log(response.getText());
          getDisplay().setFailed((ClientErrorDto) JsonUtils.unsafeEval(response.getText()));
        } else {
          getDisplay().setFailed(null);
        }
      }

    });
    resourceRequestPresenter.getDisplay().setResourceName(dto.getName());
    resourceRequestPresenter.getDisplay().setResourceClickHandler(new ResourceClickHandler() {

      @Override
      public String getResourceLink() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public void onClick(ClickEvent arg0) {
        eventBus.fireEvent(new WorkbenchChangeEvent(navigatorPresenter.get()));
        if(datasourceDto != null) {
          eventBus.fireEvent(new DatasourceSelectionChangeEvent(datasourceDto));
        }
      }

    });
    resourceRequestPresenter.setSuccessCodes(201);
    resourceRequestPresenter.setErrorCodes(400, 405, 500);

    getDisplay().setDatasourceRequestDisplay(resourceRequestPresenter.getDisplay());

    resourceRequestPresenter.sendRequest();
  }

  public void setReturnPresenter(WidgetPresenter<?> presenter) {
    this.returnPresenter = presenter;
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addReturnClickHandler(new ReturnClickHandler()));
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  class ReturnClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(new WorkbenchChangeEvent(returnPresenter));
    }

  }

}
