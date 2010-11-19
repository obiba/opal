/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewCreationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.UploadVariablesStepPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class NavigatorPresenter extends WidgetPresenter<NavigatorPresenter.Display> {

  public interface Display extends WidgetDisplay {

    void setTreeDisplay(NavigatorTreePresenter.Display treeDisplay);

    void addCreateDatasourceClickHandler(ClickHandler handler);

    HasWidgets getDetailsPanel();

    void addExportDataClickHandler(ClickHandler handler);

    void addImportDataClickHandler(ClickHandler handler);

    void addImportVariablesClickHandler(ClickHandler handler);

    void addAddViewClickHandler(ClickHandler handler);
  }

  @Inject
  private Provider<UploadVariablesStepPresenter> importVariablesPresenter;

  @Inject
  private Provider<DataImportPresenter> importDataPresenter;

  @Inject
  private NavigatorTreePresenter navigatorTreePresenter;

  @Inject
  private DatasourcePresenter datasourcePresenter;

  @Inject
  private TablePresenter tablePresenter;

  @Inject
  private VariablePresenter variablePresenter;

  @Inject
  private CreateDatasourcePresenter createDatasourcePresenter;

  @Inject
  private Provider<DataExportPresenter> dataExportPresenter;

  @Inject
  public NavigatorPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    navigatorTreePresenter.bind();
    datasourcePresenter.bind();
    tablePresenter.bind();
    variablePresenter.bind();
    createDatasourcePresenter.bind();

    getDisplay().setTreeDisplay(navigatorTreePresenter.getDisplay());

    getDisplay().addCreateDatasourceClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        createDatasourcePresenter.revealDisplay();
      }
    });

    getDisplay().addExportDataClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        DataExportPresenter presenter = dataExportPresenter.get();
        presenter.revealDisplay();
      }
    });

    getDisplay().addImportVariablesClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        eventBus.fireEvent(new WorkbenchChangeEvent(importVariablesPresenter.get()));
      }
    });

    getDisplay().addImportDataClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        eventBus.fireEvent(new WorkbenchChangeEvent(importDataPresenter.get()));
      }
    });

    getDisplay().addAddViewClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        eventBus.fireEvent(new ViewCreationRequiredEvent());
      }
    });

    super.registerHandler(eventBus.addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionChangeEvent.Handler() {

      @Override
      public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
        displayDetails(datasourcePresenter.getDisplay());
      }
    }));

    super.registerHandler(eventBus.addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {

      @Override
      public void onTableSelectionChanged(TableSelectionChangeEvent event) {
        displayDetails(tablePresenter.getDisplay());
      }
    }));

    super.registerHandler(eventBus.addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {

      @Override
      public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
        displayDetails(variablePresenter.getDisplay());
      }

    }));
  }

  private void displayDetails(WidgetDisplay detailsDisplay) {
    getDisplay().getDetailsPanel().clear();
    getDisplay().getDetailsPanel().add(detailsDisplay.asWidget());
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
    navigatorTreePresenter.unbind();
    datasourcePresenter.unbind();
    tablePresenter.unbind();
    variablePresenter.unbind();
    createDatasourcePresenter.unbind();
  }

  @Override
  public void refreshDisplay() {
    navigatorTreePresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    navigatorTreePresenter.revealDisplay();
  }

}
