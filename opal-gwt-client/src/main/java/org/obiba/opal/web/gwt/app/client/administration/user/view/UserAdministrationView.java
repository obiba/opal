/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.user.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.IconActionCell;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.UserStatusIconActionCell;
import org.obiba.opal.web.model.client.opal.GroupDto;
import org.obiba.opal.web.model.client.opal.UserDto;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewImpl;

import static com.github.gwtbootstrap.client.ui.constants.IconType.OK;
import static org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserAdministrationPresenter.Display;

public class UserAdministrationView extends ViewImpl implements Display {

  @UiTemplate("UserAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, UserAdministrationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  @UiField
  Panel usersPanel;

  @UiField
  Panel groupsPanel;

  @UiField
  NavLink usersLink;

  @UiField
  NavLink groupsLink;

//  @UiField
//  DropdownButton actionsDropdown;

  //
  @UiField
  SimplePager indexTablePager;

//  @UiField
//  Alert selectAllAlert;
//
//  @UiField
//  Label selectAllStatus;
//
//  @UiField
//  Anchor selectAllAnchor;
//
//  @UiField
//  Anchor clearSelectionAnchor;

  @UiField
  CellTable<UserDto> usersTable;

  @UiField
  CellTable<GroupDto> groupsTable;

  @UiField
  Panel breadcrumbs;

  private final ListDataProvider<UserDto> userDataProvider = new ListDataProvider<UserDto>();

  private final ListDataProvider<GroupDto> groupDataProvider = new ListDataProvider<GroupDto>();

  Column<UserDto, UserDto> status;

  public UserAdministrationView() {
    uiWidget = uiBinder.createAndBindUi(this);
    usersLink.setActive(true);
    indexTablePager.setDisplay(usersTable);

//    checkboxColumn = new CheckboxColumn<TableIndexStatusDto>(new TableIndexStatusDtoDisplay());
//    checkboxColumn.setActionHandler(new ActionHandler<Integer>() {
//      @Override
//      public void doAction(Integer object, String actionName) {
//        selectAllAlert.setVisible(object > 0);
//      }
//    });
    Column<UserDto, String> name = new TextColumn<UserDto>() {

      @Override
      public String getValue(UserDto object) {
        return object.getName();
      }
    };

    Column<UserDto, String> groups = new TextColumn<UserDto>() {

      @Override
      public String getValue(UserDto object) {
        return object.getGroupsCount() > 0 ? object.getGroupsArray().join(", ") : "";
      }
    };

    status = new Column<UserDto, UserDto>(new UserStatusIconActionCell(OK, null)) {

      @Override
      public UserDto getValue(UserDto object) {
        return object;
      }
    };
    ActionsColumn<UserDto> actions = new ActionsColumn<UserDto>(new ActionsProvider<UserDto>() {

      private final String[] all = new String[] { ActionsColumn.EDIT_ACTION, ActionsColumn.DELETE_ACTION,
          PERMISSIONS_ACTION };

      @Override
      public String[] allActions() {
        return all;
      }

      @Override
      public String[] getActions(UserDto value) {
        return allActions();
      }
    });
    usersTable.addColumn(name, translations.userNameLabel());
    usersTable.addColumn(groups, translations.userGroupsLabel());
    usersTable.addColumn(status, translations.userStatusLabel());
    usersTable.addColumn(actions, translations.actionsLabel());
    usersTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
//    indexTable.setColumnWidth(checkboxColumn, 1, Style.Unit.PX);

    userDataProvider.addDataDisplay(usersTable);

    /*Groups*/
    groupsTable.addColumn(GroupColumns.name, translations.groupNameLabel());
    groupsTable.addColumn(GroupColumns.users, translations.groupUsersLabel());
    groupsTable.addColumn(GroupColumns.actions, translations.actionsLabel());
    groupDataProvider.addDataDisplay(groupsTable);
  }

  @Override
  public void setDelegate(IconActionCell.Delegate<UserDto> delegate) {
    ((IconActionCell<UserDto>) status.getCell()).setDelegate(delegate);
  }

  @Override
  public HasData<UserDto> getUsersTable() {
    return usersTable;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void clear() {
    renderUserRows((JsArray<UserDto>) JavaScriptObject.createArray());
    renderGroupRows((JsArray<GroupDto>) JavaScriptObject.createArray());
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public void renderUserRows(JsArray<UserDto> rows) {
    userDataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(rows)));
    indexTablePager.firstPage();
    userDataProvider.refresh();
    indexTablePager.setVisible(userDataProvider.getList().size() > indexTablePager.getPageSize());
  }

  @Override
  public void renderGroupRows(JsArray<GroupDto> rows) {
    groupDataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(rows)));
    indexTablePager.firstPage();
    groupDataProvider.refresh();
    indexTablePager.setVisible(groupDataProvider.getList().size() > indexTablePager.getPageSize());
  }

  @Override
  public void setBreadcrumbItems(List<BreadcrumbsBuilder.Item> items) {
    breadcrumbs.add(new BreadcrumbsBuilder().setItems(items).build());
  }

  @Override
  public HasClickHandlers getUsersLink() {
    return usersLink;
  }

  @Override
  public void showUsers() {
    usersLink.setActive(true);
    usersPanel.setVisible(true);
    groupsLink.setActive(false);
    groupsPanel.setVisible(false);
  }

  @Override
  public void showGroups() {
    usersLink.setActive(false);
    usersPanel.setVisible(false);
    groupsLink.setActive(true);
    groupsPanel.setVisible(true);
  }

  @Override
  public HasClickHandlers getGroupsLink() {
    return groupsLink;
  }

//  private final class UserColumns {
//
//    final Column<UserDto, String> name = new TextColumn<UserDto>() {
//
//      @Override
//      public String getValue(UserDto object) {
//        return object.getName();
//      }
//    };
//
//    final Column<UserDto, String> groups = new TextColumn<UserDto>() {
//
//      @Override
//      public String getValue(UserDto object) {
//        return object.getGroupsCount() > 0 ? object.getGroupsArray().join(", ") : "";
//      }
//    };

//    static final Column<UserDto, String> status = new Column<UserDto, String>(new IconActionCell<String>(IconType.MAP_MARKER, "", new IconActionCell.Delegate<String>() {
//      @Override
//      public void executeClick(NativeEvent event, String value) {
//        //To change body of implemented methods use File | Settings | File Templates.
//      }
//
//      @Override
//      public void executeMouseDown(NativeEvent event, String value) {
//        //To change body of implemented methods use File | Settings | File Templates.
//      }
//    })) {
//
//      @Override
//      public String getValue(UserDto user) {
//        return "image/16/bullet_black.png";
//      }
//    };

//    static final Column<UserDto, String> status = new ValueColumn<UserDto, String>(
//        new ClickableTextCell(new ClickableIconRenderer(IconType.DOWNLOAD)) {}) {
//
//      @Override
//      public String getValue(UserDto object) {
//        return "";
//      }
//    };

//  }

  private static final class GroupColumns {

    static final Column<GroupDto, String> name = new TextColumn<GroupDto>() {

      @Override
      public String getValue(GroupDto object) {
        return object.getName();
      }
    };

    static final Column<GroupDto, String> users = new TextColumn<GroupDto>() {

      @Override
      public String getValue(GroupDto object) {
        return object.getUsersCount() > 0 ? object.getUsersArray().join(", ") : "";
      }
    };

    static final ActionsColumn<GroupDto> actions = new ActionsColumn<GroupDto>(new ActionsProvider<GroupDto>() {

      private final String[] all = new String[] { ActionsColumn.DELETE_ACTION, PERMISSIONS_ACTION };

      private final String[] permissions = new String[] { PERMISSIONS_ACTION };

      @Override
      public String[] allActions() {
        return all;
      }

      public String[] permissionsActions() {
        return permissions;
      }

      @Override
      public String[] getActions(GroupDto value) {
        return value.getUsersCount() > 0 ? permissionsActions() : allActions();
      }
    });
  }
}
