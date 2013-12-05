package org.obiba.opal.web.gwt.app.client.permissions.view;

import java.util.Arrays;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.AddResourcePermissionModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.permissions.support.PermissionResourceType;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.model.client.opal.Subject;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AddResourcePermissionModalView extends AbstractResourcePermissionModalView<ResourcePermissionModalUiHandlers>
    implements AddResourcePermissionModalPresenter.Display{

  interface Binder extends UiBinder<Widget, AddResourcePermissionModalView> {}

  @UiField
  Modal dialog;

  @UiField
  TextBox principal;

  @UiField
  Chooser subjectType;

  private final Translations translations;

  @Inject
  public AddResourcePermissionModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    dialog.setTitle(translations.updateResourcePermissionsModalTile());
    subjectType.addItem(Subject.SubjectType.GROUP.getName());
    subjectType.addItem(Subject.SubjectType.USER.getName());
  }

  @Override
  public void setData(PermissionResourceType type) {
    createPermissionRadios(type, null);
  }

  @Override
  public String getPermission() {
    return getSelectedPermission();
  }

  @Override
  public List<String> getPrincipals() {
    // TODO change component for a multiple input
    return Arrays.asList(principal.getText());
  }

  @Override
  public String getSubjectType() {
    return subjectType.getSelectedValue();
  }

  @Override
  public void close() {
    dialog.hide();
  }

  @UiHandler("saveButton")
  public void onSaveButton(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCloseButton(ClickEvent event) {
    close();
  }

}
