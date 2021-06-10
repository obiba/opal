/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.Acl;

import javax.annotation.Nonnull;
import java.util.List;

public class ResourcePermissionsView extends ViewWithUiHandlers<ResourcePermissionsUiHandlers>
    implements ResourcePermissionsPresenter.Display {

  interface Binder extends UiBinder<Widget, ResourcePermissionsView> {
  }

  @UiField
  CellTable<Acl> permissionsTable;

  @UiField
  OpalSimplePager tablePager;

  @UiField
  DropdownButton addDropdown;

  private final static Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<Acl> permissionsDataProvider = new ListDataProvider<Acl>();

  private ActionsColumn<Acl> actionsColumn;

  @Inject
  public ResourcePermissionsView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    addDropdown.setText(translations.addPermission());
    initSubjectsPermissionTable();
  }

  @Override
  public void setData(@Nonnull List<Acl> acls) {
    renderSubjectsPermissionTable(acls);
  }

  @Override
  public void setActionHandler(ActionHandler<Acl> handler) {
    actionsColumn.setActionHandler(handler);
  }

  @Override
  public List<Acl> getAclList() {
    return permissionsDataProvider.getList();
  }

  @UiHandler("addUserPermission")
  public void onAddUserPermission(ClickEvent event) {
    getUiHandlers().addUserPermission();
  }

  @UiHandler("addGroupPermission")
  public void onAddGroupPermission(ClickEvent event) {
    getUiHandlers().addGroupPermission();
  }

  private void renderSubjectsPermissionTable(List<Acl> acls) {
    permissionsDataProvider.setList(acls);
    tablePager.firstPage();
    permissionsDataProvider.refresh();
    tablePager.setPagerVisible(permissionsDataProvider.getList().size() > tablePager.getPageSize());
  }

  private void initSubjectsPermissionTable() {
    tablePager.setDisplay(permissionsTable);
    permissionsTable.addColumn(new TextColumn<Acl>() {

      @Override
      public String getValue(Acl acl) {
        return acl.getSubject().getPrincipal();
      }
    }, translations.nameLabel());
    permissionsTable.addColumn(new TextColumn<Acl>() {

      @Override
      public String getValue(Acl acl) {
        return translations.shortSubjectTypeMap().get(acl.getSubject().getType().getName());
      }
    }, translations.typeLabel());
    permissionsTable.addColumn(new TextColumn<Acl>() {

      @Override
      public String getValue(Acl acl) {
        return translations.permissionMap().get(acl.getActions(0));
      }
    }, translations.permissionLabel());
    actionsColumn = new ActionsColumn<Acl>(new ActionsProvider<Acl>() {

      @Override
      public String[] allActions() {
        return new String[]{ActionsColumn.EDIT_ACTION, ActionsColumn.REMOVE_ACTION};
      }

      @Override
      public String[] getActions(Acl value) {
        String action = value.getActions(0);
        if (ResourcePermissionType.PROJECT.hasPermission(action) ||
            ResourcePermissionType.VARIABLE.hasPermission(action)) {
          return new String[]{ActionsColumn.REMOVE_ACTION};
        }

        return allActions();
      }
    });
    permissionsTable.addColumn(actionsColumn, translations.actionsLabel());
    permissionsDataProvider.addDataDisplay(permissionsTable);
  }

}
