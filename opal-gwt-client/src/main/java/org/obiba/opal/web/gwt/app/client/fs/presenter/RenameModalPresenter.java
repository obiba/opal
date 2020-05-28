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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.fs.event.FileRenameRequestEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.model.client.opal.FileDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class RenameModalPresenter extends ModalPresenterWidget<RenameModalPresenter.Display>
    implements RenameModalUiHandlers {

  private final ValidationHandler validationHandler;

  private FileDto currentFolder;

  private FileDto selectedFile;

  @Inject
  public RenameModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    validationHandler = new RenameValidationHandler();
  }

  @Override
  protected void onBind() {
  }

  public void initialize(FileDto folder, FileDto file) {
    currentFolder = folder;
    selectedFile = file;
    getView().getName().setText(selectedFile.getName());
  }

  @Override
  public void onRename() {
    getView().clearErrors();
    if(validationHandler.validate()) {
      String newName = getView().getName().getText();
      if (!newName.equals(selectedFile.getName())) {
        fireEvent(new FileRenameRequestEvent(currentFolder, selectedFile, newName));
      }
      getView().hideDialog();
    }
  }

  private class RenameValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<>();
          String nameForm = Display.FormField.NAME.name();
          HasText name = getView().getName();
          validators.add(new RequiredTextValidator(name, "NameIsRequired", nameForm));
          validators.add(new FileNameUniqueCondition(currentFolder.getChildrenArray(), selectedFile, name, nameForm));
        }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  private static class FileNameUniqueCondition extends AbstractFieldValidator {

    private final HasText newName;

    private final JsArray<FileDto> files;

    private final FileDto selectedFile;

    private FileNameUniqueCondition(JsArray<FileDto> dtos, FileDto file, HasText name, String id) {
      super("FileAlreadyExists", id);
      files = dtos;
      newName = name;
      selectedFile = file;
    }

    @Override
    protected boolean hasError() {
      String newNameValue = newName.getText();
      FileDto.FileType selectedfileType = selectedFile.getType();

      if (!newNameValue.equals(selectedFile.getName())) {
        for(int i = 0; i < files.length(); i++) {
          FileDto f = files.get(i);

          if(newNameValue.equals(f.getName()) && (!FileDto.FileType.FILE.isFileType(selectedfileType) ||
              f.getType().getName().equals(selectedfileType.getName()))) {

            setErrorMessageKey(FileDto.FileType.FILE.isFileType(f.getType()) ? "FileAlreadyExists" : "FolderAlreadyExists");
            List<String> args = new ArrayList<String>();
            args.add(newNameValue);
            setArgs(args);
            return true;
          }
        }
      }
      return false;
    }
  }

  public interface Display extends PopupView, HasUiHandlers<RenameModalUiHandlers> {

    enum FormField {
      NAME
    }

    HasText getName();

    void clearErrors();

    void showError(@Nullable FormField formField, String message);

    void hideDialog();
  }

}
