/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.UpdateResourcePermissionModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.model.client.opal.Acl;

import com.github.gwtbootstrap.client.ui.Heading;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class UpdateResourcePermissionModalView extends AbstractResourcePermissionModalView<ResourcePermissionModalUiHandlers>
    implements UpdateResourcePermissionModalPresenter.Display {

  interface Binder extends UiBinder<Widget, UpdateResourcePermissionModalView> {}

  @UiField
  Heading subjectLabel;

  private final Translations translations;

  @Inject
  public UpdateResourcePermissionModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    dialog.setTitle(translations.updateResourcePermissionsModalTile());
    // TODO: fix the width of the dialog
  }

  @Override
  public void setData(ResourcePermissionType type, Acl acl) {
    subjectLabel.setText(translations.userResourcePermissionLabel() + acl.getSubject().getPrincipal());
    createPermissionRadios(type, acl.getActions(0));
  }

  @Override
  public String getPermission() {
    return getSelectedPermission();
  }

  @Override
  public void close() {
    dialog.hide();
  }

  @UiHandler("saveButton")
  public void onSaveButtonClicked(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCloseButtonClicked(ClickEvent event) {
    close();
  }

}
