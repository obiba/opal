/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.model.client.opal.AclAction;

import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

import javax.annotation.Nullable;

public class ResourcePermissionsPanel extends Composite {

  static final Translations translations = GWT.create(Translations.class);

  private final Panel permissions;

  private ResourcePermissionType type;

  private String selectedPermission;

  public ResourcePermissionsPanel() {
    permissions = new FlowPanel();
    initWidget(permissions);
  }

  public void initialize(@Nonnull ResourcePermissionType resourcePermissionType, @Nullable String currentPermission) {
    type = resourcePermissionType;
    selectedPermission = currentPermission == null ? type.getPermissions().get(0).getName() : currentPermission;
    createPermissionRadios();
  }

  public String getSelectedPermission() {
    return selectedPermission;
  }

  private void createPermissionRadios() {
    permissions.clear();
    boolean addTopMargin = false;

    for(AclAction aclAction : type.getPermissions()) {
      String permission = aclAction.getName();
      boolean select = permission.equals(selectedPermission);
      permissions.add(createPermissionPanel(permission, select, addTopMargin));
      addTopMargin = true;
    }
  }

  private Panel createPermissionPanel(final String permissionKey, boolean select, boolean addTopMargin) {
    Panel panel = new FlowPanel();
    if (addTopMargin) panel.setStyleName("large-top-margin");
    RadioButton radio = new RadioButton("permission", translations.permissionMap().get(permissionKey));
    radio.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        selectedPermission = permissionKey;
      }
    });

    radio.addStyleName("no-bottom-margin");
    radio.setValue(select);
    HelpBlock help = new HelpBlock(translations.permissionExplanationMap().get(permissionKey + ".help"));

    panel.add(radio);
    panel.add(help);

    return panel;
  }

}
