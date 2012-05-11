/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.presenter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.DisallowedCharactersValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.MatchingTableEntitiesValidator;
import org.obiba.opal.web.gwt.app.client.validator.MinimumSizeCollectionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredOptionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.FileViewDto;
import org.obiba.opal.web.model.client.magma.FileViewDto.FileViewType;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreateViewStepPresenter extends WizardPresenterWidget<CreateViewStepPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  public static class Wizard extends WizardProxy<CreateViewStepPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<CreateViewStepPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  private final TableListPresenter tableListPresenter;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  private DatasourceDto datasourceDto;

  private Runnable overwriteConfirmation;

  //
  // Constructors
  //

  @Inject
  public CreateViewStepPresenter(final Display display, final EventBus eventBus, TableListPresenter tableListPresenter,
      FileSelectionPresenter fileSelectionPresenter) {
    super(eventBus, display);
    this.tableListPresenter = tableListPresenter;
    this.fileSelectionPresenter = fileSelectionPresenter;
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    super.onBind();
    tableListPresenter.bind();
    getView().setTableSelector(tableListPresenter.getDisplay());

    fileSelectionPresenter.setFileSelectionType(FileSelectionType.EXISTING_FILE);
    fileSelectionPresenter.bind();
    getView().setFileSelectionDisplay(fileSelectionPresenter.getDisplay());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    tableListPresenter.unbind();
    fileSelectionPresenter.unbind();
  }

  protected void addEventHandlers() {
    registerHandler(getView().addConfigureHandler(new ConfigureHandler()));
    getView().setTablesValidator(new TablesValidator());
    getView().setSelectTypeValidator(new SelectTypeValidator());
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
  }

  @Override
  public void onReveal() {
    super.onReveal();
    tableListPresenter.getTables().clear();

    getView().clear();
  }

  @Override
  protected void onFinish() {
    super.onFinish();
    createViewIfDoesNotExist();
  }

  @Override
  protected void onClose() {
    super.onClose();
    getEventBus().fireEvent(new DatasourceUpdatedEvent(datasourceDto));
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 0) {
      if(event.getEventParameters()[0] instanceof String) {
        datasourceName = (String) event.getEventParameters()[0];
        UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName);
        ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource(ub.build()).get()
            .withCallback(new ResourceCallback<DatasourceDto>() {

              @Override
              public void onResource(Response response, DatasourceDto resource) {
                datasourceDto = resource;
              }
            }).send();
      } else {
        throw new IllegalArgumentException("unexpected event parameter type (expected String)");
      }
    } else {
      throw new IllegalArgumentException("Datasource name is expected as first wizard argument.");
    }
  }

  private void createViewIfDoesNotExist() {
    // Get the view name and datasource name.
    String viewName = getView().getViewName().getText();

    ViewFoundCallback overwrite = new ViewFoundCallback();
    ViewNotFoundCreateCallback create = new ViewNotFoundCreateCallback();

    // Create the resource request (the builder).
    getViewRequest(datasourceName, viewName).withCallback(Response.SC_OK, overwrite)//
        .withCallback(Response.SC_NOT_FOUND, create).send();
  }

  private ResourceRequestBuilder<ViewDto> getViewRequest(String datasourceName, String viewName) {
    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName, "view", viewName);
    return ResourceRequestBuilderFactory.<ViewDto> newBuilder().get().forResource(ub.build())
        .accept("application/x-protobuf+json");
  }

  private void createView() {
    getView().renderPendingConclusion();

    CompletedCallback completed = new CompletedCallback();
    FailedCallback failed = new FailedCallback();

    String viewName = getView().getViewName().getText();

    // Build the ViewDto for the request.
    ViewDtoBuilder viewDtoBuilder =
        ViewDtoBuilder.newBuilder().setName(viewName).fromTables(tableListPresenter.getTables());

    // Create the resource request (the builder).
    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName, "views");
    ResourceRequestBuilder<JavaScriptObject> resourceRequestBuilder = ResourceRequestBuilderFactory.newBuilder()//
        .post()//
        .forResource(ub.build())//
        .withResourceBody(ViewDto.stringify(createViewDto(viewDtoBuilder)))//
        .withCallback(Response.SC_CREATED, completed)//
        .withCallback(Response.SC_OK, completed)//
        .withCallback(Response.SC_BAD_REQUEST, failed)//
        .withCallback(Response.SC_NOT_FOUND, failed)//
        .withCallback(Response.SC_FORBIDDEN, failed)//
        .withCallback(Response.SC_METHOD_NOT_ALLOWED, failed)//
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, failed);

    resourceRequestBuilder.send();
  }

  private ViewDto createViewDto(ViewDtoBuilder viewDtoBuilder) {
    // Get the view name and datasource name.
    if(getView().getAddVariablesOneByOneOption().getValue()) {
      viewDtoBuilder.defaultVariableListView();
    } else if(getView().getFileViewOption().getValue() || getView().getExcelFileOption().getValue()) {
      FileViewDto fileView = FileViewDto.create();
      fileView.setFilename(fileSelectionPresenter.getSelectedFile());
      if(getView().getFileViewOption().getValue()) {
        fileView.setType(FileViewType.SERIALIZED_XML);
      } else if(getView().getExcelFileOption().getValue()) {
        fileView.setType(FileViewType.EXCEL);
      }
      viewDtoBuilder.fileView(fileView);
    }
    return viewDtoBuilder.build();
  }

  private void updateView() {
    getView().renderPendingConclusion();

    CompletedCallback completed = new CompletedCallback();
    FailedCallback failed = new FailedCallback();

    String viewName = getView().getViewName().getText();

    // Build the ViewDto for the request.
    ViewDtoBuilder viewDtoBuilder =
        ViewDtoBuilder.newBuilder().setName(viewName).fromTables(tableListPresenter.getTables());

    // Create the resource request (the builder).
    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName, "view", viewName);
    ResourceRequestBuilder<JavaScriptObject> resourceRequestBuilder = ResourceRequestBuilderFactory.newBuilder()//
        .put()//
        .forResource(ub.build())//
        .withResourceBody(ViewDto.stringify(createViewDto(viewDtoBuilder)))//
        .withCallback(Response.SC_CREATED, completed)//
        .withCallback(Response.SC_OK, completed)//
        .withCallback(Response.SC_BAD_REQUEST, failed)//
        .withCallback(Response.SC_NOT_FOUND, failed)//
        .withCallback(Response.SC_FORBIDDEN, failed)//
        .withCallback(Response.SC_METHOD_NOT_ALLOWED, failed)//
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, failed);

    resourceRequestBuilder.send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WizardView {

    void clear();

    void setTableSelector(TableListPresenter.Display tableSelector);

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);

    HasText getViewName();

    HasValue<Boolean> getAddVariablesOneByOneOption();

    HasValue<Boolean> getFileViewOption();

    HasValue<Boolean> getExcelFileOption();

    void setSelectTypeValidator(ValidationHandler validator);

    void setTablesValidator(ValidationHandler validator);

    void renderPendingConclusion();

    void renderFailedConclusion(String msg);

    void renderCompletedConclusion();

    HandlerRegistration addConfigureHandler(ClickHandler handler);

  }

  final class ConfigureHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent evt) {
      getEventBus().fireEvent(new DatasourceUpdatedEvent(datasourceDto));

      // Get the new view dto
      getViewRequest(datasourceName, getView().getViewName().getText())//
          .withCallback(new ResourceCallback<ViewDto>() {

            @Override
            public void onResource(Response response, ViewDto resource) {
              getView().hide();
              getEventBus().fireEvent(new ViewConfigurationRequiredEvent(resource));
            }
          }).send();

    }
  }

  private class FailedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String msg = "UnknownError";
      if(response.getText() != null && response.getText().length() != 0) {
        try {
          ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          msg = errorDto.getStatus();
        } catch(Exception e) {

        }
      }
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(msg).build());
      getView().renderFailedConclusion(msg);
    }
  }

  private class CompletedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getView().renderCompletedConclusion();
    }
  }

  class SelectTypeValidator extends AbstractValidationHandler {

    public SelectTypeValidator() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      validators.add(new RequiredTextValidator(getView().getViewName(), "ViewNameRequired"));
      validators.add(new DisallowedCharactersValidator(getView().getViewName(), new char[] { '.', ':' },
          "ViewNameDisallowedChars"));
      validators.add(new RequiredOptionValidator(RequiredOptionValidator.asSet(getView()
          .getAddVariablesOneByOneOption(), getView().getFileViewOption(), getView().getExcelFileOption()),
          "VariableDefinitionMethodRequired"));
      validators.add(new ConditionalValidator(getView().getFileViewOption(), new RequiredFileSelectionValidator(
          "XMLFileRequired")));
      validators.add(new ConditionalValidator(getView().getExcelFileOption(), new RequiredFileSelectionValidator(
          "ExcelFileRequired")));
      return validators;
    }
  }

  class TablesValidator extends AbstractValidationHandler {

    public TablesValidator() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      HasCollection<TableDto> tablesField = new HasCollection<TableDto>() {
        public Collection<TableDto> getCollection() {
          return tableListPresenter.getTables();
        }
      };
      validators.add(new MinimumSizeCollectionValidator<TableDto>(tablesField, 1, "TableSelectionRequired"));
      validators.add(new MatchingTableEntitiesValidator(tablesField));
      return validators;
    }

  }

  class ViewFoundCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      // getEventBus().fireEvent(NotificationEvent.newBuilder().error("ViewAlreadyExists").build());
      overwriteConfirmation = new Runnable() {
        @Override
        public void run() {
          updateView();
        }
      };
      getEventBus().fireEvent(
          ConfirmationRequiredEvent.createWithKeys(overwriteConfirmation, "overwriteView", "confirmOverwriteView"));
    }
  }

  private class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @SuppressWarnings("AssignmentToNull")
    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(overwriteConfirmation != null && event.getSource().equals(overwriteConfirmation) && event.isConfirmed()) {
        overwriteConfirmation.run();
        overwriteConfirmation = null;
      }
    }
  }

  class ViewNotFoundCreateCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      createView();
    }
  }

  class RequiredFileSelectionValidator extends AbstractFieldValidator {

    public RequiredFileSelectionValidator(String msg) {
      super(msg);
    }

    @Override
    protected boolean hasError() {
      return fileSelectionPresenter.getSelectedFile() == null || fileSelectionPresenter.getSelectedFile().isEmpty();
    }

  }
}
