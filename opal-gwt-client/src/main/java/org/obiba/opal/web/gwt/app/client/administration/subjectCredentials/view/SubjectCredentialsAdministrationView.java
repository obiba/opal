/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.view;

import java.util.Collections;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.presenter.SubjectCredentialsAdministrationUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.IconCell;
import org.obiba.opal.web.model.client.opal.GroupDto;
import org.obiba.opal.web.model.client.opal.SubjectCredentialsDto;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.constants.IconType;
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

import static org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.presenter.SubjectCredentialsAdministrationPresenter.Display;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.DELETE_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;

public class SubjectCredentialsAdministrationView extends ViewWithUiHandlers<SubjectCredentialsAdministrationUiHandlers>
    implements Display {

  private final Translations translations;

  interface Binder extends UiBinder<Widget, SubjectCredentialsAdministrationView> {}

  @UiField
  SimplePager usersTablePager;

  @UiField
  CellTable<SubjectCredentialsDto> usersTable;

  @UiField
  SimplePager applicationsTablePager;

  @UiField
  CellTable<SubjectCredentialsDto> applicationsTable;

  @UiField
  SimplePager groupsTablePager;

  @UiField
  CellTable<GroupDto> groupsTable;

  @UiField
  HasWidgets breadcrumbs;

  private final ListDataProvider<SubjectCredentialsDto> userDataProvider
      = new ListDataProvider<SubjectCredentialsDto>();

  private final ListDataProvider<SubjectCredentialsDto> applicationDataProvider
      = new ListDataProvider<SubjectCredentialsDto>();

  private final ListDataProvider<GroupDto> groupDataProvider = new ListDataProvider<GroupDto>();

  @Inject
  public SubjectCredentialsAdministrationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    usersTable.setVisibleRange(0, 10);
    applicationsTable.setVisibleRange(0, 10);
    groupsTable.setVisibleRange(0, 10);
    configSubjectCredentialsTable(userDataProvider, usersTable, usersTablePager);
    configSubjectCredentialsTable(applicationDataProvider, applicationsTable, applicationsTablePager);
    configGroupTable();
  }

  private void configSubjectCredentialsTable(ListDataProvider<SubjectCredentialsDto> dataProvider,
      CellTable<SubjectCredentialsDto> table, SimplePager pager) {
    pager.setDisplay(table);
    table.addColumn(SubjectCredentialColumns.NAME, translations.userNameLabel());
    table.addColumn(SubjectCredentialColumns.GROUPS, translations.userGroupsLabel());
    table.addColumn(SubjectCredentialColumns.STATUS, translations.userStatusLabel());
    table.addColumn(SubjectCredentialColumns.ACTIONS, translations.actionsLabel());
    table.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
    dataProvider.addDataDisplay(table);
  }

  private void configGroupTable() {
    groupsTable.addColumn(GroupColumns.NAME, translations.groupNameLabel());
    groupsTable.addColumn(GroupColumns.USERS, translations.groupUsersLabel());
    groupsTable.addColumn(GroupColumns.ACTIONS, translations.actionsLabel());
    groupDataProvider.addDataDisplay(groupsTable);
  }

  @UiHandler("addUser")
  public void onAddUser(ClickEvent event) {
    getUiHandlers().onAddUser();
  }

  @UiHandler("addApplication")
  public void onAddApplication(ClickEvent event) {
    getUiHandlers().onAddApplication();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void clear() {
    renderUserRows(Collections.<SubjectCredentialsDto>emptyList());
    renderApplicationRows(Collections.<SubjectCredentialsDto>emptyList());
    renderGroupRows(Collections.<GroupDto>emptyList());
  }

  @Override
  public void renderUserRows(List<SubjectCredentialsDto> rows) {
    userDataProvider.setList(rows);
    usersTablePager.firstPage();
    userDataProvider.refresh();
    usersTablePager.setVisible(userDataProvider.getList().size() > usersTablePager.getPageSize());
  }

  @Override
  public void renderApplicationRows(List<SubjectCredentialsDto> rows) {
    applicationDataProvider.setList(rows);
    applicationsTablePager.firstPage();
    applicationDataProvider.refresh();
    applicationsTablePager.setVisible(applicationDataProvider.getList().size() > applicationsTablePager.getPageSize());
  }

  @Override
  public void renderGroupRows(List<GroupDto> rows) {
    groupDataProvider.setList(rows);
    groupsTablePager.firstPage();
    groupDataProvider.refresh();
    groupsTablePager.setVisible(groupDataProvider.getList().size() > groupsTablePager.getPageSize());
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public HasActionHandler<SubjectCredentialsDto> getSubjectCredentialActions() {
    return SubjectCredentialColumns.ACTIONS;
  }

  @Override
  public HasActionHandler<GroupDto> getGroupsActions() {
    return GroupColumns.ACTIONS;
  }

  private static final class SubjectCredentialColumns {

    static final Column<SubjectCredentialsDto, String> NAME = new TextColumn<SubjectCredentialsDto>() {

      @Override
      public String getValue(SubjectCredentialsDto object) {
        return object.getName();
      }
    };

    static final Column<SubjectCredentialsDto, String> GROUPS = new TextColumn<SubjectCredentialsDto>() {

      @Override
      public String getValue(SubjectCredentialsDto object) {
        return object.getGroupsCount() > 0 ? object.getGroupsArray().join(", ") : "";
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
      public Boolean getValue(SubjectCredentialsDto object) {
        return object.getEnabled();
      }
    };

    static final ActionsColumn<SubjectCredentialsDto> ACTIONS = new ActionsColumn<SubjectCredentialsDto>(
        new ActionsProvider<SubjectCredentialsDto>() {

          @Override
          public String[] allActions() {
            return new String[] { EDIT_ACTION, DELETE_ACTION, ENABLE_ACTION, DISABLE_ACTION };
          }

          @Override
          public String[] getActions(SubjectCredentialsDto value) {
            if(value.getEnabled()) {
              return new String[] { EDIT_ACTION, DELETE_ACTION, DISABLE_ACTION };
            }
            return new String[] { EDIT_ACTION, DELETE_ACTION, ENABLE_ACTION };
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
        return object.getUsersCount() > 0 ? object.getUsersArray().join(", ") : "";
      }
    };

    static final ActionsColumn<GroupDto> ACTIONS = new ActionsColumn<GroupDto>(new ActionsProvider<GroupDto>() {

      @Override
      public String[] allActions() {
        return new String[] { DELETE_ACTION };
      }

      @Override
      public String[] getActions(GroupDto value) {
        return allActions();
      }
    });
  }
}
