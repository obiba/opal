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

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter.Display.ComparisonResult;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceCompareDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class ComparedDatasourcesReportStepPresenter extends WidgetPresenter<ComparedDatasourcesReportStepPresenter.Display> {

  @Inject
  private UploadVariablesStepPresenter uploadVariablesStepPresenter;

  @Inject
  private ImportVariablesStepPresenter importVariablesStepPresenter;

  private String sourceDatasourceName;

  private String targetDatasourceName;

  private JsArray<TableCompareDto> comparedTables;

  private boolean conflictsExist;

  private boolean modificationsExist;

  @Inject
  public ComparedDatasourcesReportStepPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public interface Display extends WidgetDisplay {

    enum ComparisonResult {
      CREATION, MODIFICATION, CONFLICT
    }

    HandlerRegistration addSaveClickHandler(ClickHandler handler);

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addIgnoreAllModificationsHandler(ClickHandler ignoreAllModificationsClickHandler);

    void addTableCompareTab(TableCompareDto tableCompareData, ComparisonResult comparisonResult);

    void clearDisplay();

    void setEnabledSaveButton(boolean enabled);

    void setEnabledIgnoreAllModifications(boolean enabled);

    boolean ignoreAllModifications();

  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    initComparedDatasourceReport();
  }

  private void initComparedDatasourceReport() {
    getDisplay().clearDisplay();
    ResourceRequestBuilderFactory.<DatasourceCompareDto> newBuilder().forResource("/datasource/" + sourceDatasourceName + "/compare/" + targetDatasourceName).get().withCallback(new ResourceCallback<DatasourceCompareDto>() {

      @Override
      public void onResource(Response response, DatasourceCompareDto resource) {
        comparedTables = resource.getTableComparisonsArray();
        conflictsExist = false;
        for(int i = 0; i < comparedTables.length(); i++) {
          TableCompareDto tableComparison = comparedTables.get(i);
          ComparisonResult comparisonResult = getTableComparisonResult(tableComparison);
          getDisplay().addTableCompareTab(tableComparison, comparisonResult);
          if(comparisonResult == ComparisonResult.CONFLICT) {
            conflictsExist = true;
          } else if(comparisonResult == ComparisonResult.MODIFICATION) {
            modificationsExist = true;
          }
        }
        getDisplay().setEnabledSaveButton(!conflictsExist);
        getDisplay().setEnabledIgnoreAllModifications(conflictsExist || modificationsExist);
      }

    }).send();

  }

  private void addEventHandlers() {
    registerHandler(getDisplay().addSaveClickHandler(new SaveClickHandler()));
    registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    registerHandler(getDisplay().addIgnoreAllModificationsHandler(new IgnoreAllModificationsClickHandler()));
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

  private ComparisonResult getTableComparisonResult(TableCompareDto tableComparison) {
    if(tableComparison.getConflictsArray() != null && tableComparison.getConflictsArray().length() > 0) {
      return ComparisonResult.CONFLICT;
    } else if(!tableComparison.hasWithTable()) {
      return ComparisonResult.CREATION;
    } else {
      return ComparisonResult.MODIFICATION;
    }
  }

  public void setSourceDatasourceName(String sourceDatasourceName) {
    this.sourceDatasourceName = sourceDatasourceName;
  }

  public void setTargetDatasourceName(String targetDatasourceName) {
    this.targetDatasourceName = targetDatasourceName;
  }

  @SuppressWarnings("unchecked")
  private void addUpdateVariablesResourceRequests() {
    for(int tableIndex = 0; tableIndex < comparedTables.length(); tableIndex++) {
      TableCompareDto tableCompareDto = comparedTables.get(tableIndex);
      JsArray<VariableDto> newVariables = (JsArray<VariableDto>) (tableCompareDto.getNewVariablesArray() != null ? tableCompareDto.getNewVariablesArray() : JsArray.createArray());
      JsArray<VariableDto> existingVariables = (JsArray<VariableDto>) (tableCompareDto.getExistingVariablesArray() != null ? tableCompareDto.getExistingVariablesArray() : JsArray.createArray());

      JsArray<VariableDto> variablesToStringify = (JsArray<VariableDto>) JsArray.createArray();
      addVariables(newVariables, variablesToStringify);
      if(!getDisplay().ignoreAllModifications()) {
        addVariables(existingVariables, variablesToStringify);
      }

      if(variablesToStringify.length() > 0) {
        importVariablesStepPresenter.addResourceRequest(tableCompareDto.getCompared().getName(), "/datasource/" + targetDatasourceName + "/table/" + tableCompareDto.getCompared().getName(), createResourceRequestBuilder(tableCompareDto.getCompared(), !tableCompareDto.hasWithTable(), variablesToStringify));
      }
    }
  }

  private void addVariables(JsArray<VariableDto> variables, JsArray<VariableDto> variablesToStringify) {
    for(int variableIndex = 0; variableIndex < variables.length(); variableIndex++) {
      VariableDto variableDto = variables.get(variableIndex);
      variablesToStringify.push(variableDto);
    }
  }

  ResourceRequestBuilder<? extends JavaScriptObject> createResourceRequestBuilder(TableDto comparedTableDto, boolean newTable, JsArray<VariableDto> variables) {
    if(newTable) {
      TableDto newTableDto = TableDto.create();
      newTableDto.setName(comparedTableDto.getName());
      newTableDto.setEntityType(comparedTableDto.getEntityType());
      newTableDto.setVariablesArray(variables);

      return ResourceRequestBuilderFactory.newBuilder().post().forResource("/datasource/" + targetDatasourceName + "/tables").accept("application/x-protobuf+json").withResourceBody(stringify(newTableDto));
    } else {
      return ResourceRequestBuilderFactory.newBuilder().post().forResource("/datasource/" + targetDatasourceName + "/table/" + comparedTableDto.getName() + "/variables").accept("application/x-protobuf+json").withResourceBody(stringify(variables));
    }
  }

  class SaveClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      importVariablesStepPresenter.clearResourceRequests();
      addUpdateVariablesResourceRequests();
      if(importVariablesStepPresenter.getResourceRequestCount() != 0) {
        importVariablesStepPresenter.setReturnButtonEnabled(false);
        importVariablesStepPresenter.sendResourceRequests();
      }
      eventBus.fireEvent(new WorkbenchChangeEvent(importVariablesStepPresenter));
    }
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      eventBus.fireEvent(new WorkbenchChangeEvent(uploadVariablesStepPresenter));
    }
  }

  class IgnoreAllModificationsClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      getDisplay().setEnabledSaveButton(!conflictsExist || getDisplay().ignoreAllModifications());
    }
  }

  public static native String stringify(JavaScriptObject obj)
  /*-{
  return $wnd.JSON.stringify(obj);
  }-*/;

}
