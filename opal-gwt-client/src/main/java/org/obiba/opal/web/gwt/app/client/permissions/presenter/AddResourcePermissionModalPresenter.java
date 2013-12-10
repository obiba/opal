/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.presenter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.AbstractFieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.Subject;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class AddResourcePermissionModalPresenter
    extends ModalPresenterWidget<AddResourcePermissionModalPresenter.Display>
    implements ResourcePermissionModalUiHandlers {

  private UpdateResourcePermissionHandler updateHandler;

  private List<Acl> currentAclList;

  @Inject
  public AddResourcePermissionModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    getView().getSubjectType().setValue(Subject.SubjectType.USER.getName());
    getView().getSubjectType().setValue(Subject.SubjectType.GROUP.getName());
  }

  public void initialize(@Nonnull ResourcePermissionType type, @Nonnull UpdateResourcePermissionHandler updateHandler,
      List<Acl> currentAclList) {
    getView().setData(type);
    this.updateHandler = updateHandler;
    this.currentAclList = currentAclList;
  }

  @Override
  public void save() {
    getView().clearErrors();
    if(new ViewValidatorHandler().validate()) {
      if (updateHandler != null) {
        updateHandler.update(Arrays.asList(getView().getPrincipal().getText()), getView().getSubjectType().getValue(),
            getView().getPermission());
      }
      getView().close();
    }
  }

  private final class ViewValidatorHandler extends ViewValidationHandler {

    private ViewValidatorHandler() {}

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(
          new RequiredTextValidator(getView().getPrincipal(), "NameIsRequired", Display.FormField.PRINCIPAL.name()));
      validators.add(
          new PermissionValidator(getView().getPermission(), "PermissionRequired", Display.FormField.PERMISSIONS.name()));
      validators.add(
          new DuplicateSubjectValidator(getView().getPrincipal().getText(), getView().getSubjectType().getValue(),
              Display.FormField.PERMISSIONS.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(message, Display.FormField.valueOf(id));
    }
  }

  private final class PermissionValidator extends AbstractFieldValidator {

    private final String permission;

    public PermissionValidator(String permission, String errorMessageKey, String id) {
      super(errorMessageKey, id);
      this.permission = permission;
    }

    @Override
    protected boolean hasError() {
      return permission == null;
    }
  }

  private final class DuplicateSubjectValidator extends AbstractFieldValidator {

    private final String principal;
    private final Subject.SubjectType type;

    public DuplicateSubjectValidator(String principal, String typeName, String id) {
      super("", id);
      this.principal = principal;
      type = Subject.SubjectType.USER.getName().equals(typeName) ? Subject.SubjectType.USER : Subject.SubjectType.GROUP;
      setErrorMessageKey(
          Subject.SubjectType.USER.isSubjectType(type) ? "DuplicateAclSubjectUser" : "DuplicateAclSubjectGroup");
      setArgs(Arrays.asList(principal));
    }

    @Override
    protected boolean hasError() {

      for (Acl acl : currentAclList) {
        Subject subject = acl.getSubject();
        if (subject.getPrincipal().equals(principal) && subject.getType().isSubjectType(type)) {
          return true;
        }
      }

      return false;
    }
  }

  public interface Display extends PopupView, HasUiHandlers<ResourcePermissionModalUiHandlers> {
    enum FormField {
      PRINCIPAL,
      SUBJECT_TYPE,
      PERMISSIONS
    }

    void setData(ResourcePermissionType type);

    String getPermission();

    TakesValue<String> getSubjectType();

    HasText getPrincipal();

    void close();

    void showError(String message, FormField field);

    void clearErrors();
  }

}