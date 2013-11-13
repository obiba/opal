/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.table.presenter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.DisallowedCharactersValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.MatchingTableEntitiesValidator;
import org.obiba.opal.web.gwt.app.client.validator.MinimumSizeCollectionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
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

public class AddViewModalPresenter extends ModalPresenterWidget<AddViewModalPresenter.Display>
    implements AddViewModalUiHandlers {

  private final PlaceManager placeManager;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  private DatasourceDto datasourceDto;

  private ViewValidationHandler viewValidator;

  //
  // Constructors
  //

  @Inject
  public AddViewModalPresenter(Display display, EventBus eventBus, FileSelectionPresenter fileSelectionPresenter,
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
    viewValidator = new ViewValidator();
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

  @Override
  public void createView() {
    getView().clearErrors();

    if (!viewValidator.validate()) return;

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

  private ViewDto createViewDto(ViewDtoBuilder viewDtoBuilder) {
    String fileName = fileSelectionPresenter.getSelectedFile();
    if(Strings.isNullOrEmpty(fileName)) {
      viewDtoBuilder.defaultVariableListView();
    }
    else {
      FileViewDto fileView = FileViewDto.create();
      fileView.setFilename(fileName);
      fileView.setType(getFileType(fileName));
      viewDtoBuilder.fileView(fileView);
    }

    return viewDtoBuilder.build();
  }

  private FileViewDto.FileViewType getFileType(String fileName) {
    RegExp regExp = RegExp.compile("\\.xml$");
    if(regExp.test(fileName.toLowerCase())) {
      return FileViewDto.FileViewType.SERIALIZED_XML;
    }

    return FileViewDto.FileViewType.EXCEL;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends PopupView, HasUiHandlers<AddViewModalUiHandlers> {

    enum FormField {
      VIEW_NAME,
      TABLES,
      FILE_SELECTION
    }

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);

    HasText getViewName();

    void addTableSelections(JsArray<TableDto> tables);

    List<TableDto> getSelectedTables();

    void closeDialog();

    void clearErrors();

    void showError(@Nullable FormField formField, String message);

    void showError(String messageKey);
  }

  private class CompletedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      placeManager.revealPlace(ProjectPlacesHelper.getDatasourcePlace(datasourceDto.getName()));
      getView().closeDialog();
    }
  }

  private class FailedCallback implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getView().clearErrors();

      if(response.getText() != null && response.getText().length() != 0) {
        try {
          ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
          getView().showError(null, errorDto.getStatus());
          return;
        } catch(Exception ignored) {
        }
      }

      getView().showError("CreateViewFailed");
    }
  }

  class ViewValidator extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      addViewNameValidators(validators);
      addTablesValidators(validators);
      addFileSelectionValidators(validators);
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }

    private void addViewNameValidators(Collection<FieldValidator> validators) {
      validators.add(new RequiredTextValidator(getView().getViewName(), "ViewNameRequired", Display.FormField.VIEW_NAME.name()));
      validators.add(new DisallowedCharactersValidator(getView().getViewName(), new char[] { '.', ':' },
          "ViewNameDisallowedChars", Display.FormField.VIEW_NAME.name()));
    }

    private void addTablesValidators(Collection<FieldValidator> validators) {
      HasCollection<TableDto> tablesField = new HasCollection<TableDto>() {
        @Override
        public Collection<TableDto> getCollection() {
          return getView().getSelectedTables();
        }
      };
      validators.add(new MinimumSizeCollectionValidator<TableDto>(tablesField, 1, "TableSelectionRequired",
          Display.FormField.TABLES.name()));
      validators.add(new MatchingTableEntitiesValidator(tablesField, Display.FormField.TABLES.name()));
    }

    private void addFileSelectionValidators(Collection<FieldValidator> validators) {
      validators.add(new RequiredFileSelectionValidator(Display.FormField.FILE_SELECTION.name()));
    }

  }

  class RequiredFileSelectionValidator extends AbstractFieldValidator {

    private static final String EXTENSION_PATTERN = "\\.(xml|xls|xlsx)$";

    RequiredFileSelectionValidator(String id) {
      super("XMLOrExcelFileRequired", id);
    }

    @Override
    protected boolean hasError() {
      String fileName = fileSelectionPresenter.getSelectedFile().toLowerCase();
      return !Strings.isNullOrEmpty(fileName) && !RegExp.compile(EXTENSION_PATTERN).test(fileName);
    }

  }
}
