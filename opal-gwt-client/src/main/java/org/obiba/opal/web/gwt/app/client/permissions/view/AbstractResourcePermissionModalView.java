/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.view;

import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ResourcePermissionsPanel;

import com.google.gwt.uibinder.client.UiField;
import com.google.web.bindery.event.shared.EventBus;

public abstract class AbstractResourcePermissionModalView<C extends ModalUiHandlers>
    extends ModalPopupViewWithUiHandlers<C> {

  @UiField
  Modal dialog;

  @UiField
  ResourcePermissionsPanel permissions;

  public AbstractResourcePermissionModalView(EventBus eventBus) {
    super(eventBus);
  }

  protected String getSelectedPermission() {
    return permissions.getSelectedPermission();
  }

  protected void createPermissionRadios(ResourcePermissionType type, String permission) {
    permissions.initialize(type, permission);
  }
}
