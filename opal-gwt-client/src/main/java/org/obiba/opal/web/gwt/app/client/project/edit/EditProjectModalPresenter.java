/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.edit;

import java.util.*;

import javax.annotation.Nullable;
import javax.inject.Provider;

import com.google.common.collect.Lists;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.copy.DataExportFolderService;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.admin.ProjectAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectCreatedEvent;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.opal.*;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_OK;

public class EditProjectModalPresenter extends ModalPresenterWidget<EditProjectModalPresenter.Display>
    implements EditProjectUiHandlers {

  @Nullable
  private String projectName;

  @Nullable
  private ProjectDto project;

  private JsArray<ProjectDto> projects;

  private ValidationHandler validationHandler;

  private final FileSelectionPresenter fileSelectionPresenter;

  private final DataExportFolderService dataExportFolderService;

  private final Translations translations;

  @Inject
  public EditProjectModalPresenter(EventBus eventBus,
                                   Display display,
                                   Provider<FileSelectionPresenter> fileSelectionPresenterProvider,
                                   DataExportFolderService dataExportFolderService,
                                   Translations translations) {
    super(eventBus, display);
    this.fileSelectionPresenter = fileSelectionPresenterProvider.get();
    this.dataExportFolderService = dataExportFolderService;
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  public void setProjectName(@Nullable String projectName) {
    this.projectName = projectName;
    if(!Strings.isNullOrEmpty(projectName)) {
      ResourceRequestBuilderFactory.<ProjectDto>newBuilder() //
          .forResource(UriBuilders.PROJECT.create().build(projectName)) //
          .withCallback(new ResourceCallback<ProjectDto>() {
            @Override
            public void onResource(Response response, ProjectDto projectDto) {
              project = projectDto;
              getView().setProject(project);
              getView().getDatabase().setText(project.getDatabase());
              getView().getVcfStoreService().setText(project.getVcfStoreService());
              getView().setExportFolder(project.getExportFolder());
            }
          }).get().send();
    }
  }

  @Override
  protected void onBind() {
    super.onBind();
    validationHandler = new ProjectValidationHandler();

    fileSelectionPresenter.setFileSelectionType(FileSelectorPresenter.FileSelectionType.FOLDER);
    fileSelectionPresenter.bind();
    getView().setFileWidgetDisplay(fileSelectionPresenter.getView());

    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
        .forResource(UriBuilders.DATABASES_FOR_STORAGE.create().build()) //
        .withCallback(new ResourceCallback<JsArray<DatabaseDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatabaseDto> databases) {
            getView().setAvailableDatabases(databases);
            if(project != null) {
              getView().getDatabase().setText(project.getDatabase());
              getView().getVcfStoreService().setText(project.getVcfStoreService());
              getView().setExportFolder(project.getExportFolder());
            }
          }
        }).get().send();

    Map<String, String> pluginsParams = new HashMap<>();
    pluginsParams.put("type", ProjectAdministrationPresenter.VCF_STORE_SERVICE_TYPE);

    ResourceRequestBuilderFactory.<PluginPackagesDto>newBuilder().forResource(UriBuilders.PLUGINS.create().query(pluginsParams).build())
        .withCallback(new ResourceCallback<PluginPackagesDto>() {

          @Override
          public void onResource(Response response, PluginPackagesDto resource) {
            List<PluginPackageDto> installed = Lists.newArrayList();
            for (PluginPackageDto pluginPackage : JsArrays.toIterable(resource.getPackagesArray())) {
              if (ProjectAdministrationPresenter.VCF_STORE_SERVICE_TYPE.equals(pluginPackage.getType()))
                installed.add(pluginPackage);
            }
            getView().setAvailableVcfStoreServices(installed);
          }
        }).get().send();
  }

  @Override
  public void save() {
    getView().setBusy(true);
    getView().clearErrors();
    if(project == null) {
      create();
    } else {
      update();
    }

    dataExportFolderService.setProjectExportFolder(project.getExportFolder());
  }

  private void create() {
    if(!validationHandler.validate()) return;
    createProject();
  }

  private void update() {
    ResourceRequestBuilderFactory.<ProjectFactoryDto>newBuilder() //
        .forResource(UriBuilders.PROJECT.create().build(projectName))  //
        .withResourceBody(ProjectDto.stringify(getProjectDto())) //
        .withCallback(SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            fireEvent(new ProjectUpdatedEvent(project));
          }
        }) //
        .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget()) {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setBusy(false);
            super.onResponseCode(request, response);
          }
        }) //
        .put().send();
  }

  private ProjectDto getProjectDto() {
    ProjectDto dto = ProjectDto.create();
    dto.setName(getView().getName().getText());
    String title = getView().getTitle().getText();
    dto.setTitle(Strings.isNullOrEmpty(title) ? dto.getName() : title);
    dto.setDescription(getView().getDescription().getText());
    String tags = getView().getTags().getText();
    if(!Strings.isNullOrEmpty(tags)) {
      JsArrayString arr = JavaScriptObject.createArray().cast();
      dto.setTagsArray(arr);
      for(String t : tags.split(",")) {
        arr.push(t.trim());
      }
    }
    dto.setDatabase(getView().getDatabase().getText());
    dto.setVcfStoreService(getView().getVcfStoreService().getText());
    dto.setExportFolder(getView().getExportFolder().getText());
    if(project != null) dto.setArchived(project.getArchived());
    return dto;
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  public void setProjects(JsArray<ProjectDto> projects) {
    this.projects = projects;
  }

  private void createProject() {

    ResourceRequestBuilderFactory.<ProjectFactoryDto>newBuilder() //
        .forResource("/projects")  //
        .withResourceBody(ProjectFactoryDto.stringify(getProjectFactoryDto())) //
        .withCallback(SC_CREATED, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            fireEvent(new ProjectCreatedEvent(getView().getName().getText()));
          }
        }) //
        .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget()) {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setBusy(false);
            super.onResponseCode(request, response);
          }
        }) //
        .post().send();
  }

  private ProjectFactoryDto getProjectFactoryDto() {
    ProjectFactoryDto dto = ProjectFactoryDto.create();
    dto.setName(getView().getName().getText());
    String title = getView().getTitle().getText();
    dto.setTitle(Strings.isNullOrEmpty(title) ? dto.getName() : title);
    dto.setDescription(getView().getDescription().getText());
    dto.setDatabase(getView().getDatabase().getText());
    dto.setVcfStoreService(getView().getVcfStoreService().getText());
    dto.setExportFolder(getView().getExportFolder().getText());
    String tags = getView().getTags().getText();
    if(!Strings.isNullOrEmpty(tags)) {
      JsArrayString tagsArray = JavaScriptObject.createArray().cast();
      dto.setTagsArray(tagsArray);
      for(String tag : tags.split(" ")) {
        tagsArray.push(tag);
      }
    }
    return dto;
  }

  public void setTag(String tag) {
    getView().setTag(tag);
  }

  private class ProjectValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<>();
        validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired", Display.FormField.NAME.name()));
        validators.add(new UniqueProjectNameValidator());
        validators.add(new RegExValidator(getView().getName(), "^[\\w _-]*$", "NameHasInvalidCharacters",
            Display.FormField.NAME.name()));
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().setBusy(false);
      getView().showError(Display.FormField.valueOf(id), message);
    }

    private class UniqueProjectNameValidator extends AbstractFieldValidator {

      private UniqueProjectNameValidator() {
        super("ProjectNameMustBeUnique", Display.FormField.NAME.name());
      }

      @Override
      protected boolean hasError() {
        String name = getView().getName().getText();
        for(ProjectDto projectDto : JsArrays.toIterable(projects)) {
          if(projectDto.getName().equals(name)) {
            return true;
          }
        }
        return false;
      }
    }

  }

  public interface Display extends PopupView, HasUiHandlers<EditProjectUiHandlers> {

    enum FormField {
      NAME,
      DATABASE
    }

    void setProject(ProjectDto project);

    void setTag(String tag);

    HasText getName();

    HasText getTitle();

    HasText getDescription();

    HasText getTags();

    HasText getDatabase();

    HasText getVcfStoreService();

    HasText getExportFolder();

    void setExportFolder(String exportFolder);

    void showError(@Nullable FormField formField, String message);

    void hideDialog();

    void clearErrors();

    void setAvailableDatabases(JsArray<DatabaseDto> availableDatabases);

    void setBusy(boolean busy);

    void setAvailableVcfStoreServices(List<PluginPackageDto> availableVcfStoreServices);

    void setFileWidgetDisplay(FileSelectionPresenter.Display display);
  }
}
