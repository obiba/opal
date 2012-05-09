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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.FileViewDto;
import org.obiba.opal.web.model.client.magma.FileViewDto.FileViewType;
import org.obiba.opal.web.model.client.magma.StaticDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VariablesImportPresenter extends WizardPresenterWidget<VariablesImportPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  public static class Wizard extends WizardProxy<VariablesImportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<VariablesImportPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  private static final String EXCEL_TEMPLATE = "/opalVariableTemplate.xls";

  private final ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter;

  private final ConclusionStepPresenter conclusionPresenter;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public VariablesImportPresenter(final Display display, final EventBus eventBus,
      ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter,
      ConclusionStepPresenter conclusionPresenter, FileSelectionPresenter fileSelectionPresenter) {
    super(eventBus, display);
    this.comparedDatasourcesReportPresenter = comparedDatasourcesReportPresenter;
    this.conclusionPresenter = conclusionPresenter;
    this.fileSelectionPresenter = fileSelectionPresenter;
  }

  @Override
  protected void onBind() {
    super.onBind();
    comparedDatasourcesReportPresenter.bind();
    getView().setComparedDatasourcesReportDisplay(comparedDatasourcesReportPresenter.getDisplay());

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    fileSelectionPresenter.bind();
    getView().setFileSelectionDisplay(fileSelectionPresenter.getDisplay());

    conclusionPresenter.bind();
    getView().setConclusionDisplay(conclusionPresenter.getDisplay());

    initDatasources();
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    datasourceName = null;
  }

  private void initDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            getView().setDatasources(JsArrays.toSafeArray(resource));
            if(datasourceName != null) {
              getView().setSelectedDatasource(datasourceName);
            }
          }
        }).send();
  }

  protected void addEventHandlers() {
    super.registerHandler(getView().addDownloadExcelTemplateClickHandler(new DownloadExcelTemplateClickHandler()));
    super.registerHandler(getView().addFileSelectedClickHandler(new FileSelectedHandler()));
    getView().setFileSelectionValidator(new FileSelectionValidator());
    getView().setImportableValidator(new ImportableValidator());

  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length > 0) {
      datasourceName = (String) event.getEventParameters()[0];
      getView().setSelectedDatasource(datasourceName);
    }
  }

  @Override
  protected void onFinish() {
    super.onFinish();
    conclusionPresenter.sendResourceRequests();
  }

  private final class ImportableValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(comparedDatasourcesReportPresenter.getSelectedTables().size() == 0) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("TableSelectionIsRequired").build());
        return false;
      } else if(!comparedDatasourcesReportPresenter.canBeSubmitted()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("NotIgnoredConlicts").build());
        return false;
      }

      conclusionPresenter.clearResourceRequests();
      comparedDatasourcesReportPresenter.addUpdateVariablesResourceRequests(conclusionPresenter);
      if(conclusionPresenter.getResourceRequestCount() == 0) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("NoVariablesToBeImported").build());
        return false;
      }

      return true;
    }
  }

  private final class FileSelectionValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(getView().getSelectedFile().length() > 0
          && (getView().getSelectedFile().endsWith(".xls") || getView().getSelectedFile().endsWith(".xlsx") || getView()
              .getSelectedFile().endsWith(".xml"))) {
        return true;
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("ExcelFileRequired").build());
        return false;
      }
    }
  }

  public interface Display extends WizardView {

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);

    void setFileSelectionValidator(ValidationHandler handler);

    void setImportableValidator(ValidationHandler handler);

    void setComparedDatasourcesReportDisplay(ComparedDatasourcesReportStepPresenter.Display display);

    HandlerRegistration addDownloadExcelTemplateClickHandler(ClickHandler handler);

    HandlerRegistration addFileSelectedClickHandler(ClickHandler handler);

    String getSelectedFile();

    DatasourceCreatedCallback getDatasourceCreatedCallback();

    void hideErrors();

    String getSelectedDatasource();

    void setSelectedDatasource(String dsName);

    void setDatasources(JsArray<DatasourceDto> datasources);

    void setConclusionDisplay(ConclusionStepPresenter.Display display);

  }

  class DownloadExcelTemplateClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      String url = new StringBuilder("/templates").append(EXCEL_TEMPLATE).toString();
      getEventBus().fireEvent(new FileDownloadEvent(url));
    }
  }

  class FileSelectedHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      getView().hideErrors();

      final DatasourceFactoryDto factory = createDatasourceFactoryDto(getView().getSelectedFile());

      ResourceCallback<DatasourceDto> callback = new ResourceCallback<DatasourceDto>() {

        public void onResource(Response response, DatasourceDto resource) {
          if(response.getStatusCode() == 201) {
            comparedDatasourcesReportPresenter.compare(((DatasourceDto) resource).getName(), getView()
                .getSelectedDatasource(), getView().getDatasourceCreatedCallback(), factory, resource);
          }
        }
      };

      ResponseCodeCallback errorCallback = new ResponseCodeCallback() {

        public void onResponseCode(Request request, Response response) {
          final ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());

          if(errorDto.getExtension(ClientErrorDtoExtensions.errors) == null) {
            getEventBus().fireEvent(NotificationEvent.newBuilder().error("fileReadError").build());
          }
          getView().getDatasourceCreatedCallback().onFailure(factory, errorDto);
        }
      };

      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder()//
          .forResource("/transient-datasources").post().withResourceBody(DatasourceFactoryDto.stringify(factory))//
          .withCallback(callback)//
          .withCallback(400, errorCallback).withCallback(500, errorCallback).send();
    }

    private DatasourceFactoryDto createDatasourceFactoryDto(String tmpFilePath) {
      if(tmpFilePath.endsWith(".xls") || tmpFilePath.endsWith(".xlsx")) {
        return createExcelDatasourceFactoryDto(tmpFilePath);
      } else {
        return createStaticDatasourceFactoryDto(tmpFilePath);
      }
    }

    private DatasourceFactoryDto createExcelDatasourceFactoryDto(String tmpFilePath) {
      ExcelDatasourceFactoryDto excelDto = ExcelDatasourceFactoryDto.create();
      excelDto.setFile(tmpFilePath);
      excelDto.setReadOnly(true);

      DatasourceFactoryDto dto = DatasourceFactoryDto.create();
      dto.setExtension(ExcelDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, excelDto);

      return dto;
    }

    private DatasourceFactoryDto createStaticDatasourceFactoryDto(String tmpFilePath) {
      StaticDatasourceFactoryDto staticDto = StaticDatasourceFactoryDto.create();
      ViewDtoBuilder viewDtoBuilder = ViewDtoBuilder.newBuilder();
      String name = tmpFilePath.substring(tmpFilePath.lastIndexOf('/') + 1, tmpFilePath.lastIndexOf('.'));
      viewDtoBuilder.setName(name);

      FileViewDto fileView = FileViewDto.create();
      fileView.setFilename(tmpFilePath);
      fileView.setType(FileViewType.SERIALIZED_XML);

      viewDtoBuilder.fileView(fileView);
      JsArray<ViewDto> views = JsArrays.create();
      views.push(viewDtoBuilder.build());
      staticDto.setViewsArray(views);

      DatasourceFactoryDto dto = DatasourceFactoryDto.create();
      dto.setExtension(StaticDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, staticDto);

      return dto;
    }
  }

}
