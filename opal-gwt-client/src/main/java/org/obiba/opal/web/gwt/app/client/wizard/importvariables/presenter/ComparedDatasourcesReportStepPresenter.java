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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter.Display.ComparisonResult;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceCompareDto;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

public class ComparedDatasourcesReportStepPresenter extends
    WidgetPresenter<ComparedDatasourcesReportStepPresenter.Display> {

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

  public Request compare(String sourceDatasourceName,
      @SuppressWarnings("ParameterHidesMemberVariable") String targetDatasourceName,
      DatasourceCreatedCallback datasourceCreatedCallback, DatasourceFactoryDto factory,
      DatasourceDto datasourceResource) {
    this.targetDatasourceName = targetDatasourceName;
    getDisplay().clearDisplay();
    authorizedComparedTables = JsArrays.create();
    String resourceUri = UriBuilder.create()
        .segment("datasource", sourceDatasourceName, "compare", targetDatasourceName).build();
    return ResourceRequestBuilderFactory.<DatasourceCompareDto>newBuilder() //
        .forResource(resourceUri) //
        .get() //
        .withCallback(new DatasourceCompareResourceCallback(datasourceCreatedCallback, factory, datasourceResource)) //
        .withCallback(SC_INTERNAL_SERVER_ERROR, new CompareErrorRequestCallback()) //
        .send();
  }

  public void allowIgnoreAllModifications(boolean allow) {
    getDisplay().setIgnoreAllModificationsVisible(allow);
  }

  public boolean canBeSubmitted() {
    return !conflictsExist || getDisplay().ignoreAllModifications() || !getDisplay().isIgnoreAllModificationsVisible();
  }

  public List<String> getSelectedTables() {
    return getDisplay().getSelectedTables();
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

  public void addUpdateVariablesResourceRequests(ConclusionStepPresenter conclusionStepPresenter) {
    final List<String> selectedTableNames = getDisplay().getSelectedTables();
    Iterable<TableCompareDto> filteredTables = Iterables
        .filter(JsArrays.toIterable(authorizedComparedTables), new Predicate<TableCompareDto>() {

          @Override
          public boolean apply(TableCompareDto input) {
            return selectedTableNames.contains(input.getCompared().getName());
          }
        });
    for(TableCompareDto tableCompareDto : filteredTables) {
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
    if(!getDisplay().ignoreAllModifications()) {
      JsArrays.pushAll(variablesToStringify, modifiedVariables);
    }

    if(variablesToStringify.length() > 0) {
      conclusionStepPresenter.setTargetDatasourceName(targetDatasourceName);
      conclusionStepPresenter.addResourceRequest(tableCompareDto.getCompared().getName(),
          "/datasource/" + targetDatasourceName + "/table/" + tableCompareDto.getCompared().getName(),
          createResourceRequestBuilder(tableCompareDto, variablesToStringify));
    }
  }

  ResourceRequestBuilder<? extends JavaScriptObject> createResourceRequestBuilder(TableCompareDto tableCompareDto,
      JsArray<VariableDto> variables) {
    TableDto compared = tableCompareDto.getCompared();
    if(!tableCompareDto.hasWithTable()) {
      // new table
      TableDto newTableDto = TableDto.create();
      newTableDto.setName(compared.getName());
      newTableDto.setEntityType(compared.getEntityType());
      newTableDto.setVariablesArray(variables);
      UriBuilder ub = UriBuilder.create().segment("datasource", targetDatasourceName, "tables");
      return ResourceRequestBuilderFactory.newBuilder().post().forResource(ub.build())
          .withResourceBody(stringify(newTableDto));
    }
    if(tableCompareDto.getWithTable().hasViewLink()) {
      UriBuilder ub = UriBuilder.create()
          .segment("datasource", targetDatasourceName, "view", compared.getName(), "variables");
      return ResourceRequestBuilderFactory.newBuilder().post().forResource(ub.build())
          .withResourceBody(stringify(variables));
    }
    UriBuilder ub = UriBuilder.create()
        .segment("datasource", targetDatasourceName, "table", compared.getName(), "variables");
    return ResourceRequestBuilderFactory.newBuilder().post().forResource(ub.build())
        .withResourceBody(stringify(variables));
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
      eventBus.fireEvent(NotificationEvent.newBuilder().error("DataImportFailed").args(response.getText()).build());
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
      for(TableCompareDto tableComparison : comparedTables) {
        ComparisonResult comparisonResult = getTableComparisonResult(tableComparison);
        addTableCompareTab(tableComparison, comparisonResult);
        if(comparisonResult == ComparisonResult.CONFLICT) {
          conflictsExist = true;
        } else if(tableComparison.getModifiedVariablesArray() != null) {
          modificationsExist = true;
        }
      }
      getDisplay().setIgnoreAllModificationsEnabled(conflictsExist || modificationsExist);
      if(datasourceCreatedCallback != null) {
        datasourceCreatedCallback.onSuccess(factory, datasourceResource);
      }
    }

    private void addTableCompareTab(TableCompareDto tableCompareDto, ComparisonResult comparisonResult) {
      TableDto comparedTableDto = tableCompareDto.getCompared();
      if(tableCompareDto.hasWithTable()) {
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
      if(JsArrays.toSafeArray(tableComparison.getConflictsArray()).length() > 0 //
          || JsArrays.toSafeArray(tableComparison.getModifiedVariablesArray()).length() +
          JsArrays.toSafeArray(tableComparison.getUnmodifiedVariablesArray()).length() +
          JsArrays.toSafeArray(tableComparison.getNewVariablesArray()).length() == 0) {
        return ComparisonResult.CONFLICT;
      }
      if(!tableComparison.hasWithTable()) {
        return ComparisonResult.CREATION;
      }
      if(JsArrays.toSafeArray(tableComparison.getModifiedVariablesArray()).length() > 0 || JsArrays
          .toSafeArray(tableComparison.getNewVariablesArray()).length() > 0) {
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
      for(int i = 0; i < comparedTables.length(); i++) {
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
      getDisplay().addTableComparison(tableCompareDto, ComparisonResult.FORBIDDEN);
    }

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      authorizedComparedTables.push(tableCompareDto);
      getDisplay().addTableComparison(tableCompareDto, comparisonResult);
    }
  }

  //
  // Interfaces
  //
  public interface Display extends WidgetDisplay, WizardStepDisplay {

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
