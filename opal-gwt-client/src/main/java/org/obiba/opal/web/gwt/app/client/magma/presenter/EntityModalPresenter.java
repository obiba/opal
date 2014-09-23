/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.VariablesFilter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.JavaScriptErrorDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

/**
 *
 */
public class EntityModalPresenter extends ModalPresenterWidget<EntityModalPresenter.Display>
    implements EntityModalUiHandlers {

  private String entityType;

  private String entityId;

  private TableDto selectedTable;

  private Map<String, VariableDto> variablesMap = new HashMap<String, VariableDto>();

  private final ModalProvider<ValueSequencePopupPresenter> valueSequencePopupProvider;

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  @Inject
  public EntityModalPresenter(EventBus eventBus, Display display,
      ModalProvider<ValueSequencePopupPresenter> valueSequencePopupProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.valueSequencePopupProvider = valueSequencePopupProvider.setContainer(this);
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public void initialize(TableDto table, String entityType, String entityId, String filterText) {
    if (table == null) selectedTable = null;
    else if(table.getEntityType().equals(entityType)) selectedTable = table;
    this.entityType = entityType;
    this.entityId = entityId;
    getView().setFilterText(table != null && table.getEntityType().equals(entityType) ? filterText : "");
    getView().setEntityType(entityType);
    getView().setEntityId(entityId);
    getView().setValueViewHandler(new ValueSequenceHandlerImpl());

    loadTables();
  }

  @Override
  public void selectTable(TableDto table) {
    if(table != null) {
      // Fetch table info
      UriBuilder uriBuilder = UriBuilder.create()
          .segment("datasource", table.getDatasourceName(), "table", table.getName());
      ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(uriBuilder.build()).get()
          .withCallback(new ResourceCallback<TableDto>() {
            @Override
            public void onResource(Response response, TableDto resource) {
              selectedTable = resource;
              loadVariablesInternal(selectedTable, getView().getFilterText());
            }
          }).send();
    }
  }

  @Override
  public void loadVariables() {
    loadVariablesInternal(selectedTable, "");
  }

  @Override
  public void filterVariables(String filter) {
    loadVariablesInternal(selectedTable, filter);
  }

  /**
   * find all table where this entity appears
   */
  private void loadTables() {
    UriBuilder uriBuilder = UriBuilder.create().segment("entity", entityId, "type", entityType, "tables");
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR,
            new ResponseErrorCallback(getEventBus(), "InternalError")).withCallback(Response.SC_NOT_FOUND,
        new ResponseErrorCallback(getEventBus(), "NoTablesForEntityIdType", entityId, entityType))//
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            JsArray<TableDto> tables = JsArrays.toSafeArray(resource);
            if(tables.length() == 0) {
              getView().setTables(tables, null);
              getEventBus()
                  .fireEvent(NotificationEvent.newBuilder().warn("NoSuchEntity").args(entityId, entityType).build());
            } else {
              if(selectedTable == null) selectedTable = tables.get(0);
              getView().setTables(tables, selectedTable);
              loadVariablesInternal(selectedTable, getView().getFilterText());
            }
          }
        }).send();
  }

  private void loadVariablesInternal(final TableDto table, String select) {
    new VariablesFilter() {
      @Override
      public void beforeVariableResourceCallback() {
        // nothing
      }

      @Override
      public void onVariableResourceCallback() {
        buildVariableMap(results, table);
      }

      private void buildVariableMap(List<VariableDto> variables, TableDto table) {
        variablesMap = new HashMap<String, VariableDto>();

        for(VariableDto v : variables) {
          //VariableDto variable = variables.get(i);
          variablesMap.put(v.getName(), v);
        }
        loadValueSets(table);
      }

      private void loadValueSets(TableDto table) {
        UriBuilder uriBuilder = UriBuilder.create()
            .segment("datasource", table.getDatasourceName(), "table", table.getName(), "valueSet", entityId);

        ResourceRequestBuilderFactory.<ValueSetsDto>newBuilder().forResource(uriBuilder.build()).get()
            .withCallback(Response.SC_INTERNAL_SERVER_ERROR, new ResponseErrorCallback(getEventBus(), "InternalError"))
            .withCallback(Response.SC_NOT_FOUND,
                new ResponseErrorCallback(getEventBus(), "NoVariableValuesFound")).withCallback(Response.SC_BAD_REQUEST,
            new BadRequestCallback())//
            .withCallback(new ResourceCallback<ValueSetsDto>() {
              @Override
              public void onResource(Response response, ValueSetsDto resource) {
                populateRows(resource);
              }

              private void populateRows(ValueSetsDto valueSets) {
                JsArrayString variables = valueSets.getVariablesArray();
                JsArray<ValueSetsDto.ValueSetDto> valueSetList = valueSets.getValueSetsArray();
                JsArray<ValueSetsDto.ValueDto> values = valueSetList.get(0).getValuesArray();

                List<VariableValueRow> rows = new ArrayList<VariableValueRow>();
                int variableCount = variables.length();

                for(int i = 0; i < variableCount; i++) {
                  String variableName = variables.get(i);
                  if(variablesMap.containsKey(variableName)) {
                    rows.add(new VariableValueRow(variableName, values.get(i), variablesMap.get(variableName)));
                  }
                }
                getView().renderRows(rows);
              }
            }).send();
      }
    }//
        .withQuery(select)//
        .withVariable(true)//
        .withLimit(table.getVariableCount())//
        .filter(getEventBus(), table);
  }

  private static class ResponseErrorCallback implements ResponseCodeCallback {

    private final EventBus eventBus;

    private final String message;

    private final List<String> messageArgs;

    private ResponseErrorCallback(EventBus widget, String message, String... messageArgs) {
      eventBus = widget;
      this.message = message;
      this.messageArgs = Arrays.asList(messageArgs);
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      NotificationEvent.Builder notificationBuilder = NotificationEvent.newBuilder();

      notificationBuilder.error(message.isEmpty() ? response.getStatusText() : message);

      if(!messageArgs.isEmpty()) {
        notificationBuilder.args(messageArgs);
      }

      eventBus.fireEvent(notificationBuilder.build());
    }
  }

  private class BadRequestCallback implements ResponseCodeCallback {

    @SuppressWarnings("unchecked")
    private List<JavaScriptErrorDto> extractJavaScriptErrors(ClientErrorDto errorDto) {
      List<JavaScriptErrorDto> javaScriptErrors = new ArrayList<JavaScriptErrorDto>();

      JsArray<JavaScriptErrorDto> errors = (JsArray<JavaScriptErrorDto>) errorDto
          .getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors);
      if(errors != null) {
        for(int i = 0; i < errors.length(); i++) {
          javaScriptErrors.add(errors.get(i));
        }
      }
      return javaScriptErrors;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
      if(errorDto.getExtension(JavaScriptErrorDto.ClientErrorDtoExtensions.errors) != null) {
        List<JavaScriptErrorDto> errors = extractJavaScriptErrors(errorDto);
        for(JavaScriptErrorDto error : errors) {
          getEventBus().fireEvent(NotificationEvent.newBuilder()
              .error(translationMessages.errorAt(error.getLineNumber(), error.getColumnNumber(), error.getMessage()))
              .build());
        }
      } else {
        getEventBus().fireEvent(
            NotificationEvent.newBuilder().error(errorDto.getStatus()).args(errorDto.getArgumentsArray()).build());
      }
    }
  }

  public interface Display extends PopupView, HasUiHandlers<EntityModalUiHandlers> {

    void setEntityType(String entityType);

    void setEntityId(String entityId);

    void setTables(JsArray<TableDto> tables, TableDto selectedTable);

    void setValueViewHandler(ValueViewHandler handler);

    void renderRows(List<VariableValueRow> rows);

    String getFilterText();

    void setFilterText(String filter);
  }

  public static class VariableValueRow {

    private final String variableName;

    private final ValueSetsDto.ValueDto valueDto;

    private final VariableDto variableDto;

    public VariableValueRow(String variableName, ValueSetsDto.ValueDto valueDto, VariableDto variableDto) {
      this.variableName = variableName;
      this.valueDto = valueDto;
      this.variableDto = variableDto;

    }

    public String getVariableName() {
      return variableName;
    }

    public ValueSetsDto.ValueDto getValueDto() {
      return valueDto;
    }

    public VariableDto getVariableDto() {
      return variableDto;
    }

    public String toString() {
      return "Variable: " + variableName + " value: " + valueDto.getValue() + " type: " + variableDto.getValueType();
    }

  }

  public interface ValueSelectionHandler {
    void onValueSelection(VariableValueRow variableValueRow);
  }

  public interface ValueViewHandler {
    void requestValueSequenceView(VariableDto variableDto);

    void requestBinaryValueView(VariableDto variable);

    void requestGeoValueView(VariableDto variable, ValueSetsDto.ValueDto value);

    void requestEntityView(VariableDto variable, ValueSetsDto.ValueDto value);
  }

  public class ValueSequenceHandlerImpl implements ValueViewHandler {

    @Override
    public void requestValueSequenceView(VariableDto variableDto) {
      ValueSequencePopupPresenter valueSequencePopupPresenter = valueSequencePopupProvider.get();
      valueSequencePopupPresenter.initialize(selectedTable, variableDto, entityId, true);
    }

    @Override
    public void requestBinaryValueView(VariableDto variableDto) {
      StringBuilder link = new StringBuilder(selectedTable.getLink());
      link.append("/valueSet/").append(entityId).append("/variable/").append(variableDto.getName()).append("/value");
      getEventBus().fireEvent(new FileDownloadRequestEvent(link.toString()));
    }

    @Override
    public void requestGeoValueView(VariableDto variable, ValueSetsDto.ValueDto value) {
      getEventBus().fireEvent(new GeoValueDisplayEvent(variable, entityId, value));
    }

    @Override
    public void requestEntityView(VariableDto variable, ValueSetsDto.ValueDto value) {
      initialize(null, variable.getReferencedEntityType(), value.getValue(), "");
    }
  }
}
