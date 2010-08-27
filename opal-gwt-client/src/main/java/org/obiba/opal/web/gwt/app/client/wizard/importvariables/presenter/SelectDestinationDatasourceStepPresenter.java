/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class SelectDestinationDatasourceStepPresenter extends WidgetPresenter<SelectDestinationDatasourceStepPresenter.Display> {

  @Inject
  private Provider<UploadVariablesStepPresenter> uploadVariablesStepPresenterProvider;

  @Inject
  private Provider<ComparedDatasourcesReportStepPresenter> comparedDatasourcesReportPresenterProvider;

  private String sourceDatasourceName;

  @Inject
  public SelectDestinationDatasourceStepPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public interface Display extends WidgetDisplay {

    String getSelectedDatasource();

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    void setDatasources(JsArray<DatasourceDto> datasources);

  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    initDatasources();
  }

  private void initDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
        getDisplay().setDatasources(datasources);
      }
    }).send();

  }

  private void addEventHandlers() {
    registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
    registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
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

  public void setSourceDatasourceName(String sourceDatasourceName) {
    this.sourceDatasourceName = sourceDatasourceName;
  }

  class NextClickHandler implements ClickHandler {
    public void onClick(ClickEvent event) {
      String selectedDatasourceName = getDisplay().getSelectedDatasource();
      if(selectedDatasourceName.equals("")) {
        eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, "datasourceMustBeSelected", null));
      } else {
        ComparedDatasourcesReportStepPresenter compareDatasourcesReportPresenter = comparedDatasourcesReportPresenterProvider.get();
        compareDatasourcesReportPresenter.setSourceDatasourceName(sourceDatasourceName);
        compareDatasourcesReportPresenter.setTargetDatasourceName(selectedDatasourceName);
        compareDatasourcesReportPresenter.getDisplay().clearDisplay();
        eventBus.fireEvent(new WorkbenchChangeEvent(compareDatasourcesReportPresenter));
      }
    }
  }

  class CancelClickHandler implements ClickHandler {
    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new WorkbenchChangeEvent(uploadVariablesStepPresenterProvider.get()));
    }
  }

}
