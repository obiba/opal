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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter.Display.ComparisonResult;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceCompareDto;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ComparedDatasourcesReportStepPresenter extends WidgetPresenter<ComparedDatasourcesReportStepPresenter.Display> {

  private String targetDatasourceName;

  private JsArray<TableCompareDto> authorizedComparedTables;

  private boolean conflictsExist;

  private boolean modificationsExist;

  @Inject
  public ComparedDatasourcesReportStepPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
  }

  public void compare(String sourceDatasourceName, String targetDatasourceName, final VariablesImportPresenter.Display display, final DatasourceFactoryDto factory, final DatasourceDto datasourceResource) {
    this.targetDatasourceName = targetDatasourceName;
    getDisplay().clearDisplay();
    authorizedComparedTables = JsArrays.create();

    ResourceRequestBuilderFactory.<DatasourceCompareDto> newBuilder().forResource("/datasource/" + sourceDatasourceName + "/compare/" + targetDatasourceName).get().withCallback(new ResourceCallback<DatasourceCompareDto>() {

      @Override
      public void onResource(Response response, DatasourceCompareDto resource) {
        Set<TableCompareDto> comparedTables = sortComparedTables(JsArrays.toSafeArray(resource.getTableComparisonsArray()));
        conflictsExist = false;
        for(TableCompareDto tableComparison : comparedTables) {
          ComparisonResult comparisonResult = getTableComparisonResult(tableComparison);
          addTableCompareTab(tableComparison, comparisonResult);
          if(comparisonResult == ComparisonResult.CONFLICT) {
            conflictsExist = true;
          } else if(tableComparison.getExistingVariablesArray() != null) {
            modificationsExist = true;
          }
        }
        getDisplay().setEnabledIgnoreAllModifications(conflictsExist || modificationsExist);
        display.getDatasourceCreatedCallback().onSuccess(factory, datasourceResource);
      }

      private TreeSet<TableCompareDto> sortComparedTables(JsArray<TableCompareDto> comparedTables) {
        TreeSet<TableCompareDto> tree = new TreeSet<TableCompareDto>(new Comparator<TableCompareDto>() {
          @Override
          public int compare(TableCompareDto table1, TableCompareDto table2) {
            return table1.getCompared().getName().compareTo(table2.getCompared().getName());
          }
        });
        for(int i = 0; i < comparedTables.length(); i++) {
          tree.add(comparedTables.get(i));
        }
        return tree;
      }

    }).send();

  }

  public boolean canBeSubmitted() {
    return !conflictsExist || getDisplay().ignoreAllModifications();
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

  @SuppressWarnings("unchecked")
  public void addUpdateVariablesResourceRequests(ConclusionStepPresenter conclusionStepPresenter) {
    for(TableCompareDto tableCompareDto : JsArrays.toIterable(authorizedComparedTables)) {
      JsArray<VariableDto> newVariables = JsArrays.toSafeArray(tableCompareDto.getNewVariablesArray());
      JsArray<VariableDto> existingVariables = JsArrays.toSafeArray(tableCompareDto.getExistingVariablesArray());

      JsArray<VariableDto> variablesToStringify = (JsArray<VariableDto>) JsArray.createArray();
      JsArrays.pushAll(variablesToStringify, newVariables);
      if(!getDisplay().ignoreAllModifications()) {
        JsArrays.pushAll(variablesToStringify, existingVariables);
      }

      if(variablesToStringify.length() > 0) {
        conclusionStepPresenter.setTargetDatasourceName(targetDatasourceName);
        conclusionStepPresenter.addResourceRequest(tableCompareDto.getCompared().getName(), "/datasource/" + targetDatasourceName + "/table/" + tableCompareDto.getCompared().getName(), createResourceRequestBuilder(tableCompareDto.getCompared(), !tableCompareDto.hasWithTable(), variablesToStringify));
      }
    }
  }

  ResourceRequestBuilder<? extends JavaScriptObject> createResourceRequestBuilder(TableDto comparedTableDto, boolean newTable, JsArray<VariableDto> variables) {
    if(newTable) {
      TableDto newTableDto = TableDto.create();
      newTableDto.setName(comparedTableDto.getName());
      newTableDto.setEntityType(comparedTableDto.getEntityType());
      newTableDto.setVariablesArray(variables);

      return ResourceRequestBuilderFactory.newBuilder().post().forResource("/datasource/" + targetDatasourceName + "/tables").withResourceBody(stringify(newTableDto));
    } else {
      return ResourceRequestBuilderFactory.newBuilder().post().forResource("/datasource/" + targetDatasourceName + "/table/" + comparedTableDto.getName() + "/variables").withResourceBody(stringify(variables));
    }
  }

  private void addTableCompareTab(TableCompareDto tableCompareDto, ComparisonResult comparisonResult) {
    TableDto comparedTableDto = tableCompareDto.getCompared();
    if(!tableCompareDto.hasWithTable()) {
      ResourceAuthorizationRequestBuilderFactory.newBuilder()//
      .forResource("/datasource/" + targetDatasourceName + "/tables").post()//
      .authorize(new TableEditionAuthorizer(tableCompareDto, comparisonResult)).send();
    } else {
      ResourceAuthorizationRequestBuilderFactory.newBuilder()//
      .forResource("/datasource/" + targetDatasourceName + "/table/" + comparedTableDto.getName() + "/variables").post()//
      .authorize(new TableEditionAuthorizer(tableCompareDto, comparisonResult)).send();
    }
  }

  public static native String stringify(JavaScriptObject obj)
  /*-{
  return $wnd.JSON.stringify(obj);
  }-*/;

  //
  // Inner classes
  //

  private final class TableEditionAuthorizer implements HasAuthorization {

    private final ComparisonResult comparisonResult;

    private final TableCompareDto tableCompareDto;

    public TableEditionAuthorizer(TableCompareDto tableCompareDto, ComparisonResult comparisonResult) {
      super();
      this.comparisonResult = comparisonResult;
      this.tableCompareDto = tableCompareDto;
    }

    @Override
    public void unauthorized() {
      getDisplay().addForbiddenTableCompareTab(tableCompareDto, comparisonResult);
    }

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      authorizedComparedTables.push(tableCompareDto);
      getDisplay().addTableCompareTab(tableCompareDto, comparisonResult);
    }
  }

  //
  // Interfaces
  //
  public interface Display extends WidgetDisplay {

    enum ComparisonResult {
      CREATION, MODIFICATION, CONFLICT
    }

    void addTableCompareTab(TableCompareDto tableCompareData, ComparisonResult comparisonResult);

    void addForbiddenTableCompareTab(TableCompareDto tableCompareData, ComparisonResult comparisonResult);

    void clearDisplay();

    void setEnabledIgnoreAllModifications(boolean enabled);

    boolean ignoreAllModifications();

    Widget getStepHelp();

  }

}
