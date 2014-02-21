package org.obiba.opal.web.gwt.app.client.project.properties;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
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
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.opal.ProjectFactoryDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
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

public class ProjectPropertiesModalPresenter extends ModalPresenterWidget<ProjectPropertiesModalPresenter.Display>
    implements ProjectPropertiesUiHandlers {

  private ProjectDto project;

  private JsArray<ProjectDto> projects;

  private ValidationHandler validationHandler;

  private final Translations translations;

  @Inject
  public ProjectPropertiesModalPresenter(EventBus eventBus, Display display, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  public void initialize(ProjectDto projectDto) {
    project = projectDto;
    getView().setProject(project);
  }

  @Override
  protected void onBind() {
    super.onBind();
    validationHandler = new ProjectValidationHandler();
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
        .forResource(UriBuilders.DATABASES_FOR_STORAGE.create().build()) //
        .withCallback(new ResourceCallback<JsArray<DatabaseDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatabaseDto> databases) {
            getView().setAvailableDatabases(databases);
            if(project != null) getView().getDatabase().setText(project.getDatabase());
          }
        }) //
        .get().send();
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
  }

  private void create() {
    if(validationHandler.validate()) {
      // Validate database connection
      final String databaseName = getView().getDatabase().getText();

      if(Strings.isNullOrEmpty(databaseName)) {
        createProject();
        return;
      }

      if(validationHandler.validate()) {
        // Validate database connection
        final String name = getView().getDatabase().getText();
        ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder() //
            .forResource(UriBuilders.DATABASE_CONNECTIONS.create().build(name)) //
            .withCallback(Response.SC_OK, new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                createProject();
              }
            }).withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().showError(Display.FormField.DATABASE, TranslationsUtils
                .replaceArguments(translations.userMessageMap().get("FailedToConnectToDatabase"), name));
            getView().setBusy(false);
          }
        }, Response.SC_SERVICE_UNAVAILABLE, Response.SC_NOT_FOUND, Response.SC_BAD_REQUEST) //
            .post().send();
      }
    }
  }

  private void update() {
    ResourceRequestBuilderFactory.<ProjectFactoryDto>newBuilder() //
        .forResource(UriBuilders.PROJECT.create().build(project.getName()))  //
        .withResourceBody(ProjectDto.stringify(getProjectDto())) //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            fireEvent(new ProjectUpdatedEvent(project));
          }
        }) //
        .withCallback(Response.SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget()) {
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
      for(String t : tags.split(" ")) {
        arr.push(t);
      }
    }
    dto.setDatabase(getView().getDatabase().getText());
    dto.setArchived(project.getArchived());
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
        .withCallback(Response.SC_CREATED, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            fireEvent(new ProjectCreatedEvent.Builder().build());
          }
        }) //
        .withCallback(Response.SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget()) {
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
    String tags = getView().getTags().getText();
    if(!Strings.isNullOrEmpty(tags)) {
      JsArrayString arr = JavaScriptObject.createArray().cast();
      dto.setTagsArray(arr);
      for(String t : tags.split(" ")) {
        arr.push(t);
      }
    }

    return dto;
  }

  private class ProjectValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired", Display.FormField.NAME.name()));
        validators.add(new UniqueProjectNameValidator());

        GWT.log("REGEX");
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
          if(projectDto.getName().equalsIgnoreCase(name)) {
            return true;
          }
        }
        return false;
      }
    }

  }

  public interface Display extends PopupView, HasUiHandlers<ProjectPropertiesUiHandlers> {

    enum FormField {
      NAME,
      DATABASE
    }

    void setProject(ProjectDto project);

    HasText getName();

    HasText getTitle();

    HasText getDescription();

    HasText getTags();

    HasText getDatabase();

    void showError(@Nullable FormField formField, String message);

    void hideDialog();

    void clearErrors();

    void setAvailableDatabases(JsArray<DatabaseDto> availableDatabases);

    void setBusy(boolean busy);
  }
}
