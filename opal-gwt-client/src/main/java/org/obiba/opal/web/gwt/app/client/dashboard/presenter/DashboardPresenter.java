/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.dashboard.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class DashboardPresenter extends WidgetPresenter<DashboardPresenter.Display> {

  //
  // Instance Variables
  //

  @Inject
  private Provider<DataImportPresenter> dataImportPresenter;

  @Inject
  private Provider<DataExportPresenter> dataExportPresenter;

  @Inject
  private Provider<NavigatorPresenter> navigationPresenter;

  //
  // Constructors
  //

  @Inject
  public DashboardPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().getImportDataLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new WorkbenchChangeEvent(dataImportPresenter.get()));
      }
    }));

    super.registerHandler(getDisplay().getExportDataLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new WorkbenchChangeEvent(dataExportPresenter.get()));
      }
    }));

    super.registerHandler(getDisplay().getExploreVariablesLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new WorkbenchChangeEvent(navigationPresenter.get()));
      }
    }));
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
    ResourceRequestBuilderFactory.newBuilder().forResource("/participants/count").get().withCallback(200, new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        GWT.log(response.getText());
        getDisplay().setParticipantCount(Integer.parseInt(response.getText()));
      }
    }).send();

  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {
    void setParticipantCount(int count);

    HasClickHandlers getExploreVariablesLink();

    HasClickHandlers getImportDataLink();

    HasClickHandlers getExportDataLink();
  }

}
