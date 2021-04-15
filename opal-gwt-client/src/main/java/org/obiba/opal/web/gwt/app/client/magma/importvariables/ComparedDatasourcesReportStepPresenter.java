/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importvariables;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.ComparedDatasourcesReportStepPresenter.Display.ComparisonResult;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

public class ComparedDatasourcesReportStepPresenter
    extends PresenterWidget<ComparedDatasourcesReportStepPresenter.Display> {

  private String targetDatasourceName;

  private JsArray<TableCompareDto> authorizedComparedTables;

  private boolean conflictsExist;

  private boolean modificationsExist;

  @Inject
  public ComparedDatasourcesReportStepPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  public Request compare(String sourceDatasourceName,
                         @SuppressWarnings("ParameterHidesMemberVariable") String targetDatasourceName,
                         DatasourceCreatedCallback datasourceCreatedCallback, DatasourceFactoryDto factory,
                         DatasourceDto datasourceResource) {
    return compare(sourceDatasourceName, targetDatasourceName, datasourceCreatedCallback, factory, datasourceResource,
        false);
  }

  public Request compare(String sourceDatasourceName,
                         @SuppressWarnings("ParameterHidesMemberVariable") String targetDatasourceName,
                         DatasourceCreatedCallback datasourceCreatedCallback, DatasourceFactoryDto factory,
                         DatasourceDto datasourceResource, boolean merge) {
    this.targetDatasourceName = targetDatasourceName;
    getView().clearDisplay();
    authorizedComparedTables = JsArrays.create();
    String resourceUri = UriBuilder.create()
        .segment("datasource", sourceDatasourceName, "compare", targetDatasourceName)
        .query("merge", Boolean.toString(merge)).build();
    return ResourceRequestBuilderFactory.<DatasourceCompareDto>newBuilder() //
        .forResource(resourceUri) //
        .get() //
        .withCallback(new DatasourceCompareResourceCallback(datasourceCreatedCallback, factory, datasourceResource)) //
        .withCallback(SC_INTERNAL_SERVER_ERROR, new CompareErrorRequestCallback()) //
        .send();
  }

  public void allowIgnoreAllModifications(boolean allow) {
    getView().setIgnoreAllModificationsVisible(allow);
  }

  public boolean canBeSubmitted() {
    return !conflictsExist || getView().ignoreAllModifications() || !getView().isIgnoreAllModificationsVisible();
  }

  public List<String> getSelectedTables() {
    return getView().getSelectedTables();
  }

  public void addUpdateVariablesResourceRequests(ConclusionStepPresenter conclusionStepPresenter) {
    final List<String> selectedTableNames = getView().getSelectedTables();
    Iterable<TableCompareDto> filteredTables = Iterables
        .filter(JsArrays.toIterable(authorizedComparedTables), new Predicate<TableCompareDto>() {

          @Override
          public boolean apply(TableCompareDto input) {
            return selectedTableNames.contains(input.getCompared().getName());
          }
        });
    for (TableCompareDto tableCompareDto : filteredTables) {
      addUpdateVariablesResourceRequest(conclusionStepPresenter, tableCompareDto);
    }
  }

  @SuppressWarnings("unchecked")
  private void addUpdateVariablesResourceRequest(ConclusionStepPresenter conclusionStepPresenter,
                                                 TableCompareDto tableCompareDto) {
    JsArray<VariableDto> newVariables = JsArrays.toSafeArray(tableCompareDto.getNewVariablesArray());
    JsArray<VariableDto> modifiedVariables = JsArrays.toSafeArray(tableCompareDto.getModifiedVariablesArray());

    JsArray<VariableDto> variablesToStringify = (JsArray<VariableDto>) JsArray.createArray();
    JsArrays.pushAll(variablesToStringify, newVariables);
    if (!getView().ignoreAllModifications()) {
      JsArrays.pushAll(variablesToStringify, modifiedVariables);
    }

    if (variablesToStringify.length() > 0) {
      conclusionStepPresenter.setTargetDatasourceName(targetDatasourceName);
      conclusionStepPresenter.addResourceRequest(tableCompareDto.getCompared().getName(),
          "/datasource/" + targetDatasourceName + "/table/" + tableCompareDto.getCompared().getName(),
          createResourceRequestBuilder(tableCompareDto, variablesToStringify));
    }
  }

  ResourceRequestBuilder<? extends JavaScriptObject> createResourceRequestBuilder(TableCompareDto tableCompareDto,
                                                                                  JsArray<VariableDto> variables) {
    TableDto compared = tableCompareDto.getCompared();
    if (!tableCompareDto.hasWithTable()) {
      // new table
      TableDto newTableDto = TableDto.create();
      newTableDto.setName(compared.getName());
      newTableDto.setEntityType(compared.getEntityType());
      newTableDto.setVariablesArray(variables);
      UriBuilder ub = UriBuilders.DATASOURCE_TABLES.create();
      return ResourceRequestBuilderFactory.newBuilder().post().forResource(ub.build(targetDatasourceName))
          .withResourceBody(stringify(newTableDto));
    }

    UriBuilder uriBuilder = tableCompareDto.getWithTable().hasViewLink() //
        ? UriBuilders.DATASOURCE_VIEW_VARIABLES.create() //
        : UriBuilders.DATASOURCE_TABLE_VARIABLES.create();

    return ResourceRequestBuilderFactory.newBuilder() //
        .forResource(uriBuilder.build(targetDatasourceName, compared.getName())) //
        .withResourceBody(stringify(variables)) //
        .post();
  }

  public static native String stringify(JavaScriptObject obj)
  /*-{
    return $wnd.JSON.stringify(obj);
  }-*/;

  //
  // Inner classes
  //

  private final class CompareErrorRequestCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus()
          .fireEvent(NotificationEvent.newBuilder().error("DataImportFailed").args(response.getText()).build());
    }
  }

  private final class DatasourceCompareResourceCallback implements ResourceCallback<DatasourceCompareDto> {

    private final DatasourceCreatedCallback datasourceCreatedCallback;

    private final DatasourceFactoryDto factory;

    private final DatasourceDto datasourceResource;

    private DatasourceCompareResourceCallback(DatasourceCreatedCallback datasourceCreatedCallback,
                                              DatasourceFactoryDto factory, DatasourceDto datasourceResource) {
      this.datasourceCreatedCallback = datasourceCreatedCallback;
      this.factory = factory;
      this.datasourceResource = datasourceResource;
    }

    @Override
    public void onResource(Response response, DatasourceCompareDto resource) {
      Set<TableCompareDto> comparedTables = sortComparedTables(
          JsArrays.toSafeArray(resource.getTableComparisonsArray()));
      conflictsExist = false;
      for (TableCompareDto tableComparison : comparedTables) {
        ComparisonResult comparisonResult = getTableComparisonResult(tableComparison);
        addTableCompareTab(tableComparison, comparisonResult);
        if (comparisonResult == ComparisonResult.CONFLICT) {
          conflictsExist = true;
        } else if (tableComparison.getModifiedVariablesArray() != null) {
          modificationsExist = true;
        }
      }
      getView().setIgnoreAllModificationsEnabled(conflictsExist || modificationsExist);
      if (datasourceCreatedCallback != null) {
        datasourceCreatedCallback.onSuccess(factory, datasourceResource);
      }
    }

    private void addTableCompareTab(TableCompareDto tableCompareDto, ComparisonResult comparisonResult) {
      TableDto comparedTableDto = tableCompareDto.getCompared();
      if (tableCompareDto.hasWithTable()) {
        UriBuilder ub = UriBuilder.create()
            .segment("datasource", targetDatasourceName, "table", comparedTableDto.getName(), "variables");
        ResourceAuthorizationRequestBuilderFactory.newBuilder()//
            .forResource(ub.build()).post()//
            .authorize(new TableEditionAuthorizer(tableCompareDto, comparisonResult)).send();
      } else {
        UriBuilder ub = UriBuilder.create().segment("datasource", targetDatasourceName, "tables");
        ResourceAuthorizationRequestBuilderFactory.newBuilder()//
            .forResource(ub.build()).post()//
            .authorize(new TableEditionAuthorizer(tableCompareDto, comparisonResult)).send();
      }
    }

    private ComparisonResult getTableComparisonResult(TableCompareDto tableComparison) {
      if (JsArrays.toSafeArray(tableComparison.getConflictsArray()).length() > 0 //
          || JsArrays.toSafeArray(tableComparison.getModifiedVariablesArray()).length() +
          JsArrays.toSafeArray(tableComparison.getUnmodifiedVariablesArray()).length() +
          JsArrays.toSafeArray(tableComparison.getNewVariablesArray()).length() == 0) {
        return ComparisonResult.CONFLICT;
      }
      if (!tableComparison.hasWithTable()) {
        return ComparisonResult.CREATION;
      }
      if (JsArrays.toSafeArray(tableComparison.getModifiedVariablesArray()).length() > 0 ||
          JsArrays.toSafeArray(tableComparison.getNewVariablesArray()).length() > 0) {
        return ComparisonResult.MODIFICATION;
      }
      return ComparisonResult.SAME;
    }

    private Set<TableCompareDto> sortComparedTables(JsArray<TableCompareDto> comparedTables) {
      Set<TableCompareDto> tree = new TreeSet<TableCompareDto>(new Comparator<TableCompareDto>() {
        @Override
        public int compare(TableCompareDto table1, TableCompareDto table2) {
          return table1.getCompared().getName().compareTo(table2.getCompared().getName());
        }
      });
      for (int i = 0; i < comparedTables.length(); i++) {
        tree.add(comparedTables.get(i));
      }
      return tree;
    }
  }

  private final class TableEditionAuthorizer implements HasAuthorization {

    private final ComparisonResult comparisonResult;

    private final TableCompareDto tableCompareDto;

    private TableEditionAuthorizer(TableCompareDto tableCompareDto, ComparisonResult comparisonResult) {
      this.comparisonResult = comparisonResult;
      this.tableCompareDto = tableCompareDto;
    }

    @Override
    public void unauthorized() {
      getView().addTableComparison(tableCompareDto, ComparisonResult.FORBIDDEN);
    }

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      authorizedComparedTables.push(tableCompareDto);
      getView().addTableComparison(tableCompareDto, comparisonResult);
    }
  }

  //
  // Interfaces
  //
  public interface Display extends org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay {

    enum ComparisonResult {
      CREATION, MODIFICATION, CONFLICT, SAME, FORBIDDEN
    }

    void addTableComparison(TableCompareDto tableCompareData, ComparisonResult comparisonResult);

    void clearDisplay();

    void setIgnoreAllModificationsEnabled(boolean enabled);

    boolean isIgnoreAllModificationsVisible();

    boolean ignoreAllModifications();

    void setIgnoreAllModificationsVisible(boolean visible);

    List<String> getSelectedTables();

  }

}
