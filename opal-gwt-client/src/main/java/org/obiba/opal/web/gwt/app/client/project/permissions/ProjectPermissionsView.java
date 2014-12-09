/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.permissions;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.PlaceRequestCell;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.Acl;
import org.obiba.opal.web.model.client.opal.Subject;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.FluidRow;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavHeader;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavList;
import com.github.gwtbootstrap.client.ui.NavWidget;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class ProjectPermissionsView extends ViewWithUiHandlers<ProjectPermissionsUiHandlers>
    implements ProjectPermissionsPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectPermissionsView> {}

  @UiField
  NavList users;

  @UiField
  NavList groups;

  @UiField
  Heading principal;

  @UiField
  Button deleteAll;

  @UiField
  OpalSimplePager tablePager;

  @UiField
  Table<Acl> permissionsTable;

  @UiField
  FluidRow permissionsRow;

  @UiField
  Paragraph emptyHelp;

  private static final int SORTABLE_COLUMN_TYPE = 1;

  private final static Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<Acl> permissionsDataProvider = new ListDataProvider<Acl>();

  private Subject currentSubject;

  private TypeColumn typeColumn;

  private Comparator<Acl> resourceTypeComparator;

  private ProjectPermissionsPresenter.NodeToTypeMapper nodeToTypeMapper;

  private final PlaceManager placeManager;

  @Inject
  public ProjectPermissionsView(Binder uiBinder, PlaceManager placeManager) {
    initWidget(uiBinder.createAndBindUi(this));
    this.placeManager = placeManager;
  }

  @Override
  public void setData(@Nonnull List<Subject> subjects) {
    renderUsersAndGroups(subjects);
    permissionsRow.setVisible(subjects.size() > 0);
    emptyHelp.setVisible(!permissionsRow.isVisible());
  }

  @Override
  public void initializeTable(ProjectPermissionsPresenter.NodeToPlaceMapper nodeToPlaceMapper,
      ProjectPermissionsPresenter.NodeNameFormatter formatter,
      ProjectPermissionsPresenter.NodeToTypeMapper nodeToTypeMapper, Comparator<Acl> resourceTypeComparator) {
    this.nodeToTypeMapper = nodeToTypeMapper;
    this.resourceTypeComparator = resourceTypeComparator;

    tablePager.setDisplay(permissionsTable);
    permissionsTable.addColumn(new ResourceColumn(nodeToPlaceMapper, formatter), translations.resourceLabel());
    permissionsTable.addColumn(ProjectPermissionColumns.PERMISSION, translations.permissionLabel());
    permissionsTable.addColumn(ProjectPermissionColumns.ACTIONS, translations.actionsLabel());
    initTypeColumn();

    permissionsDataProvider.addDataDisplay(permissionsTable);

  }

  @Override
  public void setSubjectData(Subject subject, List<Acl> subjectAcls) {
    currentSubject = subject;
    principal.setText(subject.getPrincipal());
    renderSubjectsPermissionTable(subjectAcls);
    activateSubjectNavLink(subject);
  }

  @Override
  public HasActionHandler<Acl> getActions() {
    return ProjectPermissionColumns.ACTIONS;
  }

  @Override
  public HasAuthorization getDeleteAllAuthorizer() {
    return new WidgetAuthorizer(deleteAll);
  }

  @SuppressWarnings("UnusedParameters")
  @UiHandler("deleteAll")
  public void onDeleteAllClicked(ClickEvent event) {
    getUiHandlers().deleteAllPermissions(currentSubject);
  }

  //
  // Private methods
  //

  private void renderSubjectsPermissionTable(List<Acl> subjectAcls) {
    permissionsDataProvider.setList(subjectAcls);
    tablePager.firstPage();
    permissionsDataProvider.refresh();
    tablePager.setPagerVisible(permissionsDataProvider.getList().size() > tablePager.getPageSize());

    permissionsTable.removeColumn(typeColumn);
    initTypeColumn();

    ColumnSortEvent.fire(permissionsTable, permissionsTable.getColumnSortList());
  }

  private void initTypeColumn() {
    typeColumn = new TypeColumn(nodeToTypeMapper);
    permissionsTable.insertColumn(1, typeColumn, translations.typeLabel());
    ColumnSortEvent.ListHandler<Acl> typeSortHandler = new ColumnSortEvent.ListHandler<Acl>(
        permissionsDataProvider.getList());
    typeSortHandler.setComparator(typeColumn, resourceTypeComparator);
    permissionsTable.getColumnSortList().push(typeColumn);
    permissionsTable.addColumnSortHandler(typeSortHandler);
    permissionsTable.getHeader(SORTABLE_COLUMN_TYPE).setHeaderStyleNames("addColumnSortHandler");
  }

  private void activateSubjectNavLink(Subject subject) {
    NavList container = Subject.SubjectType.USER.isSubjectType(subject.getType()) ? users : groups;
    for(int i = 0; i < container.getWidgetCount(); i++) {
      if(container.getWidget(i) instanceof NavLink) {
        NavLink link = (NavLink) container.getWidget(i);
        // must trim because NavLink adds some spaces
        if(link.getText().trim().equals(subject.getPrincipal())) {
          link.setActive(true);
          return;
        }
      }
    }
  }

  private void renderUsersAndGroups(@Nonnull Iterable<Subject> subjects) {
    users.clear();
    groups.clear();

    for(Subject aSubject : subjects) {
      createSubjectNavLink(aSubject);
    }

    users.setVisible(users.getWidgetCount() > 0);
    groups.setVisible(groups.getWidgetCount() > 0);

    users.insert(new NavHeader(translations.subjectTypeUsers()), 0);
    groups.insert(new NavHeader(translations.subjectTypeGroups()), 0);
  }

  private void createSubjectNavLink(Subject subject) {
    NavList container = Subject.SubjectType.USER.isSubjectType(subject.getType()) ? users : groups;
    NavLink link = new NavLink(subject.getPrincipal());
    link.addClickHandler(new SubjectNavLinkClickHandler(subject, link));

    container.add(link);
  }

  private class ResourceColumn extends Column<Acl, Acl> {

    private ResourceColumn(final ProjectPermissionsPresenter.NodeToPlaceMapper converter,
        final ProjectPermissionsPresenter.NodeNameFormatter formatter) {
      super(new PlaceRequestCell<Acl>(placeManager) {
        @Override
        public PlaceRequest getPlaceRequest(Acl value) {
          return converter.map(value);
        }

        @Override
        public String getText(Acl value) {
          return formatter.format(value);
        }
      });
    }

    @Override
    public Acl getValue(Acl object) {
      return object;
    }
  }

  private static class TypeColumn extends TextColumn<Acl> {

    private final ProjectPermissionsPresenter.NodeToTypeMapper mapper;

    private TypeColumn(ProjectPermissionsPresenter.NodeToTypeMapper mapper) {
      this.mapper = mapper;
      setSortable(true);
      setDefaultSortAscending(true);
    }

    @Override
    public String getValue(Acl acl) {
      return mapper.map(acl);
    }
  }

  private static final class ProjectPermissionColumns {

    static final Column<Acl, String> PERMISSION = new TextColumn<Acl>() {

      @Override
      public String getValue(Acl acl) {
        return translations.permissionMap().get(acl.getActions(0));
      }
    };

    static final ActionsColumn<Acl> ACTIONS = new ActionsColumn<Acl>(new ActionsProvider<Acl>() {

      @Override
      public String[] allActions() {
        return new String[] { ActionsColumn.REMOVE_ACTION };
      }

      @Override
      public String[] getActions(Acl value) {
        return allActions();
      }
    });
  }

  private class SubjectNavLinkClickHandler implements ClickHandler {
    private final Subject subject;

    private final NavLink link;

    SubjectNavLinkClickHandler(Subject subject, NavLink link) {
      this.subject = subject;
      this.link = link;
    }

    @Override
    public void onClick(ClickEvent event) {
      getUiHandlers().selectSubject(subject);
      unActivateAll(users);
      unActivateAll(groups);
      link.setActive(true);
    }

    private void unActivateAll(IndexedPanel container) {
      for(int i = 0; i < container.getWidgetCount(); i++) {
        if(container.getWidget(i) instanceof NavLink) {
          ((NavWidget) container.getWidget(i)).setActive(false);
        }
      }
    }
  }
}