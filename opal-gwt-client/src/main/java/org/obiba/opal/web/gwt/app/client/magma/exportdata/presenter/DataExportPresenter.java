/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.CopyCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class DataExportPresenter extends ModalPresenterWidget<DataExportPresenter.Display>
    implements DataExportUiHandlers {

  private static String DEFAULT_UNIT_NAME;

  private final RequestCredentials credentials;

  private Set<TableDto> exportTables = new HashSet<TableDto>();

  private final Translations translations;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  protected String identifierEntityType;

  @Inject
  public DataExportPresenter(Display display, EventBus eventBus, Translations translations,
      FileSelectionPresenter fileSelectionPresenter, RequestCredentials credentials) {
    super(eventBus, display);
    this.translations = translations;
    this.fileSelectionPresenter = fileSelectionPresenter;
    this.credentials = credentials;

    DEFAULT_UNIT_NAME = translations.opalDefaultIdentifiersLabel();
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    initDisplayComponents();
  }

  protected void initDisplayComponents() {
    initFileSelectionType();
    fileSelectionPresenter.bind();
    fileSelectionPresenter.getView().setFile("/home/" + credentials.getUsername() + "/export");
    getView().setFileWidgetDisplay(fileSelectionPresenter.getView());
    getView().setDestinationValidator(new DestinationValidator());
  }

  private void initFileSelectionType() {
    fileSelectionPresenter.setFileSelectionType(FileSelectionType.FOLDER);
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    fileSelectionPresenter.unbind();
    datasourceName = null;
    exportTables = null;
  }

  @Override
  public void onReveal() {
    initUnits();
    getView().setUsername(credentials.getUsername());
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  public void setExportTables(Set<TableDto> tables, boolean allTables) {
    exportTables = tables;

    if(allTables) {
      getView().getExportNAlert().setText(translations.exportAllTables());
    } else if(exportTables.size() == 1) {
      getView().getExportNAlert().setText(translations.export1Table());
    } else {
      getView().getExportNAlert().setText(
          TranslationsUtils.replaceArguments(translations.exportNTables(), String.valueOf(exportTables.size())));
    }
  }

  public void setDatasourceName(String name) {
    datasourceName = name;
  }

  private void initUnits() {

    final ResponseCodeCallback errorCallback = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
      }
    };

    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource("/functional-units/entities/table").get()
        .withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, TableDto resource) {
            identifierEntityType = resource.getEntityType();

            // if exporting at least one table of type 'identifiersEntityType', show units selection
            boolean fetchUnits = false;
            for(TableDto table : exportTables) {
              if(table.hasEntityType() && table.getEntityType().equals(identifierEntityType)) {
                fetchUnits = true;
                break;
              }
            }

            if(fetchUnits) {
              ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>>newBuilder().forResource("/functional-units")
                  .get().withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
                @Override
                public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
                  getView().setUnits(units);
                }
              }).withCallback(Response.SC_FORBIDDEN, errorCallback).send();
            }
          }
        }).withCallback(Response.SC_FORBIDDEN, errorCallback).send();
  }

  @Override
  public void onSubmit() {
    getView().hideDialog();

    UriBuilder uriBuilder = UriBuilder.create();
    if(datasourceName == null) {
      uriBuilder.segment("shell", "copy");
    } else {
      uriBuilder.segment("project", datasourceName, "commands", "_export");
    }
    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).post() //
        .withResourceBody(CopyCommandOptionsDto.stringify(createCopyCommandOptions())) //
        .withCallback(Response.SC_BAD_REQUEST, new ClientFailureResponseCodeCallBack()) //
        .withCallback(Response.SC_CREATED, new SuccessResponseCodeCallBack()).send();
  }

  private CopyCommandOptionsDto createCopyCommandOptions() {
    CopyCommandOptionsDto dto = CopyCommandOptionsDto.create();

    JsArrayString selectedTables = JavaScriptObject.createArray().cast();

    for(TableDto exportTable : exportTables) {
      selectedTables.push(exportTable.getDatasourceName() + "." + exportTable.getName());
    }

    dto.setTablesArray(selectedTables);
    dto.setFormat(getView().getFileFormat());
    dto.setOut(getView().getOutFile());
    dto.setNonIncremental(!getView().isIncremental());
    dto.setNoVariables(!getView().isWithVariables());
    if(!getView().getSelectedUnit().equals(DEFAULT_UNIT_NAME)) dto.setUnit(getView().getSelectedUnit());

    return dto;
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
      List<String> result = new ArrayList<String>();

      String filename = getView().getOutFile();
      if(filename == null || "".equals(filename)) {
        result.add("DestinationFileIsMissing");
      }
      return result;
    }
  }

  class ClientFailureResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      String destination = getView().getOutFile();
      getEventBus().fireEvent(
          NotificationEvent.newBuilder().info("DataExportationProcessLaunched").args(jobId, destination).build());
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DataExportUiHandlers> {

    void setDestinationValidator(ValidationHandler handler);

    /**
     * Set a collection of Opal units retrieved from Opal.
     */
    void setUnits(JsArray<FunctionalUnitDto> units);

    /**
     * Get the Opal unit selected by the user.
     */
    String getSelectedUnit();

    String getOutFile();

    String getFileFormat();

    boolean isIncremental();

    boolean isWithVariables();

//    boolean isUnitId();

    void setFileWidgetDisplay(FileSelectionPresenter.Display display);

    void setUsername(String username);

    Alert getExportNAlert();

    void hideDialog();
  }

}
