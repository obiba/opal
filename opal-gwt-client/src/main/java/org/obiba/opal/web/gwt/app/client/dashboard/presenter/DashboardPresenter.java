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
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplatePresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

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
  private Provider<NavigatorPresenter> navigationPresenter;

  @Inject
  private Provider<FileExplorerPresenter> fileExplorerPresenter;

  @Inject
  private Provider<JobListPresenter> jobListPresenter;

  @Inject
  private Provider<ReportTemplatePresenter> reportTemplatePresenter;

  @Inject
  private Provider<FunctionalUnitPresenter> functionalUnitPresenter;

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

    super.registerHandler(getDisplay().getDatasourcesLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new WorkbenchChangeEvent(navigationPresenter.get()));
      }
    }));

    super.registerHandler(getDisplay().getFilesLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new WorkbenchChangeEvent(fileExplorerPresenter.get()));
      }
    }));

    super.registerHandler(getDisplay().getJobsLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new WorkbenchChangeEvent(jobListPresenter.get()));
      }
    }));

    super.registerHandler(getDisplay().getReportsLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new WorkbenchChangeEvent(reportTemplatePresenter.get()));
      }
    }));

    super.registerHandler(getDisplay().getUnitsLink().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new WorkbenchChangeEvent(functionalUnitPresenter.get()));
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
    authorize();
    ResourceRequestBuilderFactory.newBuilder().forResource("/participants/count").get().withCallback(200, new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        getDisplay().setParticipantCount(Integer.parseInt(response.getText()));
      }
    }).send();

  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasources").get().authorize(getDisplay().getDatasourcesAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units").get().authorize(getDisplay().getUnitsAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-templates").get().authorize(getDisplay().getReportsAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files/meta").get().authorize(getDisplay().getFilesAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/commands").get().authorize(getDisplay().getJobsAuthorizer()).send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {
    void setParticipantCount(int count);

    //
    // Click handlers
    //

    HasClickHandlers getUnitsLink();

    HasClickHandlers getDatasourcesLink();

    HasClickHandlers getFilesLink();

    HasClickHandlers getJobsLink();

    HasClickHandlers getReportsLink();

    //
    // Authorization
    //

    HasAuthorization getUnitsAuthorizer();

    HasAuthorization getDatasourcesAuthorizer();

    HasAuthorization getFilesAuthorizer();

    HasAuthorization getJobsAuthorizer();

    HasAuthorization getReportsAuthorizer();

  }

}
