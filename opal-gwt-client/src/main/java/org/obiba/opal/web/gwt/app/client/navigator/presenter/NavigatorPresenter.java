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

import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.VariablesImportPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class NavigatorPresenter extends WidgetPresenter<NavigatorPresenter.Display> {

  public interface Display extends WidgetDisplay {

    void setTreeDisplay(NavigatorTreePresenter.Display treeDisplay);

    HandlerRegistration addCreateDatasourceClickHandler(ClickHandler handler);

    HasWidgets getDetailsPanel();

    HandlerRegistration addExportDataClickHandler(ClickHandler handler);

    HandlerRegistration addImportDataClickHandler(ClickHandler handler);

    HandlerRegistration addImportVariablesClickHandler(ClickHandler handler);

    HandlerRegistration addAddViewClickHandler(ClickHandler handler);
  }

  @Inject
  private VariablesImportPresenter variablesImportPresenter;

  @Inject
  private DataImportPresenter dataImportPresenter;

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
    dataImportPresenter.bind();
    variablesImportPresenter.bind();
    createDatasourcePresenter.bind();

    getDisplay().setTreeDisplay(navigatorTreePresenter.getDisplay());

    super.registerHandler(getDisplay().addCreateDatasourceClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        createDatasourcePresenter.revealDisplay();
      }
    }));

    super.registerHandler(getDisplay().addExportDataClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        dataExportPresenter.get().revealDisplay();
      }
    }));

    super.registerHandler(getDisplay().addImportVariablesClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        variablesImportPresenter.revealDisplay();
      }
    }));

    super.registerHandler(getDisplay().addImportDataClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        dataImportPresenter.revealDisplay();
      }
    }));

    super.registerHandler(getDisplay().addAddViewClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new WizardRequiredEvent(WizardType.CREATE_VIEW));
      }
    }));

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
    dataImportPresenter.unbind();
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
