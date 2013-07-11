/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.navigator.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.navigator.util.VariablesFilter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ValueSequencePopupPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.TextBoxClearable;
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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Event;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class EntityDialogPresenter extends PresenterWidget<EntityDialogPresenter.Display> {

  private String entityType;

  private String entityId;

  private TableDto selectedTable;

  private Map<String, VariableDto> variablesMap = new HashMap<String, VariableDto>();

  private final ValueSequencePopupPresenter valueSequencePopupPresenter;

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  @Inject
  public EntityDialogPresenter(EventBus eventBus, Display display,
      ValueSequencePopupPresenter valueSequencePopupPresenter) {
    super(eventBus, display);
    this.valueSequencePopupPresenter = valueSequencePopupPresenter;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public void initialize(TableDto table, String entityType, String entityId, String filterText) {
    selectedTable = table;
    this.entityType = entityType;
    this.entityId = entityId;
    getView().getFilter().setText(filterText);
  }

  @Override
  protected void onBind() {
    addChangeHandlers();
    addCloseHandler();
  }

  private void addChangeHandlers() {
    getView().getTableChooser().addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        TableDto table = getView().getSelectedTable();
        if(table != null) {
          // Fetch table info
          UriBuilder uriBuilder = UriBuilder.create()
              .segment("datasource", table.getDatasourceName(), "table", table.getName());
          ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(uriBuilder.build()).get()
              .withCallback(new ResourceCallback<TableDto>() {
                @Override
                public void onResource(Response response, TableDto resource) {
                  selectedTable = resource;
                  loadVariables(selectedTable, getView().getFilter().getText());
                }
              }).send();
        }
      }
    });
    getView().getFilter().getClear().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        loadVariables(selectedTable, "");
      }
    });
  }

  private void addCloseHandler() {
    registerHandler(getView().getButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getView().hide();
      }
    }));
    // TODO disabled the ESCAPE key event handling to be implemented in the next sprint. Currently it causes a bug when two dialogs are on top of each other.
//    setEscapeKeyUpHandler();
  }

  @Override
  public void onReveal() {
    getView().setEntityType(entityType);
    getView().setEntityId(entityId);
    getView().setValueViewHandler(new ValueSequenceHandlerImpl());
    getView().setVariablesFilterHandler(new VariablesFilterHandlerImpl());

    loadTables();
  }

  /**
   * find all table where this entity appears
   */
  private void loadTables() {
    UriBuilder uriBuilder = UriBuilder.create().segment("entity", entityId, "type", entityType, "tables");
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(uriBuilder.build()).get()
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, new ResponseErrorCallback(getEventBus(), "InternalError"))
        .withCallback(Response.SC_NOT_FOUND,
            new ResponseErrorCallback(getEventBus(), "NoTablesForEntityIdType", entityId, entityType))//
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            getView().setTables(resource, selectedTable);
            loadVariables(selectedTable, getView().getFilter().getText());
          }
        }).send();
  }

  private void loadVariables(final TableDto table, String select) {
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
            .withCallback(Response.SC_NOT_FOUND, new ResponseErrorCallback(getEventBus(), "NoVariableValuesFound"))
            .withCallback(Response.SC_BAD_REQUEST, new BadRequestCallback())//
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

  // TODO Generalized this function so other Presenters can use it
  private void setEscapeKeyUpHandler() {
    Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        Event nativeEvent = Event.as(event.getNativeEvent());
        if(nativeEvent.getTypeInt() == Event.ONKEYUP) {
          if(KeyCodes.KEY_ESCAPE == nativeEvent.getKeyCode()) {
            getView().hide();
          }
        }
      }
    });
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
      NotificationEvent.Builder notificationBuilder = NotificationEvent.Builder.newNotification();

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

  public interface Display extends PopupView {

    void setEntityType(String entityType);

    void setEntityId(String entityId);

    void setTables(JsArray<TableDto> tables, TableDto selectedTable);

    void setValueViewHandler(ValueViewHandler handler);

    void setVariablesFilterHandler(VariablesFilterHandler handler);

    void renderRows(List<VariableValueRow> rows);

    HasClickHandlers getButton();

    TableDto getSelectedTable();

    HasChangeHandlers getTableChooser();

    TextBoxClearable getFilter();
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
  }

  public class ValueSequenceHandlerImpl implements ValueViewHandler {

    @Override
    public void requestValueSequenceView(VariableDto variableDto) {
      valueSequencePopupPresenter.initialize(selectedTable, variableDto, entityId, true);
      addToPopupSlot(valueSequencePopupPresenter);
    }

    @Override
    public void requestBinaryValueView(VariableDto variableDto) {
      StringBuilder link = new StringBuilder(selectedTable.getLink());
      link.append("/valueSet/").append(entityId).append("/variable/").append(variableDto.getName()).append("/value");
      getEventBus().fireEvent(new FileDownloadEvent(link.toString()));
    }

    @Override
    public void requestGeoValueView(VariableDto variable, ValueSetsDto.ValueDto value) {
      getEventBus().fireEvent(new GeoValueDisplayEvent(variable, entityId, value));
    }
  }

  public interface VariablesFilterHandler {
    void filterVariables(String filter);
  }

  private class VariablesFilterHandlerImpl implements VariablesFilterHandler {

    @Override
    public void filterVariables(String filter) {
      loadVariables(selectedTable, filter);
    }
  }

}
