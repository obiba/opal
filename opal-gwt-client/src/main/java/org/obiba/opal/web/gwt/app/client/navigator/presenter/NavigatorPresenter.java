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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.project.event.DatasourcesRefreshEvent;
import org.obiba.opal.web.gwt.app.client.project.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.project.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.presenter.PageContainerPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.VariableSearchListItem;
import org.obiba.opal.web.gwt.app.client.workbench.view.VariableSuggestOracle;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class NavigatorPresenter extends Presenter<NavigatorPresenter.Display, NavigatorPresenter.Proxy>
    implements HasPageTitle {

  private static final Translations translations = GWT.create(Translations.class);

  public interface Display extends View {

    HandlerRegistration addCreateDatasourceClickHandler(ClickHandler handler);

    HandlerRegistration addExportDataClickHandler(ClickHandler handler);

    HandlerRegistration addImportDataClickHandler(ClickHandler handler);

    HasAuthorization getCreateDatasourceAuthorizer();

    HasAuthorization getImportDataAuthorizer();

    HasAuthorization getExportDataAuthorizer();

    HandlerRegistration refreshClickHandler(ClickHandler handler);

    void addSearchItem(String text, VariableSearchListItem.ItemType type);

    void clearSearch();

    HandlerRegistration addSearchSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler);
  }

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> LEFT_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ContentSlot
  public static final GwtEvent.Type<RevealContentHandler<?>> CENTER_PANE = new GwtEvent.Type<RevealContentHandler<?>>();

  @ProxyStandard
  public interface Proxy extends com.gwtplatform.mvp.client.proxy.Proxy<NavigatorPresenter> {}

  @Inject
  public NavigatorPresenter(Display display, Proxy proxy, EventBus eventBus) {
    super(eventBus, display, proxy);
  }

  @Override
  public String getTitle() {
    return translations.pageDatasourcesTitle();
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, PageContainerPresenter.CONTENT, this);
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
            getView().clearSearch();
            getView().addSearchItem(event.getSelection(), VariableSearchListItem.ItemType.DATASOURCE);
          }
        }));

    registerHandler(
        getEventBus().addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {
          @Override
          public void onTableSelectionChanged(TableSelectionChangeEvent event) {
            getView().clearSearch();
            getView()
                .addSearchItem(event.getDatasourceName(), VariableSearchListItem.ItemType.DATASOURCE);
            getView().addSearchItem(event.getTableName(), VariableSearchListItem.ItemType.TABLE);
          }
        }));

    registerHandler(
        getEventBus().addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {
          @Override
          public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
            getView().clearSearch();
            getView().addSearchItem(event.getTable().getDatasourceName(), VariableSearchListItem.ItemType.DATASOURCE);
            getView().addSearchItem(event.getTable().getName(), VariableSearchListItem.ItemType.TABLE);
          }
        }));

    registerHandler(getView().addSearchSelectionHandler(new VariableSuggestionSelectionHandler()));
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

  private class VariableSuggestionSelectionHandler implements SelectionHandler<SuggestOracle.Suggestion> {

    @Override
    public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
      // Get the table dto to fire the event to select the variable
      final String datasourceName = ((VariableSuggestOracle.VariableSuggestion) event.getSelectedItem())
          .getDatasource();
      final String tableName = ((VariableSuggestOracle.VariableSuggestion) event.getSelectedItem()).getTable();
      final String variableName = ((VariableSuggestOracle.VariableSuggestion) event.getSelectedItem()).getVariable();

      UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName, "table", tableName);
      ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<TableDto>() {
            @Override
            public void onResource(Response response, final TableDto tableDto) {

              UriBuilder ub = UriBuilder.create()
                  .segment("datasource", datasourceName, "table", tableName, "variables");
              ResourceRequestBuilderFactory.<JsArray<VariableDto>>newBuilder().forResource(ub.build()).get()
                  .withCallback(new ResourceCallback<JsArray<VariableDto>>() {

                    @Override
                    public void onResource(Response response, JsArray<VariableDto> resource) {
                      JsArray<VariableDto> variables = JsArrays.toSafeArray(resource);

                      VariableDto previous = null;
                      VariableDto selection = null;
                      VariableDto next = null;
                      for(int i = 0; i < variables.length(); i++) {
                        if(variables.get(i).getName().equals(variableName)) {
                          selection = variables.get(i);

                          if(i >= 0) {
                            previous = variables.get(i - 1);
                          }

                          if(i < variables.length() - 1) {
                            next = variables.get(i + 1);
                          }
                        }
                      }
                      getEventBus().fireEvent(new VariableSelectionChangeEvent(tableDto, selection, previous, next));
                    }
                  })//
                  .withCallback(Response.SC_SERVICE_UNAVAILABLE, new ResponseCodeCallback() {
                    @Override
                    public void onResponseCode(Request request, Response response) {
                      getEventBus().fireEvent(NotificationEvent.newBuilder().error("SearchServiceUnavailable").build());
                    }
                  }).send();
            }
          }).send();

    }
  }

}
