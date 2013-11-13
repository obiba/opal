/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.ModalClosedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.support.DatasourceFileType;
import org.obiba.opal.web.gwt.app.client.presenter.CharacterSetDisplay;
import org.obiba.opal.web.gwt.app.client.support.DatasourceParsingErrorDtos;
import org.obiba.opal.web.gwt.app.client.support.LanguageLocale;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.magma.ExcelDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.FileViewDto;
import org.obiba.opal.web.model.client.magma.FileViewDto.FileViewType;
import org.obiba.opal.web.model.client.magma.SpssDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.StaticDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

public class VariablesImportPresenter extends WizardPresenterWidget<VariablesImportPresenter.Display>
    implements VariablesImportUiHandlers {

  public static final WizardType WIZARD_TYPE = new WizardType();

  private ViewValidator viewValidaror;

  public static class Wizard extends WizardProxy<VariablesImportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<VariablesImportPresenter> wizardProvider) {
      super(eventBus, WIZARD_TYPE, wizardProvider);
    }

  }

  private static final String EXCEL_TEMPLATE = "/opalVariableTemplate.xls";

  private final ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter;

  private final ConclusionStepPresenter conclusionPresenter;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public VariablesImportPresenter(Display display, EventBus eventBus,
      ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter,
      ConclusionStepPresenter conclusionPresenter, FileSelectionPresenter fileSelectionPresenter) {
    super(eventBus, display);
    this.comparedDatasourcesReportPresenter = comparedDatasourcesReportPresenter;
    this.conclusionPresenter = conclusionPresenter;
    this.fileSelectionPresenter = fileSelectionPresenter;
    init();
  }

  private void init() {
    getView().setUiHandlers(this);
    viewValidaror = new ViewValidator();
    setDefaultCharset();

    getEventBus().addHandler(FileSelectionUpdatedEvent.getType(), new FileSelectionUpdatedEvent.Handler() {
      @Override
      public void onFileSelectionUpdated(FileSelectionUpdatedEvent event) {
        String selectedFile = ((FileSelectionPresenter) event.getSource()).getSelectedFile();
        getView().showSpssSpecificPanel(DatasourceFileType.isSpssFile(selectedFile));
      }
    });
  }

  @Override
  public void onModalHidden() {
    getEventBus().fireEventFromSource(new ModalClosedEvent(this), this);
  }

  @Override
  public void selectVariableFile() {
    getView().clearErrors();
    if (!viewValidaror.validate()) return;
    createTransientDatasource();
    getView().gotoPreview();
  }

  @Override
  public void downExcelTemplate() {
    getEventBus().fireEvent(new FileDownloadRequestEvent("/templates" + EXCEL_TEMPLATE));
  }

  @Override
  protected void onBind() {
    super.onBind();
    comparedDatasourcesReportPresenter.bind();
    getView().setComparedDatasourcesReportDisplay(comparedDatasourcesReportPresenter.getView());

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
    fileSelectionPresenter.bind();
    getView().setFileSelectionDisplay(fileSelectionPresenter.getView());

    conclusionPresenter.bind();
    getView().setConclusionDisplay(conclusionPresenter.getView());
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    datasourceName = null;
  }

  protected void addEventHandlers() {
    getView().setImportableValidator(new ImportableValidator());
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length > 0) {
      datasourceName = (String) event.getEventParameters()[0];
    }
  }

  @Override
  protected void onFinish() {
    super.onFinish();
    conclusionPresenter.sendResourceRequests();
  }

  private void setDefaultCharset() {
    ResourceRequestBuilderFactory.<JsArrayString>newBuilder().forResource("/files/charsets/default").get()
        .withCallback(new ResourceCallback<JsArrayString>() {

          @Override
          public void onResource(Response response, JsArrayString resource) {
            String charset = resource.get(0);
            getView().setDefaultCharset(charset);
          }
        }).send();
  }

  private final class ImportableValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(comparedDatasourcesReportPresenter.getSelectedTables().isEmpty()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("TableSelectionIsRequired").build());
        return false;
      }
      if(!comparedDatasourcesReportPresenter.canBeSubmitted()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("NotIgnoredConflicts").build());
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

  private final class FileSelectionValidator extends AbstractFieldValidator {

    FileSelectionValidator(String id) {
      super("InvalidFileType", id);
    }

    @Override
    protected boolean hasError() {
      return DatasourceFileType.INVALID == DatasourceFileType.getFileType(getView().getSelectedFile());
    }
  }

  private final class LocaleValidator extends AbstractFieldValidator {

    LocaleValidator(String id) {
      super("InvalidLocaleName", id);
    }

    @Override
    public boolean hasError() {
      String localeName = getView().getLocale();
      setArgs(Arrays.asList(localeName));
      return !LanguageLocale.isValid(localeName);
    }
  }

  public interface Display extends WizardView, CharacterSetDisplay, HasUiHandlers<VariablesImportUiHandlers> {


    enum FormField {
      FIEL_SELECTION,
      LOCALE
    }

    void gotoPreview();

    void enableCompletion();

    void disableCompletion();

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);

    void setImportableValidator(ValidationHandler handler);

    void setComparedDatasourcesReportDisplay(ComparedDatasourcesReportStepPresenter.Display display);

    void showSpssSpecificPanel(boolean show);

    HasText getSelectedFileText();

    String getSelectedFile();

    void clearErrors();

    void hideErrors();

    void showError(@Nullable FormField formField, String message);

    HasText getSpssEntityType();

    String getLocale();

    void setConclusionDisplay(ConclusionStepPresenter.Display display);

  }

  class ViewValidator extends ViewValidationHandler {
    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      validators.add(new LocaleValidator(Display.FormField.LOCALE.name()));

      validators.add(new RequiredTextValidator(getView().getSelectedFileText(), "ViewNameRequired",
          Display.FormField.FIEL_SELECTION.name()));
      validators.add(new FileSelectionValidator(Display.FormField.FIEL_SELECTION.name()));


      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  private void createTransientDatasource() {
    final DatasourceFactoryDto factory = createDatasourceFactoryDto(getView().getSelectedFile());
    ResponseCodeCallback errorCallback = new TransientDatasourceFailureCallback(factory);

    ResourceRequestBuilderFactory.<DatasourceDto>newBuilder() //
        .forResource("/transient-datasources") //
        .post() //
        .withResourceBody(DatasourceFactoryDto.stringify(factory)) //
        .withCallback(new TransientDatasourceSuccessCallback(factory)) //
        .withCallback(SC_BAD_REQUEST, errorCallback) //
        .withCallback(SC_INTERNAL_SERVER_ERROR, errorCallback).send();
  }

  class TransientDatasourceSuccessCallback implements ResourceCallback<DatasourceDto> {

    private final DatasourceFactoryDto factory;

    TransientDatasourceSuccessCallback(DatasourceFactoryDto factory) {
      this.factory = factory;
    }

    @Override
    public void onResource(Response response, DatasourceDto resource) {
      if(response.getStatusCode() == SC_CREATED) {
        comparedDatasourcesReportPresenter.compare(resource.getName(), datasourceName,
            new DatasourceComparisonSuccessCallback(), factory, resource);
      }
    }

    class DatasourceComparisonSuccessCallback implements DatasourceCreatedCallback {

      @Override
      public void onSuccess(DatasourceFactoryDto factory, DatasourceDto datasource) {
        getView().enableCompletion();
      }

      @Override
      public void onFailure(DatasourceFactoryDto factory, ClientErrorDto errorDto) {
        // show client error
        Collection<String> errors = DatasourceParsingErrorDtos.getErrors(errorDto);
        for (String error : errors) {
          getView().showError(null, error);
        }
        getView().disableCompletion();
      }
    }

  }

  class TransientDatasourceFailureCallback implements ResponseCodeCallback {
    private final DatasourceFactoryDto factory;

    TransientDatasourceFailureCallback(DatasourceFactoryDto factory) {
      this.factory = factory;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());

      Collection<String> errors = DatasourceParsingErrorDtos.getErrors(errorDto);
      for (String error : errors) {
        getView().showError(null, error);
      }
      getView().disableCompletion();
    }
  };

  private DatasourceFactoryDto createDatasourceFactoryDto(String tmpFilePath) {
    DatasourceFileType type = DatasourceFileType.getFileType(tmpFilePath);

    switch(type) {
      case XLS:
      case XLSX:
        return createExcelDatasourceFactoryDto(tmpFilePath);

      case SAV:
        return createSpssDatasourceFactoryDto(tmpFilePath);
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

  private DatasourceFactoryDto createSpssDatasourceFactoryDto(String tmpFilePath) {
    SpssDatasourceFactoryDto spssDto = SpssDatasourceFactoryDto.create();
    spssDto.setFile(tmpFilePath);
    spssDto.setCharacterSet(getView().getCharsetText().getText());
    spssDto.setEntityType(getView().getSpssEntityType().getText());
    spssDto.setLocale(getView().getLocale());

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(SpssDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, spssDto);

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
