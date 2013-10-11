package org.obiba.opal.web.gwt.app.client.project.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectCreatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.ProjectDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class AddProjectPresenter extends ModalPresenterWidget<AddProjectPresenter.Display>
    implements AddProjectUiHandlers {

  private JsArray<ProjectDto> projects;

  private final Translations translations;

  protected ValidationHandler validationHandler;

  @Inject
  public AddProjectPresenter(EventBus eventBus, Display display, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    getView().setUiHandlers(this);

    validationHandler = new ProjectValidationHandler();
  }

  @Override
  public void save() {
    getView().clearErrors();

    if(validationHandler.validate()) {
      ResponseCodeCallback callback = new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == Response.SC_CREATED) {
            getEventBus().fireEvent(new ProjectCreatedEvent(null));
            getView().hideDialog();
          } else if(!Strings.isNullOrEmpty(response.getText())) {
            ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
            getEventBus().fireEvent(
                NotificationEvent.newBuilder().error("ProjectCreationFailed").args(errorDto.getArgumentsArray())
                    .build());
          } else {
            getEventBus()
                .fireEvent(NotificationEvent.newBuilder().error(translations.datasourceCreationFailed()).build());
          }
        }
      };

      ResourceRequestBuilderFactory.<ProjectDto>newBuilder() //
          .forResource("/projects")  //
          .withResourceBody(ProjectDto.stringify(getDto())) //
          .withCallback(callback, Response.SC_CREATED, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR) //
          .post().send();

    }
  }

  private ProjectDto getDto() {
    ProjectDto dto = ProjectDto.create();
    dto.setName(getView().getName().getText());
    String title = getView().getTitle().getText();
    dto.setTitle(Strings.isNullOrEmpty(title) ? dto.getName() : title);
    dto.setDescription(getView().getDescription().getText());
    dto.setDatabase(getView().getDatabase().getText());
    return dto;
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  public void setProjects(JsArray<ProjectDto> projects) {
    this.projects = projects;
  }

  private class ProjectValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getName(), "NameIsRequired", Display.FormField.NAME.name()));
        validators.add(new UniqueProjectNameValidator());
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
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

  public interface Display extends PopupView, HasUiHandlers<AddProjectUiHandlers> {

    void clearErrors();

    enum FormField {
      NAME,
    }

    HasText getName();

    HasText getTitle();

    HasText getDescription();

    HasText getDatabase();

    void showError(@Nullable FormField formField, String message);

    void hideDialog();
  }
}
