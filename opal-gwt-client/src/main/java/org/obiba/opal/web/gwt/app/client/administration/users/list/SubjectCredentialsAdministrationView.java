/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.users.list;

import java.util.Collections;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.IconCell;
import org.obiba.opal.web.model.client.opal.GroupDto;
import org.obiba.opal.web.model.client.opal.SubjectCredentialsDto;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import static org.obiba.opal.web.gwt.app.client.administration.users.list.SubjectCredentialsAdministrationPresenter.Display;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class SubjectCredentialsAdministrationView extends ViewWithUiHandlers<SubjectCredentialsAdministrationUiHandlers>
    implements Display {

  interface Binder extends UiBinder<Widget, SubjectCredentialsAdministrationView> {}

  @UiField
  OpalSimplePager usersTablePager;

  @UiField
  CellTable<SubjectCredentialsDto> usersTable;

  @UiField
  OpalSimplePager groupsTablePager;

  @UiField
  CellTable<GroupDto> groupsTable;

  @UiField
  HasWidgets breadcrumbs;

  @UiField
  DropdownButton addUserButton;

  private final static Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<SubjectCredentialsDto> userDataProvider
      = new ListDataProvider<SubjectCredentialsDto>();

  private final ListDataProvider<GroupDto> groupDataProvider = new ListDataProvider<GroupDto>();

  @Inject
  public SubjectCredentialsAdministrationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    addUserButton.setText(translations.addUser());
    usersTable.setVisibleRange(0, 10);
    groupsTable.setVisibleRange(0, 10);
    configUserTable();
    configGroupTable();
  }

  private void configUserTable() {
    usersTable.addColumn(UserColumns.NAME, translations.userNameLabel());
    usersTable.addColumn(UserColumns.GROUPS, translations.userGroupsLabel());
    usersTable.addColumn(UserColumns.STATUS, translations.userStatusLabel());
    usersTable.addColumn(UserColumns.AUTHENTICATION, translations.userAuthenticationLabel());
    usersTable.addColumn(UserColumns.ACTIONS, translations.actionsLabel());
    usersTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
    usersTablePager.setDisplay(usersTable);
    userDataProvider.addDataDisplay(usersTable);
  }

  private void configGroupTable() {
    groupsTable.addColumn(GroupColumns.NAME, translations.groupNameLabel());
    groupsTable.addColumn(GroupColumns.USERS, translations.groupUsersLabel());
    groupsTable.addColumn(GroupColumns.ACTIONS, translations.actionsLabel());
    groupsTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
    groupsTablePager.setDisplay(groupsTable);
    groupDataProvider.addDataDisplay(groupsTable);
  }

  @UiHandler("addUser")
  public void onAddUser(ClickEvent event) {
    getUiHandlers().onAddUserWithPassword();
  }

  @UiHandler("addApplication")
  public void onAddApplication(ClickEvent event) {
    getUiHandlers().onAddUserWithCertificate();
  }

  @Override
  public void clear() {
    renderUserRows(Collections.<SubjectCredentialsDto>emptyList());
    renderGroupRows(Collections.<GroupDto>emptyList());
  }

  @Override
  public void renderUserRows(List<SubjectCredentialsDto> rows) {
    renderRows(rows, userDataProvider, usersTablePager);
  }

  @Override
  public void renderGroupRows(List<GroupDto> rows) {
    renderRows(rows, groupDataProvider, groupsTablePager);
  }

  private <T> void renderRows(List<T> rows, ListDataProvider<T> dataProvider, OpalSimplePager pager) {
    dataProvider.setList(rows);
    pager.firstPage();
    dataProvider.refresh();
    pager.setPagerVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public HasActionHandler<SubjectCredentialsDto> getSubjectCredentialActions() {
    return UserColumns.ACTIONS;
  }

  @Override
  public HasActionHandler<GroupDto> getGroupsActions() {
    return GroupColumns.ACTIONS;
  }

  private static final class UserColumns {

    static final Column<SubjectCredentialsDto, String> NAME = new TextColumn<SubjectCredentialsDto>() {

      @Override
      public String getValue(SubjectCredentialsDto subjectCredentialsDto) {
        return subjectCredentialsDto.getName();
      }
    };

    static final Column<SubjectCredentialsDto, String> GROUPS = new TextColumn<SubjectCredentialsDto>() {

      @Override
      public String getValue(SubjectCredentialsDto subjectCredentialsDto) {
        return subjectCredentialsDto.getGroupsCount() > 0 ? subjectCredentialsDto.getGroupsArray().join(", ") : "";
      }
    };

    static final Column<SubjectCredentialsDto, Boolean> STATUS = new Column<SubjectCredentialsDto, Boolean>(
        new IconCell<Boolean>() {
          @Override
          public IconType getIconType(Boolean value) {
            return value ? IconType.OK : IconType.REMOVE;
          }
        }) {
      @Override
      public Boolean getValue(SubjectCredentialsDto subjectCredentialsDto) {
        return subjectCredentialsDto.getEnabled();
      }
    };

    static final Column<SubjectCredentialsDto, String> AUTHENTICATION = new TextColumn<SubjectCredentialsDto>() {
      @Override
      public String getValue(SubjectCredentialsDto subjectCredentialsDto) {
        return translations.authenticationTypeMap().get(subjectCredentialsDto.getAuthenticationType().getName());
      }
    };

    static final ActionsColumn<SubjectCredentialsDto> ACTIONS = new ActionsColumn<SubjectCredentialsDto>(
        new ActionsProvider<SubjectCredentialsDto>() {

          @Override
          public String[] allActions() {
            return new String[] { EDIT_ACTION, REMOVE_ACTION, ENABLE_ACTION, DISABLE_ACTION };
          }

          @Override
          public String[] getActions(SubjectCredentialsDto value) {
            if(value.getEnabled()) {
              return new String[] { EDIT_ACTION, REMOVE_ACTION, DISABLE_ACTION };
            }
            return new String[] { EDIT_ACTION, REMOVE_ACTION, ENABLE_ACTION };
          }
        });

  }

  private static final class GroupColumns {

    static final Column<GroupDto, String> NAME = new TextColumn<GroupDto>() {

      @Override
      public String getValue(GroupDto object) {
        return object.getName();
      }
    };

    static final Column<GroupDto, String> USERS = new TextColumn<GroupDto>() {

      @Override
      public String getValue(GroupDto object) {
        return object.getSubjectCredentialsCount() > 0 ? object.getSubjectCredentialsArray().join(", ") : "";
      }
    };

    static final ActionsColumn<GroupDto> ACTIONS = new ActionsColumn<GroupDto>(new ActionsProvider<GroupDto>() {

      @Override
      public String[] allActions() {
        return new String[] { REMOVE_ACTION };
      }

      @Override
      public String[] getActions(GroupDto value) {
        return allActions();
      }
    });
  }
}
