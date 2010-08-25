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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;
import org.obiba.opal.web.model.client.ws.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.client.ws.DatasourceParsingErrorDto.ClientErrorDtoExtensions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.inject.Inject;

public class UploadVariablesStepPresenter extends WidgetPresenter<UploadVariablesStepPresenter.Display> {
  //
  // Constants
  //

  private static final String EXCEL_TEMPLATE = "/opalVariableTemplate.xls";

  //
  // Instance Variables
  //

  @Inject
  private SelectDestinationDatasourceStepPresenter destinationDatasourceStepPresenter;

  @Inject
  private ValidationReportStepPresenter validationReportStepPresenter;

  //
  // Constructors
  //

  @Inject
  public UploadVariablesStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
    super.registerHandler(getDisplay().addDownloadExcelTemplateClickHandler(new DownloadExcelTemplateClickHandler()));
    super.registerHandler(getDisplay().addUploadCompleteHandler(new UploadCompleteHandler()));
  }

  @Override
  public void revealDisplay() {
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
  // Methods
  //

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    HandlerRegistration addDownloadExcelTemplateClickHandler(ClickHandler handler);

    HandlerRegistration addUploadCompleteHandler(FormPanel.SubmitCompleteHandler handler);

    String getVariablesFilename();

    void uploadVariablesFile();
  }

  class NextClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      getDisplay().uploadVariablesFile();
    }
  }

  class DownloadExcelTemplateClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      String uri = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "")).append("ws/templates").append(EXCEL_TEMPLATE).toString();
      eventBus.fireEvent(new FileDownloadEvent(uri));
    }
  }

  class UploadCompleteHandler implements FormPanel.SubmitCompleteHandler {

    public void onSubmitComplete(SubmitCompleteEvent event) {

      ResourceCallback<DatasourceDto> callback = new ResourceCallback<DatasourceDto>() {

        public void onResource(Response response, DatasourceDto resource) {
          if(response.getStatusCode() == 201) {
            destinationDatasourceStepPresenter.setSourceDatasourceName(((DatasourceDto) resource).getName());
            eventBus.fireEvent(new WorkbenchChangeEvent(destinationDatasourceStepPresenter));
          }
        }
      };

      ResponseCodeCallback errorCallback = new ResponseCodeCallback() {

        public void onResponseCode(Request request, Response response) {
          final ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());

          if(errorDto.getExtension(ClientErrorDtoExtensions.errors) != null) {
            validationReportStepPresenter.getDisplay().setErrors(extractDatasourceParsingErrors(errorDto));
            eventBus.fireEvent(new WorkbenchChangeEvent(validationReportStepPresenter));
          } else {
            eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, "fileReadError", null));
          }
        }
      };

      DatasourceFactoryDto dto = createDatasourceFactoryDto();
      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasources").post().accept("application/x-protobuf+json").withResourceBody(DatasourceFactoryDto.stringify(dto)).withCallback(callback).withCallback(400, errorCallback).withCallback(500, errorCallback).send();
    }

    private DatasourceFactoryDto createDatasourceFactoryDto() {
      ExcelDatasourceFactoryDto excelDto = ExcelDatasourceFactoryDto.create();
      excelDto.setFile("/tmp/" + getDisplay().getVariablesFilename());
      excelDto.setReadOnly(true);

      DatasourceFactoryDto dto = DatasourceFactoryDto.create();
      dto.setExtension(ExcelDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, excelDto);

      return dto;
    }

    @SuppressWarnings("unchecked")
    private List<DatasourceParsingErrorDto> extractDatasourceParsingErrors(ClientErrorDto dto) {
      List<DatasourceParsingErrorDto> datasourceParsingErrors = new ArrayList<DatasourceParsingErrorDto>();

      JsArray<DatasourceParsingErrorDto> errors = (JsArray<DatasourceParsingErrorDto>) dto.getExtension(ClientErrorDtoExtensions.errors);
      if(errors != null) {
        for(int i = 0; i < errors.length(); i++) {
          datasourceParsingErrors.add(errors.get(i));
        }
      }

      return datasourceParsingErrors;
    }
  }
}
