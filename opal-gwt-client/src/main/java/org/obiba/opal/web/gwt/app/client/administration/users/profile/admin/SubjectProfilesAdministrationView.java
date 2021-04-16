/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.users.profile.admin;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ValueRenderer;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.SubjectProfileDto;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

public class SubjectProfilesAdministrationView extends ViewImpl
    implements SubjectProfilesAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, SubjectProfilesAdministrationView> {}

  @UiField
  OpalSimplePager profilesPager;

  @UiField
  CellTable<SubjectProfileDto> profilesTable;

  @UiField
  HasWidgets breadcrumbs;

  private final static Translations translations = GWT.create(Translations.class);

  private final ListDataProvider<SubjectProfileDto> profilesDataProvider = new ListDataProvider<SubjectProfileDto>();

  private ActionsColumn<SubjectProfileDto> actionsColumn;

  @Inject
  public SubjectProfilesAdministrationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    configTable();
  }

  private void configTable() {
    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        return object.getPrincipal();
      }

    }, translations.userNameLabel());
    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        return object.getRealm();
      }

    }, translations.realmLabel());

    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        String groupsTxt = "";
        if (object.getGroupsCount()>0)
          groupsTxt = Joiner.on(", ").join(JsArrays.toIterable(object.getGroupsArray()));
        return groupsTxt;
      }

    }, translations.userGroupsLabel());
    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        return ValueRenderer.DATETIME.render(object.getCreated());
      }

    }, translations.createdLabel());
    profilesTable.addColumn(new TextColumn<SubjectProfileDto>() {

      @Override
      public String getValue(SubjectProfileDto object) {
        return Moment.create(object.getLastUpdate()).fromNow();//ValueRenderer.DATETIME.render(object.getLastUpdate());
      }

    }, translations.lastUpdatedLabel());
    profilesTable
        .addColumn(actionsColumn = new ActionsColumn<SubjectProfileDto>(new ActionsProvider<SubjectProfileDto>() {

          @Override
          public String[] allActions() {
            return new String[] { REMOVE_ACTION };
          }

          @Override
          public String[] getActions(SubjectProfileDto value) {
            return new String[] { REMOVE_ACTION };
          }
        }), translations.actionsLabel());

    profilesTable.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
    profilesPager.setDisplay(profilesTable);
    profilesDataProvider.addDataDisplay(profilesTable);
  }

  @Override
  public void renderProfiles(List<SubjectProfileDto> rows) {
    renderRows(rows, profilesDataProvider, profilesPager);
  }

  @Override
  public HasActionHandler<SubjectProfileDto> getActions() {
    return actionsColumn;
  }

  @Override
  public void clear() {
    renderProfiles(Collections.<SubjectProfileDto>emptyList());
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
}
