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

import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourcesRefreshEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class NavigatorPresenter extends Presenter<NavigatorPresenter.Display, NavigatorPresenter.Proxy> {

  public interface Display extends View {

    HandlerRegistration addCreateDatasourceClickHandler(ClickHandler handler);

    HasWidgets getDetailsPanel();

    HandlerRegistration addExportDataClickHandler(ClickHandler handler);

    HandlerRegistration addImportDataClickHandler(ClickHandler handler);

    HasAuthorization getCreateDatasourceAuthorizer();

    HasAuthorization getImportDataAuthorizer();

    HasAuthorization getExportDataAuthorizer();

    HandlerRegistration refreshClickHandler(ClickHandler handler);

    HasText getSearch();
  }

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> LEFT_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> CENTER_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ProxyStandard
  public interface Proxy extends com.gwtplatform.mvp.client.proxy.Proxy<NavigatorPresenter> {}

  @Inject
  public NavigatorPresenter(final Display display, final Proxy proxy, final EventBus eventBus) {
    super(eventBus, display, proxy);
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, ApplicationPresenter.WORKBENCH, this);
  }

  @Override
  protected void onBind() {
    super.onBind();

    registerHandler(getView().addCreateDatasourceClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new WizardRequiredEvent(CreateDatasourcePresenter.WizardType));
      }

    }));

    registerHandler(getView().addImportDataClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new WizardRequiredEvent(DataImportPresenter.WizardType));
      }
    }));

    registerHandler(getView().addExportDataClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getEventBus().fireEvent(new WizardRequiredEvent(DataExportPresenter.WizardType));
      }
    }));

    registerHandler(getView().refreshClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getEventBus().fireEvent(new DatasourcesRefreshEvent());

      }
    }));

    // Update search box on event
    registerHandler(getEventBus()
        .addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionChangeEvent.Handler() {
          @Override
          public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
            getView().getSearch().setText("datasource:" + event.getSelection().getName());
          }
        }));

    registerHandler(
        getEventBus().addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {
          @Override
          public void onTableSelectionChanged(TableSelectionChangeEvent event) {
            getView().getSearch().setText( //
                "datasource:" + event.getSelection().getDatasourceName() +//
                    " table:" + event.getSelection().getName());
          }
        }));

    registerHandler(
        getEventBus().addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {
          @Override
          public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
            getView().getSearch().setText(//
                "datasource:" + event.getTable().getDatasourceName() +//
                    " table:" + event.getTable().getName() +
                    " variable:" + event.getSelection().getName());
          }
        }));
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    authorize();
  }

  private void authorize() {
    // create datasource
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasources").post()
        .authorize(getView().getCreateDatasourceAuthorizer()).send();
    // import data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/import").post()//
        .authorize(CascadingAuthorizer.newBuilder()//
            .and("/functional-units", HttpMethod.GET)//
            .authorize(getView().getImportDataAuthorizer()).build())//
        .send();
    // export data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/copy").post()//
        .authorize(CascadingAuthorizer.newBuilder()//
            .and("/functional-units", HttpMethod.GET)//
            .and("/functional-units/entities/table", HttpMethod.GET)//
            .authorize(getView().getExportDataAuthorizer()).build())//
        .send();
  }

}
