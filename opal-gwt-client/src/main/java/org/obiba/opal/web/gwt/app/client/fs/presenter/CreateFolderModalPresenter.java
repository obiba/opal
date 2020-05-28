/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderCreatedEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FolderUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class CreateFolderModalPresenter extends ModalPresenterWidget<CreateFolderModalPresenter.Display>
    implements CreateFolderUiHandlers {

  private final ValidationHandler validationHandler;

  private FileDto currentFolder;

  @Inject
  public CreateFolderModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    validationHandler = new FolderNameValidationHandler();
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(FolderUpdatedEvent.getType(), new FolderUpdatedEvent.FolderUpdatedHandler() {
      @Override
      public void onFolderUpdated(FolderUpdatedEvent event) {
        currentFolder = event.getFolder();
      }
    });
  }

  @Override
  public void createFolder() {
    getView().clearErrors();
    if(validationHandler.validate()) {
      createRemoteFolder(currentFolder.getPath(), getView().getFolderName().getText());
    }
  }

  private void createRemoteFolder(String destination, String folder) {

    ResourceCallback<FileDto> createdCallback = new ResourceCallback<FileDto>() {

      @Override
      public void onResource(Response response, FileDto resource) {
        fireEvent(new FolderCreatedEvent(resource));
        getView().hideDialog();
      }
    };

    ResponseCodeCallback error = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    };

    ResourceRequestBuilderFactory.<FileDto>newBuilder().forResource("/files" + destination).post()
        .withBody("text/plain", folder).withCallback(createdCallback).withCallback(Response.SC_FORBIDDEN, error)
        .withCallback(Response.SC_INTERNAL_SERVER_ERROR, error).send();
  }

  public void setCurrentFolder(FileDto fileDto) {
    currentFolder = fileDto;
  }

  private class FolderNameValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getFolderName(), "FolderNameIsRequired",
            Display.FormField.NAME.name()));
        validators
            .add(new ConditionValidator(dotNamesCondition(), "DotNamesAreInvalid", Display.FormField.NAME.name()));
        validators.add(new ConditionValidator(invalidCharactersCondition(), "FolderNameInvalidCharacters",
            Display.FormField.NAME.name()));
      }
      return validators;
    }

    private HasValue<Boolean> dotNamesCondition() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !(".".equals(getView().getFolderName().getText()) || "..".equals(getView().getFolderName().getText()));
        }
      };
    }

    private HasValue<Boolean> invalidCharactersCondition() {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !(getView().getFolderName().getText().contains("#") ||
              getView().getFolderName().getText().contains("%"));
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }

  }

  public interface Display extends PopupView, HasUiHandlers<CreateFolderUiHandlers> {

    enum FormField {
      NAME,
    }

    HasText getFolderName();

    void clearErrors();

    void showError(@Nullable FormField formField, String message);

    void hideDialog();
  }

}
