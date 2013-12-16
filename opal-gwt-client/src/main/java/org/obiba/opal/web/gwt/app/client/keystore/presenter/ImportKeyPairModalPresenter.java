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

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.keystore.support.KeystoreType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import edu.umd.cs.findbugs.annotations.Nullable;

public class ImportKeyPairModalPresenter extends ModalPresenterWidget<ImportKeyPairModalPresenter.Display>
    implements KeyPairModalUiHandlers {

  public enum ImportType {
    KEY_PAIR,
    CERTIFICATE
  }

  private KeystoreType keystoreType;

  private ImportType importType;

  private SaveHandler saveHandler;

  @Inject
  public ImportKeyPairModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    getView().clearErrors();

    if(new ViewValidator().validate()) {
      if(saveHandler != null) {
        saveHandler.save(getView().getPublicKey().getText(), getView().getPrivateKey().getText(),
            getView().getName().getText());
      }
      getView().close();
    }
  }

  public void initialize(KeystoreType kType, ImportType type, SaveHandler handler) {
    saveHandler = handler;
    keystoreType = kType;
    importType = type;
    getView().setType(kType, type);
  }

  public interface SaveHandler {
    void save(@Nonnull String publicKey, @Nullable String privateKey, @Nullable String alias);
  }

  private final class ViewValidator extends ViewValidationHandler {

    private ViewValidator() {}

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      if (keystoreType == KeystoreType.PROJECT) {
        validators.add(new RequiredTextValidator(getView().getName(), "KeyPairAliasIsRequired",
            Display.FormField.NAME.name()));
      }

      if(importType == ImportType.KEY_PAIR) {
        validators.add(new RequiredTextValidator(getView().getPrivateKey(), "KeyPairPrivateKeyPEMIsRequired",
            Display.FormField.PRIVATE_KEY.name()));
      }

      validators.add(new RequiredTextValidator(getView().getPublicKey(), "KeyPairPublicKeyPEMIsRequired",
          Display.FormField.PUBLIC_KEY.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  public interface Display extends KeyPairDisplay<Display.FormField>, PopupView, HasUiHandlers<KeyPairModalUiHandlers> {

    enum FormField {
      NAME,
      PUBLIC_KEY,
      PRIVATE_KEY
    }

    HasText getName();

    HasText getPublicKey();

    HasText getPrivateKey();

    void setType(KeystoreType kType, ImportType type);
  }
}

