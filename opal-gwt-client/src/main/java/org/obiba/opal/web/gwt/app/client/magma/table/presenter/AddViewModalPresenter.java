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
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.validator.DisallowedCharactersValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.MatchingTableEntitiesValidator;
import org.obiba.opal.web.gwt.app.client.validator.MinimumSizeCollectionValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredFileSelectionValidator;
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

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_METHOD_NOT_ALLOWED;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

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

    if(!viewValidator.validate()) return;

    String viewName = getView().getViewName().getText();

    ResponseCodeCallback completed = new CompletedCallback(viewName);
    ResponseCodeCallback failed = new FailedCallback();

    // Build the ViewDto for the request.
    ViewDtoBuilder viewDtoBuilder = ViewDtoBuilder.newBuilder().setName(viewName)
        .fromTables(getView().getSelectedTables());

    // Create the resource request (the builder).
    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName, "views");
    ResourceRequestBuilder<JavaScriptObject> resourceRequestBuilder = ResourceRequestBuilderFactory.newBuilder()//
        .post()//
        .forResource(ub.build())//
        .withResourceBody(ViewDto.stringify(createViewDto(viewDtoBuilder)))//
        .withCallback(completed, SC_CREATED, SC_OK)//
        .withCallback(failed, SC_BAD_REQUEST, SC_NOT_FOUND, SC_FORBIDDEN, SC_METHOD_NOT_ALLOWED,
            SC_INTERNAL_SERVER_ERROR);//

    resourceRequestBuilder.send();
  }

  private ViewDto createViewDto(ViewDtoBuilder viewDtoBuilder) {
    String fileName = fileSelectionPresenter.getSelectedFile();
    if(Strings.isNullOrEmpty(fileName)) {
      viewDtoBuilder.defaultVariableListView();
    } else {
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

    private final String viewName;

    private CompletedCallback(String viewName) {
      this.viewName = viewName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(datasourceDto.getName(), viewName));
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
          getView().showError(null, errorDto.getStatus() +
                  Modal.alertDebugInfo(errorDto.getArgumentsArray()));
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
      Set<FieldValidator> validators = new LinkedHashSet<>();
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
      validators.add(
          new RequiredTextValidator(getView().getViewName(), "ViewNameRequired", Display.FormField.VIEW_NAME.name()));
      validators.add(
          new DisallowedCharactersValidator(getView().getViewName(), new char[] { '.', ':' }, "ViewNameDisallowedChars",
              Display.FormField.VIEW_NAME.name()));
    }

    private void addTablesValidators(Collection<FieldValidator> validators) {
      HasCollection<TableDto> tablesField = new HasCollection<TableDto>() {
        @Override
        public Collection<TableDto> getCollection() {
          return getView().getSelectedTables();
        }
      };
      validators.add(new MinimumSizeCollectionValidator<>(tablesField, 1, "TableSelectionRequired",
          Display.FormField.TABLES.name()));
      validators.add(new MatchingTableEntitiesValidator(tablesField, Display.FormField.TABLES.name()));
    }

    private void addFileSelectionValidators(Collection<FieldValidator> validators) {
      validators.add(
          new RequiredFileSelectionValidator("\\.(xml|xls|xlsx)$", fileSelectionPresenter.getView().getFileText(),
              "XMLOrExcelFileRequired", Display.FormField.FILE_SELECTION.name()));
    }

  }
}
