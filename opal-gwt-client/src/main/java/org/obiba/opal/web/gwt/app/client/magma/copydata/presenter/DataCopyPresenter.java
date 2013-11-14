/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials&
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.copydata.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.CopyCommandOptionsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class DataCopyPresenter extends ModalPresenterWidget<DataCopyPresenter.Display> implements DataCopyUiHandlers {

  private String datasourceName;

  private Set<TableDto> copyTables = new HashSet<TableDto>();

  private final Translations translations;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public DataCopyPresenter(Display display, EventBus eventBus, Translations translations) {
    super(eventBus, display);
    this.translations = translations;

    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    initDisplayComponents();
  }

  protected void initDisplayComponents() {
    getView().setDestinationValidator(new DestinationValidator());
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    datasourceName = null;
    copyTables = null;
  }

  @Override
  public void onReveal() {
    initDatasources();
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void onSubmit() {

    // if only 1 table is selected and is copied to current datasource, validate new table name
    String selectedDatasource = getView().getSelectedDatasource();
    String newName = getView().getNewName().getText();

    if(copyTables.size() == 1) {
      if(selectedDatasource.equals(datasourceName) && newName.isEmpty()) {
        getView()
            .showError(Display.FormField.NEW_TABLE_NAME, translations.userMessageMap().get("DataCopyNewNameRequired"));
      } else if(selectedDatasource.equals(datasourceName) && copyTables.iterator().next().getName().equals(newName)) {
        getView().showError(Display.FormField.NEW_TABLE_NAME,
            translations.userMessageMap().get("DataCopyNewNameAlreadyExists"));
      } else {
        sendCommandsCopyRequest();
      }
    } else {
      sendCommandsCopyRequest();
    }
  }

  private void sendCommandsCopyRequest() {
    getView().hideDialog();

    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.PROJECT_COMMANDS_COPY.create().build(datasourceName)) //
        .post() //
        .withResourceBody(CopyCommandOptionsDto.stringify(createCopyCommandOptions())) //
        .withCallback(Response.SC_BAD_REQUEST, new ClientFailureResponseCodeCallBack()) //
        .withCallback(Response.SC_CREATED, new SuccessResponseCodeCallBack()).send();
  }

  private CopyCommandOptionsDto createCopyCommandOptions() {
    CopyCommandOptionsDto dto = CopyCommandOptionsDto.create();

    JsArrayString selectedTables = JavaScriptObject.createArray().cast();

    for(TableDto exportTable : copyTables) {
      selectedTables.push(exportTable.getDatasourceName() + "." + exportTable.getName());
    }

    dto.setTablesArray(selectedTables);
    dto.setDestination(getView().getSelectedDatasource());
    dto.setDestinationTableName(getView().getNewName().getText());
    dto.setNonIncremental(!getView().isIncremental());
    dto.setCopyNullValues(getView().isCopyNullValues());
    dto.setNoVariables(!getView().isWithVariables());

    return dto;
  }

  private void initDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES.create().build()).get()
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
              getView().showError(null, translations.userMessageMap().get("NoDataToCopy"));
            }
          }

          private List<DatasourceDto> filterDatasources(JsArray<DatasourceDto> datasources) {

            List<DatasourceDto> filteredDatasources = new ArrayList<DatasourceDto>();

            for(DatasourceDto datasource : JsArrays.toList(datasources)) {
              // Allow to copy in itself if only one table is selected
              if(copyTables.size() == 1 || !datasource.getName().equals(datasourceName)) {
                filteredDatasources.add(datasource);
              }
            }
            return filteredDatasources;
          }
        }).send();
  }

  public void setCopyTables(Set<TableDto> tables, boolean allTables) {
    copyTables = tables;

    if(allTables) {
      getView().showCopyNAlert(translations.exportAllTables());
    } else if(copyTables.size() == 1) {
      getView().showCopyNAlert(translations.export1Table());
      getView().showNewName(tables.iterator().next().getName());
    } else {
      getView().showCopyNAlert(TranslationsUtils.replaceArguments(translations.exportNTables(), String.valueOf(copyTables.size())));
    }
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
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
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);

      getEventBus().fireEvent(
          NotificationEvent.newBuilder().info("DataCopyProcessLaunched").args(jobId, getView().getSelectedDatasource())
              .build());
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DataCopyUiHandlers> {

    enum FormField {
      NEW_TABLE_NAME
    }

    void setDestinationValidator(ValidationHandler handler);

    /**
     * Set a collection of datasources retrieved from Opal.
     */
    void setDatasources(List<DatasourceDto> datasources);

    /**
     * Get the datasource selected by the user.
     */
    String getSelectedDatasource();

    HasText getNewName();

    void showNewName(String name);

    boolean isIncremental();

    boolean isWithVariables();

    boolean isCopyNullValues();

    void hideDialog();

    void showCopyNAlert(String message);

    void showError(FormField field, String message);

  }

}
