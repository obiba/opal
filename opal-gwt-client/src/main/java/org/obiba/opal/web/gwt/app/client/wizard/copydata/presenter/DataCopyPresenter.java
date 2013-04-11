/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.copydata.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.CopyCommandOptionsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class DataCopyPresenter extends WizardPresenterWidget<DataCopyPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  public static class Wizard extends WizardProxy<DataCopyPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<DataCopyPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }
  }

  private String datasourceName;

  private TableDto table;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public DataCopyPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    super.onBind();
    initDisplayComponents();

  }

  protected void initDisplayComponents() {
    registerHandler(getView().addJobLinkClickHandler(new JobLinkClickHandler()));
    getView().setTablesValidator(new TablesValidator());
    getView().setDestinationValidator(new DestinationValidator());
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    datasourceName = null;
    table = null;
  }

  @Override
  public void onReveal() {
    initDatasources();
  }

  @Override
  protected void onFinish() {
    super.onFinish();
    getView().renderPendingConclusion();
    UriBuilder uriBuilder = UriBuilder.create();
    uriBuilder.segment("datasource", datasourceName, "commands", "_copy");
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(uriBuilder.build()) //
        .post() //
        .withResourceBody(CopyCommandOptionsDto.stringify(createCopyCommandOptions())) //
        .withCallback(Response.SC_BAD_REQUEST, new ClientFailureResponseCodeCallBack()) //
        .withCallback(Response.SC_CREATED, new SuccessResponseCodeCallBack()).send();
  }

  private CopyCommandOptionsDto createCopyCommandOptions() {
    CopyCommandOptionsDto dto = CopyCommandOptionsDto.create();

    JsArrayString selectedTables = JavaScriptObject.createArray().cast();
    if(table == null) {
      for(TableDto tableDto : getView().getSelectedTables()) {
        selectedTables.push(tableDto.getDatasourceName() + "." + tableDto.getName());
      }
    } else {
      selectedTables.push(table.getDatasourceName() + "." + table.getName());
    }

    dto.setTablesArray(selectedTables);
    dto.setDestination(getView().getSelectedDatasource());
    dto.setNonIncremental(!getView().isIncremental());
    dto.setCopyNullValues(getView().isCopyNullValues());
    dto.setNoVariables(!getView().isWithVariables());
    if(getView().isUseAlias()) {
      dto.setTransform("attribute('alias').isNull().value ? name() : attribute('alias')");
    }
    return dto;
  }

  private void initDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            List<DatasourceDto> datasources = null;
            if(resource != null && resource.length() > 0) {
              datasources = filterDatasources(resource);

            }
            if(datasources != null && datasources.size() > 0) {
              getView().setDatasources(datasources);
            } else {
              getEventBus().fireEvent(NotificationEvent.newBuilder().error("NoDataToCopy").build());
            }
          }

          private List<DatasourceDto> filterDatasources(JsArray<DatasourceDto> datasources) {

            List<DatasourceDto> filteredDatasources = new ArrayList<DatasourceDto>();
            Set<String> originDatasourceName = getOriginDatasourceNames();
            for(DatasourceDto datasource : JsArrays.toList(datasources)) {
              if(!originDatasourceName.contains(datasource.getName())) {
                filteredDatasources.add(datasource);
              }
            }
            return filteredDatasources;
          }

          private Set<String> getOriginDatasourceNames() {
            Set<String> originDatasourceNames = new HashSet<String>();
            for(TableDto tableDto : getView().getSelectedTables()) {
              originDatasourceNames.add(tableDto.getDatasourceName());
            }
            // can't copy in itself
            if(datasourceName != null) {
              originDatasourceNames.add(datasourceName);
            } else if(table != null) {
              originDatasourceNames.add(table.getDatasourceName());
            }
            return originDatasourceNames;
          }

        }).send();
  }

  //
  // Wizard Methods
  //

  @SuppressWarnings("ChainOfInstanceofChecks")
  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 0) {
      if(event.getEventParameters()[0] instanceof String) {
        datasourceName = (String) event.getEventParameters()[0];
      } else if(event.getEventParameters()[0] instanceof TableDto) {
        table = (TableDto) event.getEventParameters()[0];
        datasourceName = table.getDatasourceName();
      } else {
        throw new IllegalArgumentException("unexpected event parameter type (expected String)");
      }
      ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
          .forResource("/datasource/" + datasourceName + "/tables").get()
          .withCallback(new ResourceCallback<JsArray<TableDto>>() {
            @Override
            public void onResource(Response response, JsArray<TableDto> resource) {
              getView().addTableSelections(JsArrays.toSafeArray(resource));
              if(table == null) {
                getView().selectAllTables();
              } else {
                getView().selectTable(table);
              }
            }

          }).send();
    }
  }

  //
  // Interfaces and classes
  //

  private final class DestinationValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      List<String> errors = formValidationErrors();
      if(errors.size() > 0) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(errors).build());
        return false;
      }
      return true;
    }

    private List<String> formValidationErrors() {
      return new ArrayList<String>();
    }
  }

  private final class TablesValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(getView().getSelectedTables().isEmpty()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("ExportDataMissingTables").build());
        return false;
      }
      return true;
    }
  }

  class ClientFailureResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      NotificationEvent.Builder builder = NotificationEvent.newBuilder();
      try {
        ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
        builder.error(errorDto.getStatus()).args(errorDto.getArgumentsArray());
      } catch(Exception e) {
        builder.error(response.getText());
      }
      getEventBus().fireEvent(builder.build());
      getView().renderFailedConclusion();
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      getView().renderCompletedConclusion(jobId);
    }
  }

  class JobLinkClickHandler implements ClickHandler {

    JobLinkClickHandler() {
    }

    @Override
    public void onClick(ClickEvent arg0) {
      getEventBus().fireEvent(new PlaceChangeEvent(Places.jobsPlace));
    }
  }

  public interface Display extends WizardView {

    void setTablesValidator(ValidationHandler validationHandler);

    void setDestinationValidator(ValidationHandler handler);

    /**
     * Set a collection of datasources retrieved from Opal.
     */
    void setDatasources(List<DatasourceDto> datasources);

    /**
     * Get the datasource selected by the user.
     */
    String getSelectedDatasource();

    /**
     * Display the conclusion step
     */
    void renderCompletedConclusion(String jobId);

    void renderFailedConclusion();

    void renderPendingConclusion();

    /**
     * Add a handler to the job list
     */
    HandlerRegistration addJobLinkClickHandler(ClickHandler handler);

    boolean isIncremental();

    boolean isWithVariables();

    boolean isUseAlias();

    void addTableSelections(JsArray<TableDto> tables);

    void selectTable(TableDto tableDto);

    void selectAllTables();

    List<TableDto> getSelectedTables();

    boolean isCopyNullValues();
  }

}
