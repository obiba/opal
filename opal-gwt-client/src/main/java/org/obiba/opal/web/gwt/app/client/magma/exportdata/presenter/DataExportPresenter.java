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
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.identifiers.IdentifiersMappingConfigDto;
import org.obiba.opal.web.model.client.identifiers.IdentifiersMappingDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.ExportCommandOptionsDto;

import com.google.common.base.Strings;
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

  private TranslationMessages translationMessages;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  private String valuesQuery;

  @Inject
  public DataExportPresenter(Display display, EventBus eventBus, Translations translations,
      TranslationMessages translationMessages, FileSelectionPresenter fileSelectionPresenter, RequestCredentials credentials) {
    super(eventBus, display);
    this.translations = translations;
    this.translationMessages = translationMessages;
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
    } else {
      getView().showExportNAlert(translationMessages.exportNTables(exportTables.size()));
    }
  }

  public void setValuesQuery(String query, String text) {
    valuesQuery = query;
    getView().setValuesQuery(Strings.isNullOrEmpty(query) || "*".equals(query) ? "" : text);
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
        getView().setIdentifiersMappings(getEntityTypeMappings(exportTables, mappings));
      }

      private JsArray<IdentifiersMappingDto> getEntityTypeMappings(Set<TableDto> tables,
          JsArray<IdentifiersMappingDto> mappings) {
        JsArray<IdentifiersMappingDto> entityTypeMappings = JsArrays.create();

        for(IdentifiersMappingDto mapping : JsArrays.toIterable(mappings)) {
          for(TableDto table : tables) {
            if(hasEntityType(table, mapping)) {
              entityTypeMappings.push(mapping);
              break;
            }
          }
        }

        return entityTypeMappings;
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
    if (!Strings.isNullOrEmpty(valuesQuery) && getView().applyQuery()) {
      dto.setQuery(valuesQuery);
    }

    return dto;
  }

  //
  // Interfaces and classes
  //

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

    void setValuesQuery(String query);

    boolean applyQuery();

    void setIdentifiersMappings(JsArray<IdentifiersMappingDto> mappings);

    void setFileWidgetDisplay(FileSelectionPresenter.Display display);

    void setFileBaseName(String username);

    void showExportNAlert(String message);

    void hideDialog();
  }

}
