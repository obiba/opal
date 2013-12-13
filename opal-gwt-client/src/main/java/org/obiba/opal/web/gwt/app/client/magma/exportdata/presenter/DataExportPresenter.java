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

import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.identifiers.IdentifiersMappingConfigDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.ExportCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.IdentifiersMappingDto;

import com.google.common.collect.Sets;
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

  private final RequestCredentials credentials;

  private Set<TableDto> exportTables = Sets.newHashSet();

  private final Translations translations;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  @Inject
  public DataExportPresenter(Display display, EventBus eventBus, Translations translations,
      FileSelectionPresenter fileSelectionPresenter, RequestCredentials credentials) {
    super(eventBus, display);
    this.translations = translations;
    this.fileSelectionPresenter = fileSelectionPresenter;
    this.credentials = credentials;

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
    initIdentifiersMappings();
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  public void setExportTables(Set<TableDto> tables, boolean allTables) {
    exportTables = tables;

    if(allTables) {
      getView().showExportNAlert(translations.exportAllTables());
    } else if(exportTables.size() == 1) {
      getView().showExportNAlert(translations.export1Table());
    } else {
      getView().showExportNAlert(
          TranslationsUtils.replaceArguments(translations.exportNTables(), String.valueOf(exportTables.size())));
    }
  }

  public void setDatasourceName(String name) {
    datasourceName = name;
    getView().setFileBaseName(datasourceName);
  }

  private void initIdentifiersMappings() {

    ResponseCodeCallback errorCallback = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
      }
    };

    ResourceRequestBuilderFactory.<JsArray<IdentifiersMappingDto>>newBuilder().forResource("/identifiers/mappings")
        .get().withCallback(new ResourceCallback<JsArray<IdentifiersMappingDto>>() {
      @Override
      public void onResource(Response response, JsArray<IdentifiersMappingDto> resource) {

        JsArray<IdentifiersMappingDto> mappings = JsArrays.toSafeArray(resource);

        // if exporting at least one table of type 'identifiersEntityType', show id mappings selection
        for(TableDto table : exportTables) {
          if(hasEntityTypeMapping(table, mappings)) {
            getView().setIdentifiersMappings(mappings);
            break;
          }
        }
      }

      private boolean hasEntityTypeMapping(TableDto table, JsArray<IdentifiersMappingDto> mappings) {
        for(IdentifiersMappingDto mapping : JsArrays.toIterable(mappings)) {
          if(hasEntityType(table, mapping)) return true;
        }
        return false;
      }

      private boolean hasEntityType(TableDto table, IdentifiersMappingDto mapping) {
        for(String type : JsArrays.toIterable(mapping.getEntityTypesArray())) {
          if(type.equals(table.getEntityType())) {
            return true;
          }
        }
        return false;
      }

    }).withCallback(Response.SC_FORBIDDEN, errorCallback).send();
  }

  @Override
  public void onSubmit(String fileFormat, String outFile, String idMapping) {
    getView().hideDialog();

    UriBuilder uriBuilder = UriBuilders.PROJECT_COMMANDS_EXPORT.create();
    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build(datasourceName)).post() //
        .withResourceBody(
            ExportCommandOptionsDto.stringify(createExportCommandOptions(fileFormat, outFile, idMapping))) //
        .withCallback(Response.SC_BAD_REQUEST, new ClientFailureResponseCodeCallBack()) //
        .withCallback(Response.SC_CREATED, new SuccessResponseCodeCallBack(outFile)).send();
  }

  private ExportCommandOptionsDto createExportCommandOptions(String fileFormat, String outFile, String idMapping) {
    ExportCommandOptionsDto dto = ExportCommandOptionsDto.create();

    JsArrayString selectedTables = JavaScriptObject.createArray().cast();

    for(TableDto exportTable : exportTables) {
      selectedTables.push(exportTable.getDatasourceName() + "." + exportTable.getName());
    }

    dto.setTablesArray(selectedTables);
    dto.setFormat(fileFormat);
    dto.setOut(outFile);
    dto.setNonIncremental(true);
    dto.setNoVariables(false);
    if(idMapping != null) {
      IdentifiersMappingConfigDto idConfig = IdentifiersMappingConfigDto.create();
      idConfig.setName(idMapping);
      idConfig.setAllowIdentifierGeneration(false);
      idConfig.setIgnoreUnknownIdentifier(false);
      dto.setIdConfig(idConfig);
    }

    return dto;
  }

  //
  // Interfaces and classes
  //

  class ClientFailureResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {

    private final String outFile;

    SuccessResponseCodeCallBack(String outFile) {
      this.outFile = outFile;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      getEventBus().fireEvent(
          NotificationEvent.newBuilder().info("DataExportationProcessLaunched").args(jobId, outFile).build());
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DataExportUiHandlers> {

    void setIdentifiersMappings(JsArray<IdentifiersMappingDto> mappings);

    void setFileWidgetDisplay(FileSelectionPresenter.Display display);

    void setFileBaseName(String username);

    void showExportNAlert(String message);

    void hideDialog();
  }

}
