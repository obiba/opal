/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import javax.inject.Inject;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariableVcsCommitHistoryPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariableVcsCommitHistoryUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class VariableVcsCommitHistoryView extends ViewWithUiHandlers<VariableVcsCommitHistoryUiHandlers> implements
    VariableVcsCommitHistoryPresenter.Display {

  interface Binder extends UiBinder<Widget, VariableVcsCommitHistoryView> {}

  interface VariableGitHistoryViewUiBinder extends UiBinder<Widget, VariableVcsCommitHistoryView> {}

  private static final VariableGitHistoryViewUiBinder uiBinder = GWT.create(VariableGitHistoryViewUiBinder.class);

  private static final Translations translations = com.google.gwt.core.shared.GWT.create(Translations.class);

  @UiField
  CellTable<VcsCommitInfoDto> commitInfoTable;

  @UiField
  SimplePager commitInfoTablePager;


  private final ListDataProvider<VcsCommitInfoDto> commitInfoDataProvider = new ListDataProvider<VcsCommitInfoDto>();

  @Inject
  public VariableVcsCommitHistoryView() {
    initWidget(uiBinder.createAndBindUi(this));
    createTableColumns();
    commitInfoTablePager.setDisplay(commitInfoTable);
  }

  @Override
  public void setData(JsArray<VcsCommitInfoDto> commitInfos) {
    commitInfoDataProvider.setList(JsArrays.toList(JsArrays.toSafeArray(commitInfos)));
    commitInfoTablePager.firstPage();
    commitInfoDataProvider.refresh();
    commitInfoTablePager.setVisible(commitInfoDataProvider.getList().size() > commitInfoTablePager.getPageSize());
  }

  private void createTableColumns() {
    commitInfoTable.addColumn(createDateColumn(), translations.commitInfoMap().get("date"));
    commitInfoTable.addColumn(createAuthorColumn(), translations.commitInfoMap().get("author"));
    commitInfoTable.addColumn(createCommentColumn(), translations.commitInfoMap().get("comment"));
    commitInfoDataProvider.addDataDisplay(commitInfoTable);
    commitInfoTable.setEmptyTableWidget(new Label(translations.noVcsCommitHistoryAvailable()));
  }

  private Column<VcsCommitInfoDto, String> createDateColumn() {
    return new TextColumn<VcsCommitInfoDto>() {

      @Override
      public String getValue(VcsCommitInfoDto commitInfo) {
        return Moment.create(commitInfo.getDate()).fromNow();
      }
    };
  }

  private Column<VcsCommitInfoDto, String> createAuthorColumn() {

    return new TextColumn<VcsCommitInfoDto>() {

      @Override
      public String getValue(VcsCommitInfoDto commitInfo) {
        return commitInfo.getAuthor();
      }
    };
  }

  private Column<VcsCommitInfoDto, String> createCommentColumn() {

    ClickableColumn<VcsCommitInfoDto> column = new ClickableColumn<VcsCommitInfoDto>() {

      @Override
      public String getValue(VcsCommitInfoDto commitInfo) {
        return commitInfo.getComment();
      }
    };

    column.setFieldUpdater(new FieldUpdater<VcsCommitInfoDto, String>() {
      @Override
      public void update(int index, VcsCommitInfoDto dto, String value) {
        getUiHandlers().showCommitInfo(dto);
      }
    });

    return column;
  }

}
