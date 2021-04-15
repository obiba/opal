/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Lists;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.support.ViewDtoBuilder;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredFileSelectionValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.FileViewDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import static com.google.gwt.http.client.Response.SC_OK;

public class AddVariablesModalPresenter extends ModalPresenterWidget<AddVariablesModalPresenter.Display>
    implements AddVariablesModalUiHandlers {

  private final FileSelectionPresenter fileSelectionPresenter;

  private static final String EXCEL_TEMPLATE = "/opalVariableTemplate.xls";

  private final ViewValidator validator;

  private final PlaceManager placeManager;

  private TableDto tableDto;

  @Inject
  AddVariablesModalPresenter(EventBus eventBus, Display view, FileSelectionPresenter fileSelection, PlaceManager placeManager) {
    super(eventBus, view);
    fileSelectionPresenter = fileSelection;
    getView().setUiHandlers(this);
    this.placeManager = placeManager;
    validator = new ViewValidator();
  }

  public void initialize(TableDto dto) {
    tableDto = dto;
  }

  @Override
  protected void onBind() {
    super.onBind();
    fileSelectionPresenter.setFileSelectionType(FileSelectorPresenter.FileSelectionType.FILE);
    fileSelectionPresenter.bind();
    getView().setFileSelectionDisplay(fileSelectionPresenter.getView());
  }

  @Override
  public void save() {
    assert tableDto.hasViewLink();
    assert tableDto != null;
    getView().clearErrors();
    if(!validator.validate()) return;

    UriBuilder uriBuilder = UriBuilders.DATASOURCE_VIEW_VARIABLES_FILE.create();

    ResourceRequestBuilderFactory.newBuilder()
        .forResource(uriBuilder.build(tableDto.getDatasourceName(), tableDto.getName()))//
        .withResourceBody(ViewDto.stringify(createViewDto()))//
        .withCallback(SC_OK, new VariablesCreatedCallback())//
        .post()//
        .send();
  }

  @Override
  public void downloadTemplate() {
    fireEvent(new FileDownloadRequestEvent("/templates" + EXCEL_TEMPLATE));
  }

  private ViewDto createViewDto() {
    ViewDtoBuilder builder = ViewDtoBuilder.newBuilder();
    String fileName = fileSelectionPresenter.getSelectedFile();
    builder.setName(tableDto.getName() + "-variables-source");
    builder.fromTables(Lists.newArrayList(tableDto));
    FileViewDto fileView = FileViewDto.create();
    fileView.setFilename(fileName);
    fileView.setType(getFileType(fileName));
    builder.fileView(fileView);

    return builder.build();
  }

  private FileViewDto.FileViewType getFileType(String fileName) {
    RegExp regExp = RegExp.compile("\\.xml$");
    if(regExp.test(fileName.toLowerCase())) {
      return FileViewDto.FileViewType.SERIALIZED_XML;
    }

    return FileViewDto.FileViewType.EXCEL;
  }

  public interface Display extends PopupView, HasUiHandlers<AddVariablesModalUiHandlers> {
    enum FormField {
      FILE_SELECTION
    }

    void closeDialog();

    void clearErrors();

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);

    void showError(FormField fileSelection, String message);
  }

  private final class ViewValidator extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<>();
      validators.add(
          new RequiredFileSelectionValidator("\\.(xml|xls|xlsx)$", fileSelectionPresenter.getView().getFileText(),
              "XMLOrExcelFileRequired", Display.FormField.FILE_SELECTION.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.FILE_SELECTION, message);
    }
  }

  private class VariablesCreatedCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == SC_OK) {
        getView().closeDialog();
        placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(tableDto.getDatasourceName(), tableDto.getName()));
      }
    }
  }
}
