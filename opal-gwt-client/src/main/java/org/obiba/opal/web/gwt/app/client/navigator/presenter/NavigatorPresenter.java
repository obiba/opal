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

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class NavigatorPresenter extends Presenter<NavigatorPresenter.Display, NavigatorPresenter.Proxy> {

  public interface Display extends View {

    void setTreeDisplay(NavigatorTreePresenter.Display treeDisplay);

    HandlerRegistration addCreateDatasourceClickHandler(ClickHandler handler);

    HasWidgets getDetailsPanel();

    HandlerRegistration addExportDataClickHandler(ClickHandler handler);

    HandlerRegistration addImportDataClickHandler(ClickHandler handler);

    HasAuthorization getCreateDatasourceAuthorizer();

    HasAuthorization getImportDataAuthorizer();

    HasAuthorization getExportDataAuthorizer();
  }

  @ProxyStandard
  @NameToken(Places.navigator)
  public interface Proxy extends ProxyPlace<NavigatorPresenter> {
  }

  private NavigatorTreePresenter navigatorTreePresenter;

  private DatasourcePresenter datasourcePresenter;

  private TablePresenter tablePresenter;

  private VariablePresenter variablePresenter;

  @Inject
  public NavigatorPresenter(final Display display, final Proxy proxy, final EventBus eventBus, NavigatorTreePresenter navigatorTreePresenter, DatasourcePresenter datasourcePresenter, TablePresenter tablePresenter, VariablePresenter variablePresenter) {
    super(eventBus, display, proxy);

    this.navigatorTreePresenter = navigatorTreePresenter;
    this.datasourcePresenter = datasourcePresenter;
    this.tablePresenter = tablePresenter;
    this.variablePresenter = variablePresenter;
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, ApplicationPresenter.WORKBENCH, this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    navigatorTreePresenter.bind();
    datasourcePresenter.bind();
    tablePresenter.bind();
    variablePresenter.bind();

    getView().setTreeDisplay(navigatorTreePresenter.getDisplay());

    super.registerHandler(getView().addCreateDatasourceClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new WizardRequiredEvent(WizardType.CREATE_DATASOURCE));
      }
    }));

    super.registerHandler(getView().addImportDataClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new WizardRequiredEvent(WizardType.IMPORT_DATA));
      }
    }));

    super.registerHandler(getView().addExportDataClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new WizardRequiredEvent(WizardType.EXPORT_DATA));
      }
    }));

    super.registerHandler(getEventBus().addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionChangeEvent.Handler() {

      @Override
      public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
        displayDetails(datasourcePresenter.getDisplay());
      }
    }));

    super.registerHandler(getEventBus().addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {

      @Override
      public void onTableSelectionChanged(TableSelectionChangeEvent event) {
        displayDetails(tablePresenter.getDisplay());
      }
    }));

    super.registerHandler(getEventBus().addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {

      @Override
      public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
        displayDetails(variablePresenter.getDisplay());
      }

    }));
  }

  @Override
  protected void onUnbind() {
    navigatorTreePresenter.unbind();
    datasourcePresenter.unbind();
    tablePresenter.unbind();
    variablePresenter.unbind();
  }

  @Override
  protected void onReset() {
    super.onReset();
    navigatorTreePresenter.refreshDisplay();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    navigatorTreePresenter.revealDisplay();
    authorize();
  }

  private void authorize() {
    // create datasource
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasources").post().authorize(getView().getCreateDatasourceAuthorizer()).send();
    // import data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/import").post()//
    .authorize(CascadingAuthorizer.newBuilder().and("/files/meta", HttpMethod.GET)//
    .and("/functional-units", HttpMethod.GET)//
    .authorize(getView().getImportDataAuthorizer()).build())//
    .send();
    // export data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/copy").post()//
    .authorize(CascadingAuthorizer.newBuilder().and("/files/meta", HttpMethod.GET)//
    .and("/functional-units", HttpMethod.GET)//
    .and("/functional-units/entities/table", HttpMethod.GET)//
    .authorize(getView().getExportDataAuthorizer()).build())//
    .send();
  }

  private void displayDetails(WidgetDisplay detailsDisplay) {
    getView().getDetailsPanel().clear();
    getView().getDetailsPanel().add(detailsDisplay.asWidget());
  }

}
