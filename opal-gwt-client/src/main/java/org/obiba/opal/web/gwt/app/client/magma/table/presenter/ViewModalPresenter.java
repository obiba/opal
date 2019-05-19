/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.table.presenter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.gwt.core.client.*;
import com.google.gwt.regexp.shared.RegExp;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.ui.HasCollection;
import org.obiba.opal.web.gwt.app.client.validator.*;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.model.client.magma.*;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import static com.google.gwt.http.client.Response.*;

/**
 *
 */
public class ViewModalPresenter extends ModalPresenterWidget<ViewModalPresenter.Display>
    implements ViewModalUiHandlers {

  private final Translations translations;

  private final FileSelectionPresenter fileSelectionPresenter;

  private final PlaceManager placeManager;

  private ViewDto view;

  private String datasourceName;

  private final ValidationHandler validationHandler;

  @Inject
  public ViewModalPresenter(EventBus eventBus, Display display, FileSelectionPresenter fileSelectionPresenter, Translations translations,
                            PlaceManager placeManager) {
    super(eventBus, display);
    this.fileSelectionPresenter = fileSelectionPresenter;
    this.translations = translations;
    this.placeManager = placeManager;
    validationHandler = new PropertiesValidationHandler();
    getView().setUiHandlers(this);
  }

  /**
   * Will update the view table.
   *
   * @param view
   */

  public void initialize(ViewDto view) {
    this.view = view;
    getView().renderProperties(view);
    renderSelectableTables();
  }

  public void initialize(String datasourceName) {
    this.datasourceName = datasourceName;
    renderSelectableTables();
  }

  @Override
  protected void onBind() {
    super.onBind();
    fileSelectionPresenter.setFileSelectionType(FileSelectorPresenter.FileSelectionType.FILE);
    fileSelectionPresenter.bind();
    getView().setFileSelectionDisplay(fileSelectionPresenter.getView());
   }

  @Override
  public void onSave(final String name, List<TableDto> referencedTables, List<String> innerFrom) {
    if (!validationHandler.validate()) return;

    ViewDto dto = getViewDto(name, referencedTables, innerFrom);
    if (view == null) createView(dto);
    else updateView(dto);
  }

  private void updateView(ViewDto dto) {
    ResponseCodeCallback completed = new CompletedCallback(dto.getName());

    UriBuilder ub = UriBuilders.DATASOURCE_VIEW.create().query("comment", view.getName().equals(dto.getName())
        ? TranslationsUtils.replaceArguments(translations.updateComment(), dto.getName())
        : TranslationsUtils.replaceArguments(translations.renameToComment(), view.getName(), dto.getName()));

    ResourceRequestBuilderFactory.newBuilder().put().forResource(ub.build(getDatasourceName(), view.getName()))
        .withResourceBody(ViewDto.stringify(dto)).withCallback(completed, Response.SC_OK, Response.SC_BAD_REQUEST, Response.SC_FORBIDDEN).send();
  }

  private void createView(ViewDto dto) {
    ResponseCodeCallback completed = new CompletedCallback(dto.getName());

    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName, "views");

    ResourceRequestBuilderFactory.newBuilder()//
        .post()//
        .forResource(ub.build())//
        .withResourceBody(ViewDto.stringify(dto))//
        .withCallback(completed, SC_CREATED, SC_OK, SC_BAD_REQUEST, SC_NOT_FOUND, SC_FORBIDDEN, SC_METHOD_NOT_ALLOWED,
            SC_INTERNAL_SERVER_ERROR).send();//
  }

  private String getDatasourceName() {
    return view == null ? datasourceName : view.getDatasourceName();
  }

  private ViewDto getViewDto(String name, List<TableDto> referencedTables, List<String> innerFrom) {
    ViewDto updatedView = ViewDto.create();
    updatedView.setName(name);
    JsArrayString tables = JavaScriptObject.createArray().cast();
    for(TableDto tableDto : referencedTables) {
      tables.push(tableDto.getDatasourceName() + "." + tableDto.getName());
    }
    updatedView.setFromArray(tables);
    if (!innerFrom.isEmpty()) updatedView.setInnerFromArray(JsArrays.fromIterable(innerFrom));
    if(view != null && view.hasWhere()) updatedView.setWhere(view.getWhere());

    String fileName = fileSelectionPresenter.getSelectedFile();
    if(Strings.isNullOrEmpty(fileName)) {
      if (view == null) {
        VariableListViewDto listDto = VariableListViewDto.create();
        listDto.setVariablesArray(JsArrays.<VariableDto>create());
        updatedView.setExtension(VariableListViewDto.ViewDtoExtensions.view, listDto);
      } else {
        updatedView.setExtension(VariableListViewDto.ViewDtoExtensions.view,
            view.getExtension(VariableListViewDto.ViewDtoExtensions.view));
      }
    } else {
      FileViewDto fileView = FileViewDto.create();
      fileView.setFilename(fileName);
      fileView.setType(getFileType(fileName));
      updatedView.setExtension(FileViewDto.ViewDtoExtensions.view, fileView);
    }

    return updatedView;
  }

  private FileViewDto.FileViewType getFileType(String fileName) {
    RegExp regExp = RegExp.compile("\\.xml$");
    if(regExp.test(fileName.toLowerCase())) {
      return FileViewDto.FileViewType.SERIALIZED_XML;
    }

    return FileViewDto.FileViewType.EXCEL;
  }

  private void renderSelectableTables() {
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource("/datasources/tables").get()
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            JsArray<TableDto> tables = JsArrays.toSafeArray(resource);
            if (view == null) {
              getView().prepareTables(tables, null, null);
            } else {
              TableDto viewTableDto = findViewTabledto(tables);
              getView().prepareTables(filterTables(tables, viewTableDto), view.getFromArray(), view.getInnerFromArray());
            }
          }

          /**
           * Search for the table corresponding to the view.
           * @param tables
           * @return
           */
          private TableDto findViewTabledto(JsArray<TableDto> tables) {
            TableDto viewTableDto = null;
            for(TableDto table : JsArrays.toIterable(tables)) {
              if(getDatasourceName().equals(table.getDatasourceName()) && view.getName().equals(table.getName())) {
                viewTableDto = table;
                break;
              }
            }
            return viewTableDto;
          }

          /**
           * Remove from selection the view itself and the tables of different entity types.
           * @param tables
           * @param viewTableDto
           * @return
           */
          private JsArray<TableDto> filterTables(JsArray<TableDto> tables, TableDto viewTableDto) {
            if(viewTableDto == null) return tables;

            boolean hasEntityType = hasEntityType(viewTableDto);
            JsArray<TableDto> filteredTables = JsArrays.create();
            for(TableDto table : JsArrays.toIterable(tables)) {
              if(!table.equals(viewTableDto) && (!hasEntityType || hasEntityType(table) && table.getEntityType().equals(viewTableDto.getEntityType()))) {
                filteredTables.push(table);
              }
            }
            return filteredTables;
          }

          private boolean hasEntityType(TableDto dto) {
            return dto.hasEntityType() && !dto.getEntityType().equals("?");
          }

        }).send();
  }

  public interface Display extends PopupView, HasUiHandlers<ViewModalUiHandlers> {

    enum FormField {
      NAME,
      TABLES,
      FILE_SELECTION
    }

    void renderProperties(ViewDto view);

    void prepareTables(JsArray<TableDto> tables, JsArrayString froms, JsArrayString innerFroms);

    void showError(String message, @Nullable FormField id);

    HasText getName();

    HasCollection<TableDto> getSelectedTables();

    void setFileSelectionDisplay(FileSelectionPresenter.Display display);
  }

  private class CompletedCallback implements ResponseCodeCallback {

    private final String viewName;

    private CompletedCallback(String viewName) {
      this.viewName = viewName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_CREATED) {
        getView().hide();
        placeManager.revealPlace(ProjectPlacesHelper.getTablePlace(getDatasourceName(), viewName));
      } else if(response.getStatusCode() == Response.SC_FORBIDDEN) {
        getView().showError(translations.userMessageMap().get("UnauthorizedOperation"), null);
      } else {
        ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
        getView().showError(TranslationsUtils
            .replaceArguments(translations.userMessageMap().get(error.getStatus()), error.getArgumentsArray()), null);
      }
    }
  }

  private class PropertiesValidationHandler extends ViewValidationHandler {

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
      getView().showError(message, Display.FormField.valueOf(id));
    }

    private void addViewNameValidators(Collection<FieldValidator> validators) {
      validators.add(
          new RequiredTextValidator(getView().getName(), "ViewNameRequired", Display.FormField.NAME.name()));
      validators.add(
          new DisallowedCharactersValidator(getView().getName(), new char[] { '.', ':' }, "ViewNameDisallowedChars",
              Display.FormField.NAME.name()));
    }

    private void addTablesValidators(Collection<FieldValidator> validators) {
      validators.add(new MinimumSizeCollectionValidator<>(getView().getSelectedTables(), 1, "TableSelectionRequired", Display.FormField.TABLES.name()));
      validators.add(new MatchingTableEntitiesValidator(getView().getSelectedTables(), Display.FormField.TABLES.name()));
    }

    private void addFileSelectionValidators(Collection<FieldValidator> validators) {
      validators.add(
          new RequiredFileSelectionValidator("\\.(xml|xls|xlsx)$", fileSelectionPresenter.getView().getFileText(),
              "XMLOrExcelFileRequired", Display.FormField.FILE_SELECTION.name()));
    }
  }

}
