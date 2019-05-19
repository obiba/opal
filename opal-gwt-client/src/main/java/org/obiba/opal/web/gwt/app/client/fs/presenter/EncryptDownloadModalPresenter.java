/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.presenter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import org.obiba.opal.web.gwt.app.client.fs.FileDtos;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FilesDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class EncryptDownloadModalPresenter extends ModalPresenterWidget<EncryptDownloadModalPresenter.Display>
    implements EncryptDownloadModalUiHandlers {

  private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final double PASSWORD_CHARACTERS_LENGTH = PASSWORD_CHARACTERS.length();
  private static final int PASSWORD_MX_LENGTH = 16;

  private static final int MIN_PASSWORD_LENGTH = 8;

  private final ValidationHandler validationHandler;

  private List<FileDto> files;


  @Inject
  public EncryptDownloadModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    validationHandler = new PasswordValidationHandler();
  }

  @Override
  protected void onBind() {
  }

  @Override
  public void onDownload() {
    getView().clearErrors();
    if(validationHandler.validate()) {
      String password = getView().getPassword().getText();
      if(files.size() == 1) {
        fireEvent(new FileDownloadRequestEvent.Builder(FileDtos.getLink(files.get(0))).password(password).build());
      } else {
        fireEvent(
            new FilesDownloadRequestEvent.Builder(FileDtos.getParent(files.get(0)), files).password(password).build()
        );
      }
      getView().hideDialog();
    }
  }

  @Override
  public void onViewPassword() {

  }

  @Override
  public String onGeneratePassword() {
    String generated = "";

    for( int i=0; i < PASSWORD_MX_LENGTH; i++ ) {
      generated += PASSWORD_CHARACTERS.charAt((int)Math.floor(Math.random() * PASSWORD_CHARACTERS_LENGTH));
    }

    return generated;
  }

  public void setFiles(List<FileDto> files) {
    this.files = files;
  }

  private class PasswordValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<>();
        if (getView().shouldEncrypt()) {
          String passwordForm = Display.FormField.PASSWORD.name();
          HasText password = getView().getPassword();
          validators.add(new RequiredTextValidator(password, "PasswordIsRequired", passwordForm));
          ConditionValidator minLength =
              new ConditionValidator(minLengthCondition(password), "PasswordLengthMin", passwordForm);
          minLength.setArgs(Lists.newArrayList(String.valueOf(MIN_PASSWORD_LENGTH)));
          validators.add(minLength);
        }
      }
      return validators;
    }

    private HasValue<Boolean> minLengthCondition(final HasText password) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return !password.getText().isEmpty() && password.getText().length() >= MIN_PASSWORD_LENGTH;
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }

  }

  public interface Display extends PopupView, HasUiHandlers<EncryptDownloadModalUiHandlers> {

    enum FormField {
      PASSWORD,
    }

    boolean shouldEncrypt();

    HasText getPassword();

    void clearErrors();

    void showError(@Nullable FormField formField, String message);

    void hideDialog();
  }

}
