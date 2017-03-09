/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.project.genotypes;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProjectImportVcfFileModalPresenter extends ModalPresenterWidget<ProjectImportVcfFileModalPresenter.Display>
    implements ProjectImportVcfFileModalUiHandlers {

  private final ValidationHandler validationHandler;

  @Inject
  public ProjectImportVcfFileModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    validationHandler = new ModalValidationHandler();
  }

  @Override
  protected void onBind() {
  }

  @Override
  public void onImport() {
    getView().clearErrors();
    if(validationHandler.validate()) {

    }

  }

  private class ModalValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<>();
        validators.add(new RequiredTextValidator(getView().getName(),
                "NameIsRequired", Display.FormField.NAME.name()));
      }
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }

  }

  public interface Display extends PopupView, HasUiHandlers<ProjectImportVcfFileModalUiHandlers> {

    enum FormField {
      NAME
    }

    HasText getName();

    void clearErrors();

    void showError(@Nullable FormField formField, String message);

    void hideDialog();
  }

}
