/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.support.DatasourceFileType;
import org.obiba.opal.web.gwt.app.client.presenter.CharacterSetDisplay;
import org.obiba.opal.web.gwt.app.client.support.DatasourceParsingErrorDtos;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.CharacterSetEncodingValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.LocaleValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.client.magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.FileViewDto;
import org.obiba.opal.web.model.client.magma.FileViewDto.FileViewType;
import org.obiba.opal.web.model.client.magma.StaticDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.search.QueryResultDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_OK;

public class VariablesImportPresenter extends WizardPresenterWidget<VariablesImportPresenter.Display>
    implements VariablesImportUiHandlers {

  public static final WizardType WIZARD_TYPE = new WizardType();

  public static class Wizard extends WizardProxy<VariablesImportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<VariablesImportPresenter> wizardProvider) {
      super(eventBus, WIZARD_TYPE, wizardProvider);
    }

  }

  private static final short MAX_ERROR_ALERTS = 5;

  private static final String EXCEL_TEMPLATE = "/opalVariableTemplate.xls";

  private final ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter;

  private final ConclusionStepPresenter conclusionPresenter;

  private final FileSelectionPresenter fileSelectionPresenter;

  private final Translations translations;

  private final TranslationMessages translationMessages;

  private String transientDatasourceName;

  private String datasourceName;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public VariablesImportPresenter(Display display, EventBus eventBus,
      ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter,
      ConclusionStepPresenter conclusionPresenter, FileSelectionPresenter fileSelectionPresenter,
      Translations translations, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.comparedDatasourcesReportPresenter = comparedDatasourcesReportPresenter;
    this.conclusionPresenter = conclusionPresenter;
    this.fileSelectionPresenter = fileSelectionPresenter;
    this.translations = translations;
    this.translationMessages = translationMessages;
    init();
  }

  private void init() {
    getView().setUiHandlers(this);
    setDefaultCharset();
  }

  @Override
  public void onCancel() {
    super.onCancel();
    deleteTransientDatasource();
  }

  @Override
  public void onModalHidden() {
    super.onModalHidden();
    deleteTransientDatasource();
  }

  @Override
  public void processVariablesFile() {
    getView().clearErrors();
    if(!new ViewValidator().validate()) return;
    createTransientDatasource();
    getView().gotoPreview();
  }

  @Override
  public void createTable() {
    if(!new ViewImportValidator().validate()) return;
    conclusionPresenter.sendResourceRequests();
    getView().hide();
  }

  @Override
  public void downExcelTemplate() {
    fireEvent(new FileDownloadRequestEvent("/templates" + EXCEL_TEMPLATE));
  }

  @Override
  protected void onBind() {
    super.onBind();
    comparedDatasourcesReportPresenter.bind();
    getView().setComparedDatasourcesReportDisplay(comparedDatasourcesReportPresenter.getView());

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
    fileSelectionPresenter.bind();
    getView().setFileSelectionDisplay(fileSelectionPresenter.getView());
    addHandler(FileSelectionEvent.getType(), new FileSelectionEvent.Handler() {
      @Override
      public void onFileSelection(FileSelectionEvent event) {
        getView().clearErrors();
      }
    });

    conclusionPresenter.bind();
    getView().setConclusionDisplay(conclusionPresenter.getView());
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    datasourceName = null;
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length > 0) {
      datasourceName = (String) event.getEventParameters()[0];
    }
  }

  private void setDefaultCharset() {
    ResourceRequestBuilderFactory.<QueryResultDto>newBuilder() //
        .forResource(UriBuilders.SYSTEM_CHARSET.create().build()) //
        .accept("text/plain")
        .get() //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setDefaultCharset(response.getText());
          }
        }, Response.SC_OK).send();
  }

  private void createTransientDatasource() {
    if(!Strings.isNullOrEmpty(transientDatasourceName)) deleteTransientDatasource();
    DatasourceFactoryDto factory = createDatasourceFactoryDto(getView().getSelectedFile());
    ResponseCodeCallback errorCallback = new TransientDatasourceFailureCallback(datasourceName);

    ResourceRequestBuilderFactory.<DatasourceDto>newBuilder() //
        .forResource(UriBuilders.PROJECT_TRANSIENT_DATASOURCE.create().build(datasourceName)) //
        .post() //
        .withResourceBody(DatasourceFactoryDto.stringify(factory)) //
        .withCallback(new TransientDatasourceSuccessCallback(factory)) //
        .withCallback(SC_BAD_REQUEST, errorCallback) //
        .withCallback(SC_INTERNAL_SERVER_ERROR, errorCallback).send();
  }

  private void deleteTransientDatasource() {
    if(Strings.isNullOrEmpty(transientDatasourceName)) return;

    UriBuilder builder = UriBuilder.create().segment("datasource", transientDatasourceName);
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(builder.build()) //
        .withCallback(SC_OK, ResponseCodeCallback.NO_OP).delete().send();
    transientDatasourceName = null;
  }

  private DatasourceFactoryDto createDatasourceFactoryDto(String tmpFilePath) {
    DatasourceFileType type = DatasourceFileType.getFileType(tmpFilePath);

    switch(type) {
      case XLS:
      case XLSX:
        return createExcelDatasourceFactoryDto(tmpFilePath);
    }

    return createStaticDatasourceFactoryDto(tmpFilePath);
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

  private final class ViewImportValidator extends ViewValidationHandler {
    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new ImportableValidator());
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(null, message);
    }
  }

  private final class ViewValidator extends ViewValidationHandler {
    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      DatasourceFileType fileType = DatasourceFileType.getFileType(getView().getSelectedFile());
      validators.add(new FileTypeValidator(fileType, Display.FormField.FILE_SELECTION.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  private final class ImportableValidator extends AbstractFieldValidator {

    ImportableValidator() {
      super("");
    }

    @Override
    protected boolean hasError() {
      if(comparedDatasourcesReportPresenter.getSelectedTables().isEmpty()) {
        setErrorMessageKey("TableSelectionIsRequired");
        return true;
      }
      if(!comparedDatasourcesReportPresenter.canBeSubmitted()) {
        setErrorMessageKey("NotIgnoredConflicts");
        return true;
      }

      conclusionPresenter.clearResourceRequests();
      comparedDatasourcesReportPresenter.addUpdateVariablesResourceRequests(conclusionPresenter);
      if(conclusionPresenter.getResourceRequestCount() == 0) {
        setErrorMessageKey("NoVariablesToBeImported");
        return true;
      }

      return false;
    }
  }

  private final static class FileTypeValidator extends AbstractFieldValidator {

    private final DatasourceFileType fileType;

    FileTypeValidator(DatasourceFileType fileType, String id) {
      super("InvalidFileType", id);
      this.fileType = fileType;
    }

    @Override
    protected boolean hasError() {
      return DatasourceFileType.INVALID == fileType;
    }
  }

  private final class TransientDatasourceSuccessCallback implements ResourceCallback<DatasourceDto> {

    private final DatasourceFactoryDto factory;

    TransientDatasourceSuccessCallback(DatasourceFactoryDto factory) {
      this.factory = factory;
    }

    @Override
    public void onResource(Response response, DatasourceDto resource) {
      if(response.getStatusCode() == SC_CREATED) {
        comparedDatasourcesReportPresenter
            .compare(resource.getName(), datasourceName, new DatasourceComparisonSuccessCallback(), factory, resource, getView().withMerge());
      }
    }

    private final class DatasourceComparisonSuccessCallback implements DatasourceCreatedCallback {

      @Override
      public void onSuccess(DatasourceFactoryDto factoryDto, DatasourceDto datasource) {
        transientDatasourceName = datasource.getName();
        getView().enableCompletion();
      }

      @Override
      public void onFailure(DatasourceFactoryDto factoryDto, ClientErrorDto errorDto) {
        // show client error
        Collection<String> errors = DatasourceParsingErrorDtos.getErrors(errorDto);
        for(String error : errors) {
          getView().showError(null, error);
        }
        getView().disableCompletion();
      }
    }

  }

  private final class TransientDatasourceFailureCallback implements ResponseCodeCallback {

    private String datasourceName;

    TransientDatasourceFailureCallback(String datasourceName) {
      this.datasourceName = datasourceName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onResponseCode(Request request, Response response) {
      ClientErrorDto error = JsonUtils.unsafeEval(response.getText());

      if(error.getExtension(DatasourceParsingErrorDto.ClientErrorDtoExtensions.errors) != null) {
        JsArray<DatasourceParsingErrorDto> parsingErrors = (JsArray<DatasourceParsingErrorDto>) error
            .getExtension(DatasourceParsingErrorDto.ClientErrorDtoExtensions.errors);
        short count = 0;
        for(DatasourceParsingErrorDto datasourceParsingErrorDto : JsArrays.toIterable(parsingErrors)) {
          int actualErrors = parsingErrors.length();
          getView().showError(null, TranslationsUtils
              .replaceArguments(translations.datasourceParsingErrorMap().get(datasourceParsingErrorDto.getKey()),
                  datasourceParsingErrorDto.getArgumentsArray()));

          if(++count >= MAX_ERROR_ALERTS && actualErrors != MAX_ERROR_ALERTS) {
            String resource = UriBuilders.PROJECT_TRANSIENT_DATASOURCE.create()//
                .segment("_last-errors")//
                .build(datasourceName);
            String downloadUrl = ResourceRequestBuilderFactory.newBuilder()//
                .forResource(resource) //
                .getUri();
            getView().showError(null, translationMessages.errorsRemainingMessage(
                actualErrors - MAX_ERROR_ALERTS,
                downloadUrl
            ));
            break;
          }
        }
      } else {
        getView().showError(null, translations.variableImportFailed());
      }

      getView().disableCompletion();
    }
  }

  public interface Display extends WizardView, CharacterSetDisplay, HasUiHandlers<VariablesImportUiHandlers> {

    enum FormField {
      FILE_SELECTION,
      LOCALE,
      CHARSET
    }

    void gotoPreview();

    void enableCompletion();

    void disableCompletion();

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);

    void setComparedDatasourcesReportDisplay(ComparedDatasourcesReportStepPresenter.Display display);

    String getSelectedFile();

    void clearErrors();

    void hideErrors();

    void showError(@Nullable FormField formField, String message);

    boolean withMerge();

    String getLocale();

    void setConclusionDisplay(ConclusionStepPresenter.Display display);

  }

}
