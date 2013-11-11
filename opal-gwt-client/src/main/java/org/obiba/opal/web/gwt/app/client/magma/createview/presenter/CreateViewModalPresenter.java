/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.createview.presenter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.DisallowedCharactersValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.MatchingTableEntitiesValidator;
import org.obiba.opal.web.gwt.app.client.validator.MinimumSizeCollectionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.FileViewDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class CreateViewModalPresenter extends ModalPresenterWidget<CreateViewModalPresenter.Display>
    implements CreateViewModalUiHandlers {

  private final PlaceManager placeManager;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  private DatasourceDto datasourceDto;

  private Runnable overwriteConfirmation;

  //
  // Constructors
  //

  @Inject
  public CreateViewModalPresenter(Display display, EventBus eventBus, FileSelectionPresenter fileSelectionPresenter,
      PlaceManager placeManager) {
    super(eventBus, display);
    this.fileSelectionPresenter = fileSelectionPresenter;
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
    fileSelectionPresenter.bind();
    getView().setFileSelectionDisplay(fileSelectionPresenter.getView());

    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    fileSelectionPresenter.unbind();
  }

  public void setDatasourceName(String datasourceName) {
    if(Strings.isNullOrEmpty(datasourceName)) {
      throw new IllegalArgumentException("Datasource name is expected as first wizard argument.");
    }

    this.datasourceName = datasourceName;

    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName);
    ResourceRequestBuilderFactory.<DatasourceDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResourceCallback<DatasourceDto>() {

          @Override
          public void onResource(Response response, DatasourceDto resource) {
            datasourceDto = resource;
          }
        }).send();

    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource("/datasources/tables").get()
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            getView().addTableSelections(JsArrays.toSafeArray(resource));
          }

        }).send();
  }

  protected void addEventHandlers() {
    getView().setTablesValidator(new TablesValidator());
    getView().setSelectTypeValidator(new SelectTypeValidator());
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
  }

  @Override
  public void createView() {
    if (!validate()) return;

    ResponseCodeCallback completed = new CompletedCallback();
    ResponseCodeCallback failed = new FailedCallback();

    String viewName = getView().getViewName().getText();

    // Build the ViewDto for the request.
    ViewDtoBuilder viewDtoBuilder = ViewDtoBuilder.newBuilder().setName(viewName)
        .fromTables(getView().getSelectedTables());

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

  private boolean validate() {
    return new SelectTypeValidator().validate() && new TablesValidator().validate();
  }

  private ViewDto createViewDto(ViewDtoBuilder viewDtoBuilder) {
    String fileName = fileSelectionPresenter.getSelectedFile();
    if(!Strings.isNullOrEmpty(fileName)) {
      FileViewDto fileView = FileViewDto.create();
      fileView.setFilename(fileName);
      fileView.setType(getFileType(fileName));
      viewDtoBuilder.fileView(fileView);
    }
    else {
      viewDtoBuilder.defaultVariableListView();
    }

    return viewDtoBuilder.build();
  }

  private FileViewDto.FileViewType getFileType(String fileName) {
    if(fileName.toLowerCase().matches("\\.xml$")) {
      return FileViewDto.FileViewType.SERIALIZED_XML;
    }

    return FileViewDto.FileViewType.EXCEL;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends PopupView, HasUiHandlers<CreateViewModalUiHandlers> {

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);

    HasText getViewName();

    void setSelectTypeValidator(ValidationHandler validator);

    void setTablesValidator(ValidationHandler validator);

    void addTableSelections(JsArray<TableDto> tables);

    List<TableDto> getSelectedTables();

    void closeDialog();

  }

  private class CompletedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      placeManager.revealPlace(ProjectPlacesHelper.getDatasourcePlace(datasourceDto.getName()));
    }
  }

  private class FailedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String msg = "UnknownError";
      if(response.getText() != null && response.getText().length() != 0) {
        try {
          ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
          msg = errorDto.getStatus();
        } catch(Exception ignored) {

        }
      }
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(msg).build());
    }
  }

  class SelectTypeValidator extends AbstractValidationHandler {

    SelectTypeValidator() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      validators.add(new RequiredTextValidator(getView().getViewName(), "ViewNameRequired"));
      validators.add(new DisallowedCharactersValidator(getView().getViewName(), new char[] { '.', ':' },
          "ViewNameDisallowedChars"));
      validators.add(new RequiredFileSelectionValidator("XMLFileRequired"));
      return validators;
    }
  }

  class TablesValidator extends AbstractValidationHandler {

    TablesValidator() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      HasCollection<TableDto> tablesField = new HasCollection<TableDto>() {
        @Override
        public Collection<TableDto> getCollection() {
          return getView().getSelectedTables();
        }
      };
      validators.add(new MinimumSizeCollectionValidator<TableDto>(tablesField, 1, "TableSelectionRequired"));
      validators.add(new MatchingTableEntitiesValidator(tablesField));
      return validators;
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

  class RequiredFileSelectionValidator extends AbstractFieldValidator {

    private static final String EXTENSION_PATTERN = "\\.(xml|xls|xlsx)$";

    RequiredFileSelectionValidator(String msg) {
      super(msg);
    }

    @Override
    protected boolean hasError() {
      String fileName = fileSelectionPresenter.getSelectedFile();
      return !Strings.isNullOrEmpty(fileName) ? !RegExp.compile(EXTENSION_PATTERN).test(fileName) : false;
    }

  }
}
