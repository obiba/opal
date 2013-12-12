/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class CreateKeyPairModalPresenter extends ModalPresenterWidget<CreateKeyPairModalPresenter.Display>
    implements CreateKeyPairModalUiHandlers {

  private SaveHandler saveHandler;

  @Inject
  public CreateKeyPairModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    if(new ViewValidator().validate()) {
      if(saveHandler != null) {
        saveHandler.save(getView().getAlgorithm().getText(), getView().getSize().getText(),
            getView().getFirstLastName().getText(), getView().getOrganizationalUnit().getText());
      }
      getView().close();
    }
  }

  public void initialize(SaveHandler saveHandler) {
    this.saveHandler = saveHandler;
  }

  private final class ViewValidator extends ViewValidationHandler {

    private ViewValidator() {}

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getView().getAlgorithm(), "KeyPairAlgorithmIsRequired",
          Display.FormField.ALGORITHM.name()));
      validators.add(
          new RequiredTextValidator(getView().getSize(), "KeyPairKeySizeIsRequired", Display.FormField.SIZE.name()));
      validators.add(new RequiredTextValidator(getView().getFirstLastName(), "KeyPairFirstAndLastNameIsRequired",
          Display.FormField.FIRST_LAST_NAME.name()));
      validators.add(new RequiredTextValidator(getView().getOrganizationalUnit(), "KeyPairOrganizationalUnitIsRequired",
          Display.FormField.ORGANIZATIONAL_UNIT.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  public interface SaveHandler {
    void save(String algorithm, String size, String firstLastName, String organizationalUnit);
  }

  public interface Display extends PopupView, HasUiHandlers<CreateKeyPairModalUiHandlers> {

    enum FormField {
      ALGORITHM,
      SIZE,
      FIRST_LAST_NAME,
      ORGANIZATIONAL_UNIT
    }

    HasText getAlgorithm();

    HasText getSize();

    HasText getFirstLastName();

    HasText getOrganizationalUnit();

    void showError(@Nullable FormField formField, String message);

    void close();
  }

}