/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.view;

import java.util.Iterator;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsUiHandlers;
import org.obiba.opal.web.gwt.app.client.permissions.support.PermissionResourceType;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.AclAction;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.IconCell;
import com.github.gwtbootstrap.client.ui.constants.IconSize;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ResourcePermissionsView extends ViewWithUiHandlers<ResourcePermissionsUiHandlers> implements
    ResourcePermissionsPresenter.Display {

  interface Binder extends UiBinder<Widget, ResourcePermissionsView> {}

  @UiField
  Button addPermission;

  @UiField
  Chooser displayMode;

  @UiField
  FlowPanel permissionTablePanel;

  @UiField
  CellTable<Acl> permissionsTable;

  @UiField
  FlowPanel subjectsPermissionTablePanel;

  @UiField
  CellTable<Acl> subjectsPermissionsTable;

  private final static Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<Acl> subjectsPermissionDataProvider = new ListDataProvider<Acl>();


  @Inject
  public ResourcePermissionsView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setData(PermissionResourceType resourceType, List<Acl> acls) {
    renderSubjectsPermissionTable(resourceType, acls);
  }

  @Override
  public HasActionHandler<Acl> getActions() {
    return AbstractPermissionsClass.ACTIONS;
  }

  private void renderSubjectsPermissionTable(PermissionResourceType resourceType, List<Acl> acls) {
    initSubjectsPermissionTable(resourceType);
    subjectsPermissionDataProvider.setList(acls);
    subjectsPermissionDataProvider.refresh();
  }

  private void initPermissionsTable(PermissionResourceType resourceType) {

  }

  private static class PermissionTypeProviderData {
  }


  private void initSubjectsPermissionTable(PermissionResourceType resourceType) {
    subjectsPermissionsTable.addColumn(SubjectsPermissionColumns.NAME, "Name");
    subjectsPermissionsTable.addColumn(SubjectsPermissionColumns.TYPE, "Type");
    subjectsPermissionsTable.addColumn(SubjectsPermissionColumns.PERMISSION, "Permission");

    for (Iterator<AclAction> iterator = resourceType.getPermissions().iterator(); iterator.hasNext();) {
      AclAction action = iterator.next();
      subjectsPermissionsTable
          .addColumn(new PermissionColumn(action), translations.permissionMap().get(action.getName()));
    }

    subjectsPermissionsTable.addColumn(SubjectsPermissionColumns.ACTIONS, translations.actionsLabel());
    subjectsPermissionDataProvider.addDataDisplay(subjectsPermissionsTable);
//    subjectsPermissionsTable.setEmptyTableWidget(new Label(translations.noVcsCommitHistoryAvailable()));
  }

  private static abstract class AbstractPermissionsClass {
    static final ActionsColumn<Acl> ACTIONS = new ActionsColumn<Acl>(new ActionsProvider<Acl>() {

      @Override
      public String[] allActions() {
        return new String[] { ActionsColumn.EDIT_ACTION, ActionsColumn.DELETE_ACTION };
      }

      @Override
      public String[] getActions(Acl value) {
        return allActions();
      }
    });

  }

  private static final class SubjectsPermissionColumns extends AbstractPermissionsClass {

    static final Column<Acl, String> NAME = new TextColumn<Acl>() {

      @Override
      public String getValue(Acl acl) {
        return acl.getSubject().getPrincipal();
      }
    };

    static final Column<Acl, String> TYPE = new TextColumn<Acl>() {

      @Override
      public String getValue(Acl acl) {
        return acl.getSubject().getType().getName();
      }
    };

    static final Column<Acl, String> PERMISSION = new TextColumn<Acl>() {

      @Override
      public String getValue(Acl acl) {
        return translations.permissionMap().get(acl.getActions(0));
      }
    };
  }

  private static final class PermissionIconCell extends IconCell {

    private boolean visible = true;

    public PermissionIconCell(IconType iconType) {
      super(iconType);
    }
    public PermissionIconCell(IconType iconType, IconSize iconSize) {
      super(iconType, iconSize);
    }

    public void setVisible(boolean value) {
      visible = value;
    }

    @Override
    public void render(Context context, Void value, SafeHtmlBuilder sb) {
      if (!visible) return;
      super.render(context, value, sb);
    }

  }

  private static final class PermissionColumn extends Column<Acl, Void> {

    private final String permission;

    public PermissionColumn(AclAction permission) {
      super(new PermissionIconCell(IconType.OK, IconSize.SMALL));
      this.permission = permission.getName();
    }

    @Override
    public Void getValue(Acl acl) {
      boolean visible = false;
      for (String action : JsArrays.toIterable(acl.getActionsArray())) {
        if (action.equals(permission)) {
          visible = true;
          break;
        }
      }

      ((PermissionIconCell)getCell()).setVisible(visible);
      return null;
    }
  }

}
