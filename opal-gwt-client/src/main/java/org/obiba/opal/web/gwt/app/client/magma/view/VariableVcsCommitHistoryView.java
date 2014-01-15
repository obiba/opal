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
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariableVcsCommitHistoryPresenter;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.gwtplatform.mvp.client.UiHandlers;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class VariableVcsCommitHistoryView extends ViewWithUiHandlers<UiHandlers>
    implements VariableVcsCommitHistoryPresenter.Display {

  interface Binder extends UiBinder<Widget, VariableVcsCommitHistoryView> {}

  interface VariableGitHistoryViewUiBinder extends UiBinder<Widget, VariableVcsCommitHistoryView> {}

  private static final VariableGitHistoryViewUiBinder uiBinder = GWT.create(VariableGitHistoryViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

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
    commitInfoDataProvider.setList(JsArrays.toList(commitInfos));
    commitInfoTablePager.firstPage();
    commitInfoDataProvider.refresh();
    commitInfoTablePager.setVisible(commitInfoDataProvider.getList().size() > commitInfoTablePager.getPageSize());
  }

  @Override
  public HasActionHandler<VcsCommitInfoDto> getActions() {
    return Columns.ACTIONS;
  }

  private void createTableColumns() {
    commitInfoTable.addColumn(Columns.DATE, translations.commitInfoMap().get("Date"));
    commitInfoTable.addColumn(Columns.AUTHOR, translations.commitInfoMap().get("Author"));
    commitInfoTable.addColumn(Columns.COMMENT, translations.commitInfoMap().get("Comment"));
    commitInfoTable.addColumn(Columns.ACTIONS, translations.actionsLabel());
    commitInfoDataProvider.addDataDisplay(commitInfoTable);
    commitInfoTable.setEmptyTableWidget(new Label(translations.noVcsCommitHistoryAvailable()));
  }

  private static final class Columns {

    static final Column<VcsCommitInfoDto, String> DATE = new TextColumn<VcsCommitInfoDto>() {

      @Override
      public String getValue(VcsCommitInfoDto commitInfo) {
        Moment m = Moment.create(commitInfo.getDate());
        return TranslationsUtils
            .replaceArguments(translations.momentWithAgo(), m.format(FormatType.MONTH_NAME_TIME_SHORT), m.fromNow());
      }
    };

    static final Column<VcsCommitInfoDto, String> AUTHOR = new TextColumn<VcsCommitInfoDto>() {

      @Override
      public String getValue(VcsCommitInfoDto commitInfo) {
        return commitInfo.getAuthor();
      }
    };

    static final Column<VcsCommitInfoDto, String> COMMENT = new TextColumn<VcsCommitInfoDto>() {

      @Override
      public String getValue(VcsCommitInfoDto commitInfo) {
        return commitInfo.getComment();
      }
    };

    static final ActionsColumn<VcsCommitInfoDto> ACTIONS = new ActionsColumn<VcsCommitInfoDto>(
        new ActionsProvider<VcsCommitInfoDto>() {

          @Override
          public String[] allActions() {
            return new String[] { DIFF_ACTION, DIFF_CURRENT_ACTION, ActionsColumn.EDIT_ACTION };
          }

          @Override
          public String[] getActions(VcsCommitInfoDto value) {
            return value.getIsCurrent() ? getHeadOnlyActions() : allActions();
          }

          private String[] getHeadOnlyActions() {
            return new String[] { DIFF_ACTION };
          }
        });

  }

}
