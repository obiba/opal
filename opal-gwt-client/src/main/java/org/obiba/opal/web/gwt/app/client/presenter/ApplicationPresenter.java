/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionEndedEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileExplorerPresenter;
import org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.NavigatorPresenter;
import org.obiba.opal.web.gwt.app.client.report.presenter.ReportTemplatePresenter;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 *
 */
public class ApplicationPresenter extends WidgetPresenter<ApplicationPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasClickHandlers getQuit();

    HasClickHandlers getHelp();

    MenuItem getDatasourcesItem();

    void updateWorkbench(Widget workbench);

    MenuItem getListJobsItem();

    MenuItem getFileExplorer();

    MenuItem getDashboardItem();

    MenuItem getReportsItem();

    MenuItem getUnitsItem();

    void setCurrentSelection(MenuItem selection);
  }

  @Inject
  private Provider<DashboardPresenter> dashboardPresenter;

  @Inject
  private Provider<ReportTemplatePresenter> reportTemplatePresenter;

  @Inject
  private Provider<FunctionalUnitPresenter> functionalUnitPresenter;

  @Inject
  private Provider<NavigatorPresenter> navigationPresenter;

  @Inject
  private Provider<JobListPresenter> jobListPresenter;

  @Inject
  private NotificationPresenter messageDialog;

  @Inject
  private Provider<FileExplorerPresenter> fileExplorerPresenter;

  private WidgetPresenter<?> workbench;

  private boolean unbindPreviousWorkbench;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public ApplicationPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {

    getDisplay().getDashboardItem().setCommand(new Command() {

      @Override
      public void execute() {
        eventBus.fireEvent(new WorkbenchChangeEvent(dashboardPresenter.get()));
        getDisplay().setCurrentSelection(getDisplay().getDashboardItem());
      }
    });

    getDisplay().getReportsItem().setCommand(new Command() {

      @Override
      public void execute() {
        eventBus.fireEvent(new WorkbenchChangeEvent(reportTemplatePresenter.get()));
        getDisplay().setCurrentSelection(getDisplay().getReportsItem());
      }
    });

    getDisplay().getUnitsItem().setCommand(new Command() {

      @Override
      public void execute() {
        eventBus.fireEvent(new WorkbenchChangeEvent(functionalUnitPresenter.get()));
        getDisplay().setCurrentSelection(getDisplay().getUnitsItem());
      }
    });

    getDisplay().getDatasourcesItem().setCommand(new Command() {

      @Override
      public void execute() {
        eventBus.fireEvent(new WorkbenchChangeEvent(navigationPresenter.get()));
        getDisplay().setCurrentSelection(getDisplay().getDatasourcesItem());
      }
    });

    getDisplay().getListJobsItem().setCommand(new Command() {

      @Override
      public void execute() {
        eventBus.fireEvent(new WorkbenchChangeEvent(jobListPresenter.get()));
        getDisplay().setCurrentSelection(getDisplay().getListJobsItem());
      }
    });

    getDisplay().getFileExplorer().setCommand(new Command() {

      @Override
      public void execute() {
        eventBus.fireEvent(new WorkbenchChangeEvent(fileExplorerPresenter.get()));
        getDisplay().setCurrentSelection(getDisplay().getFileExplorer());
      }
    });

    getDisplay().getQuit().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new SessionEndedEvent());
      }
    });

    getDisplay().getHelp().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        HelpUtil.openPage();
      }
    });

    registerWorkbenchChangeEventHandler();

    registerUserMessageEventHandler();
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
    eventBus.fireEvent(new WorkbenchChangeEvent(dashboardPresenter.get()));
    getDisplay().setCurrentSelection(getDisplay().getDashboardItem());
  }

  private void registerWorkbenchChangeEventHandler() {
    super.registerHandler(eventBus.addHandler(WorkbenchChangeEvent.getType(), new WorkbenchChangeEvent.Handler() {

      @Override
      public void onWorkbenchChanged(WorkbenchChangeEvent event) {
        updateWorkbench(event);
      }
    }));
  }

  private void updateWorkbench(WorkbenchChangeEvent event) {
    if(workbench != null && unbindPreviousWorkbench) {
      workbench.unbind();
    }
    unbindPreviousWorkbench = event.shouldUnbindWorkbench();

    workbench = event.getWorkbench();
    if(event.shouldBindWorkbench()) {
      workbench.bind();
    }

    WidgetDisplay wd = (WidgetDisplay) workbench.getDisplay();
    getDisplay().updateWorkbench(wd.asWidget());
    workbench.revealDisplay();
  }

  private void registerUserMessageEventHandler() {
    super.registerHandler(eventBus.addHandler(NotificationEvent.getType(), new NotificationEvent.Handler() {

      @Override
      public void onUserMessage(NotificationEvent event) {
        messageDialog.bind();
        messageDialog.setNotification(event);
        messageDialog.revealDisplay();
      }
    }));
  }
}
