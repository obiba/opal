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

import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ProjectResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ProjectResourcePermissionsUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.Subject;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Controls;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ProjectResourcePermissionsView extends ViewWithUiHandlers<ProjectResourcePermissionsUiHandlers>
    implements ProjectResourcePermissionsPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectResourcePermissionsView> {}

  @UiField
  Controls users;

  @UiField
  Controls groups;

  @UiField
  Heading principal;

  @UiField
  SimplePager tablePager;

  @UiField
  Table permissionsTable;

  @UiField
  Button deleteAll;

  private final static Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<Acl> permissionsDataProvider = new ListDataProvider<Acl>();

  private Subject subject;

  @Inject
  public ProjectResourcePermissionsView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    initPermissionTable();
  }

  @Override
  public void setData(@Nonnull List<Subject> subjects) {
    renderUsersAndGroups(subjects);
  }

  @Override
  public void setSubjectData(Subject subject, List<Acl> subjectAcls) {
    this.subject = subject;
    principal.setText(subject.getPrincipal());
    renderSubjectsPermissionTable(subjectAcls);
  }

  @Override
  public HasActionHandler<Acl> getActions() {
    return ProjectPermissionColumns.ACTIONS;
  }

  @UiHandler("deleteAll")
  public void onDeleteAllClicked(ClickEvent event) {
    getUiHandlers().deleteAllPermissions(subject);
  }

  private void initPermissionTable() {
    tablePager.setDisplay(permissionsTable);
    permissionsTable.addColumn(ProjectPermissionColumns.RESOURCE, translations.resourceLabel());
    permissionsTable.addColumn(ProjectPermissionColumns.PERMISSION, translations.permissionLabel());
    permissionsTable.addColumn(ProjectPermissionColumns.ACTIONS, translations.actionsLabel());
    permissionsDataProvider.addDataDisplay(permissionsTable);
  }

  private void renderSubjectsPermissionTable(List<Acl> subjectAcls) {
    permissionsDataProvider.setList(subjectAcls);
    tablePager.firstPage();
    permissionsDataProvider.refresh();
//    tablePager.setPageSize(0);
    tablePager.setVisible(permissionsDataProvider.getList().size() > tablePager.getPageSize());
  }

  private void renderUsersAndGroups(@Nonnull List<Subject> subjects) {
    users.clear();
    groups.clear();

    for (Subject aSubject : subjects) {
      createSubjectNavLink(aSubject);
    }
  }

  private void createSubjectNavLink(final Subject subject) {
    Controls container = Subject.SubjectType.SUBJECT_CREDENTIALS.isSubjectType(subject.getType()) ? users : groups;
    NavLink link = new NavLink(subject.getPrincipal());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getUiHandlers().selectSubject(subject);
      }
    });

    container.add(link);
  }

  private static final class ProjectPermissionColumns {

    static final Column<Acl, String> RESOURCE = new TextColumn<Acl>() {

      @Override
      public String getValue(Acl acl) {
        return acl.getResource();
      }
    };

    static final Column<Acl, String> PERMISSION = new TextColumn<Acl>() {

      @Override
      public String getValue(Acl acl) {
      return translations.permissionMap().get(acl.getActions(0));
      }
    };

    static final ActionsColumn<Acl> ACTIONS = new ActionsColumn<Acl>(new ActionsProvider<Acl>() {

      @Override
      public String[] allActions() {
        return new String[] { ActionsColumn.DELETE_ACTION };
      }

      @Override
      public String[] getActions(Acl value) {
        return allActions();
      }
    });
  }
}