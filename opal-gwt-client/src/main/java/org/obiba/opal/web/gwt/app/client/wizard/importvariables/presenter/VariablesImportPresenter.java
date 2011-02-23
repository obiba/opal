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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.Wizard;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class VariablesImportPresenter extends WidgetPresenter<VariablesImportPresenter.Display> implements Wizard {
  //
  // Constants
  //

  private static final String EXCEL_TEMPLATE = "/opalVariableTemplate.xls";

  //
  // Instance Variables
  //

  @Inject
  private ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter;

  @Inject
  private ConclusionStepPresenter conclusionPresenter;

  @Inject
  private FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  //
  // Constructors
  //

  @Inject
  public VariablesImportPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    comparedDatasourcesReportPresenter.bind();
    getDisplay().setComparedDatasourcesReportDisplay(comparedDatasourcesReportPresenter.getDisplay());

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    fileSelectionPresenter.bind();
    getDisplay().setFileSelectionDisplay(fileSelectionPresenter.getDisplay());

    conclusionPresenter.bind();
    getDisplay().setConclusionDisplay(conclusionPresenter.getDisplay());

    initDatasources();
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    datasourceName = null;
  }

  private void initDatasources() {
    if(datasourceName != null) {
      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasource/" + datasourceName).get().withCallback(new ResourceCallback<DatasourceDto>() {
        @Override
        public void onResource(Response response, DatasourceDto resource) {
          JsArray<DatasourceDto> datasources = (JsArray<DatasourceDto>) JsArray.createArray();
          if(resource != null) {
            datasources.push(resource);
          }
          getDisplay().setDatasources(datasources);
        }
      }).send();
    } else {
      ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
        @Override
        public void onResource(Response response, JsArray<DatasourceDto> resource) {
          JsArray<DatasourceDto> datasources = resource != null ? resource : (JsArray<DatasourceDto>) JsArray.createArray();
          getDisplay().setDatasources(datasources);
        }
      }).send();
    }
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addDownloadExcelTemplateClickHandler(new DownloadExcelTemplateClickHandler()));
    super.registerHandler(getDisplay().addFileSelectedClickHandler(new FileSelectedHandler()));
    getDisplay().setFileSelectionValidator(new FileSelectionValidator());
    getDisplay().addImportClickHandler(new ImportHandler());
    getDisplay().setImportableValidator(new ImportableValidator());

  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Wizard Methods
  //

  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 0) {
      if(event.getEventParameters()[0] instanceof String) {
        datasourceName = (String) event.getEventParameters()[0];
      } else {
        throw new IllegalArgumentException("unexpected event parameter type (expected String)");
      }
    }
  }

  //
  // Inner Classes / Interfaces
  //

  private final class ImportableValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(!comparedDatasourcesReportPresenter.canBeSubmitted()) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "NotIgnoredConlicts", null));
        return false;
      }

      conclusionPresenter.clearResourceRequests();
      comparedDatasourcesReportPresenter.addUpdateVariablesResourceRequests(conclusionPresenter);
      if(conclusionPresenter.getResourceRequestCount() == 0) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "NoVariablesToBeImported", null));
        return false;
      }

      return true;
    }
  }

  private final class FileSelectionValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(getDisplay().getSelectedFile().length() > 0 && (getDisplay().getSelectedFile().endsWith(".xls") || getDisplay().getSelectedFile().endsWith(".xlsx"))) {
        return true;
      } else {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "ExcelFileRequired", null));
        return false;
      }
    }
  }

  public interface Display extends WidgetDisplay {

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);

    void setFileSelectionValidator(ValidationHandler handler);

    void setImportableValidator(ValidationHandler handler);

    void setComparedDatasourcesReportDisplay(ComparedDatasourcesReportStepPresenter.Display display);

    HandlerRegistration addDownloadExcelTemplateClickHandler(ClickHandler handler);

    HandlerRegistration addFileSelectedClickHandler(ClickHandler handler);

    String getSelectedFile();

    DatasourceCreatedCallback getDatasourceCreatedCallback();

    void hideErrors();

    void hideDialog();

    void showDialog();

    String getSelectedDatasource();

    void setDatasources(JsArray<DatasourceDto> datasources);

    void setConclusionDisplay(ConclusionStepPresenter.Display display);

    HandlerRegistration addImportClickHandler(ClickHandler handler);

  }

  class DownloadExcelTemplateClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      String url = new StringBuilder("/templates").append(EXCEL_TEMPLATE).toString();
      eventBus.fireEvent(new FileDownloadEvent(url));
    }
  }

  class FileSelectedHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      getDisplay().hideErrors();

      final DatasourceFactoryDto factory = createDatasourceFactoryDto(getDisplay().getSelectedFile());

      ResourceCallback<DatasourceDto> callback = new ResourceCallback<DatasourceDto>() {

        public void onResource(Response response, DatasourceDto resource) {
          if(response.getStatusCode() == 201) {
            comparedDatasourcesReportPresenter.compare(((DatasourceDto) resource).getName(), getDisplay().getSelectedDatasource(), getDisplay(), factory, resource);
          }
        }
      };

      ResponseCodeCallback errorCallback = new ResponseCodeCallback() {

        public void onResponseCode(Request request, Response response) {
          final ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());

          if(errorDto.getExtension(ClientErrorDtoExtensions.errors) == null) {
            eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "fileReadError", null));
          }
          getDisplay().getDatasourceCreatedCallback().onFailure(factory, errorDto);
        }
      };

      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/transient-datasources").post().withResourceBody(DatasourceFactoryDto.stringify(factory)).withCallback(callback).withCallback(400, errorCallback).withCallback(500, errorCallback).send();
    }

    private DatasourceFactoryDto createDatasourceFactoryDto(String tmpFilePath) {
      ExcelDatasourceFactoryDto excelDto = ExcelDatasourceFactoryDto.create();
      excelDto.setFile(tmpFilePath);
      excelDto.setReadOnly(true);

      DatasourceFactoryDto dto = DatasourceFactoryDto.create();
      dto.setExtension(ExcelDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, excelDto);

      return dto;
    }
  }

  class ImportHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      conclusionPresenter.sendResourceRequests();
    }

  }

}
