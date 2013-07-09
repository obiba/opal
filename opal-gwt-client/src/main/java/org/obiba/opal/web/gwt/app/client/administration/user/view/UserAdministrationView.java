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

import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.model.client.opal.GroupDto;
import org.obiba.opal.web.model.client.opal.UserDto;

import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewImpl;

public class UserAdministrationView extends ViewImpl implements UserAdministrationPresenter.Display {

  @UiTemplate("UserAdministrationView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, UserAdministrationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget uiWidget;

  @UiField
  DropdownButton actionsDropdown;

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
  Table<UserDto> usersTable;

  @UiField
  Table<GroupDto> groupsTable;

  @UiField
  Panel breadcrumbs;

  private final ListDataProvider<UserDto> userDataProvider = new ListDataProvider<UserDto>();

  private final ListDataProvider<GroupDto> groupDataProvider = new ListDataProvider<GroupDto>();

//  private final CheckboxColumn<UserDto> checkboxColumn;

//  ActionsIndexColumn<UserDto> actionsColumn = new ActionsIndexColumn<UserDto>(
//      new ActionsProvider<UserDto>() {
//
//        private final String[] all = new String[] { CLEAR_ACTION, INDEX_ACTION };
//
//        @Override
//        public String[] allActions() {
//          return all;
//        }
//
//        @Override
//        public String[] getActions(UserDto value) {
//          return allActions();
//        }
//      });

  public UserAdministrationView() {
    uiWidget = uiBinder.createAndBindUi(this);
    indexTablePager.setDisplay(usersTable);

//    checkboxColumn = new CheckboxColumn<TableIndexStatusDto>(new TableIndexStatusDtoDisplay());
//    checkboxColumn.setActionHandler(new ActionHandler<Integer>() {
//      @Override
//      public void doAction(Integer object, String actionName) {
//        selectAllAlert.setVisible(object > 0);
//      }
//    });
    usersTable.addColumn(UserColumns.name, translations.userNameLabel());
    usersTable.addColumn(UserColumns.groups, translations.userGroupsLabel());
    usersTable.addColumn(UserColumns.status, translations.userStatusLabel());
//    indexTable.addColumn(actionsColumn, translations.actionsLabel());
    usersTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
//    indexTable.setColumnWidth(checkboxColumn, 1, Style.Unit.PX);

    userDataProvider.addDataDisplay(usersTable);

    /*Groups*/
    groupsTable.addColumn(GroupColumns.name, translations.groupNameLabel());
    groupsTable.addColumn(GroupColumns.users, translations.groupUsersLabel());
    groupDataProvider.addDataDisplay(groupsTable);
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
//    checkboxColumn.getSelectionModel().clear();
//    selectAllAlert.setVisible(false);
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

  private static final class UserColumns {

    static final Column<UserDto, String> name = new TextColumn<UserDto>() {

      @Override
      public String getValue(UserDto object) {
        return object.getName();
      }
    };

    static final Column<UserDto, String> groups = new TextColumn<UserDto>() {

      @Override
      public String getValue(UserDto object) {
        return object.getGroupsCount() > 0 ? object.getGroupsArray().join(", ") : "";
      }
    };

    static final Column<UserDto, String> status = new TextColumn<UserDto>() {

      @Override
      public String getValue(UserDto object) {
        return object.getEnabled() + "";
      }
    };

//    static final Column<TableIndexStatusDto, String> status = new Column<TableIndexStatusDto, String>(
//        new IndexStatusImageCell()) {
//
//      @Override
//      public String getValue(TableIndexStatusDto tableIndexStatusDto) {
//        return IndexStatusImageCell.getSrc(tableIndexStatusDto);
//      }
//    };
  }

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
  }
}
