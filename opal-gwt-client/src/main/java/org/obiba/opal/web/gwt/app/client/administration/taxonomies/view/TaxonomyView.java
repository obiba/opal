/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsSortableColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.LocaleTextColumn;
import org.obiba.opal.web.gwt.datetime.client.FormatType;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.VcsCommitInfoDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class TaxonomyView extends ViewWithUiHandlers<TaxonomyUiHandlers> implements TaxonomyPresenter.Display {

  interface Binder extends UiBinder<Widget, TaxonomyView> {}

  private final TranslationMessages translationMessages;

  private static final Translations translations = GWT.create(Translations.class);

  private static final int DEFAULT_PAGE_SIZE = 5;

  private static final int DEFAULT_HISTORY_CHANGE_PAGE_SIZE = 10;

  @UiField
  IconAnchor edit;

  @UiField
  Button remove;

  @UiField
  Button download;

  @UiField
  Button addVocabulary;

  @UiField
  IconAnchor saveChanges;

  @UiField
  IconAnchor resetChanges;

  @UiField
  Alert saveChangesAlert;

  @UiField
  Panel detailsPanel;

  @UiField
  Heading taxonomyName;

  @UiField
  Panel taxonomyPanel;

  @UiField
  Label author;

  @UiField
  Label license;

  @UiField
  Anchor licenseLink;

  @UiField
  Panel titlePanel;

  @UiField
  Panel descriptionPanel;

  @UiField
  Table<VocabularyDto> table;

  @UiField
  OpalSimplePager pager;

  @UiField
  TextBoxClearable filter;

  @UiField
  Panel commitInfoPanel;

  @UiField
  CellTable<VcsCommitInfoDto> commitInfoTable;

  @UiField
  OpalSimplePager commitInfoTablePager;

  private final ListDataProvider<VcsCommitInfoDto> commitInfoDataProvider = new ListDataProvider<VcsCommitInfoDto>();

  private final ListDataProvider<VocabularyDto> dataProvider = new ListDataProvider<VocabularyDto>();

  private ActionsColumn<VocabularyDto> actions;

  @Inject
  public TaxonomyView(Binder uiBinder, TranslationMessages translationMessages) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translationMessages = translationMessages;
    initializeVocabulariesTable();
    initializeFilter();
    createTableColumns();
    commitInfoTablePager.setDisplay(commitInfoTable);
  }

  @Override
  public void setData(JsArray<VcsCommitInfoDto> commitInfos) {
    commitInfoDataProvider.setList(JsArrays.toList(commitInfos));
    commitInfoTablePager.firstPage();
    commitInfoDataProvider.refresh();
    commitInfoTablePager.setPageSize(DEFAULT_HISTORY_CHANGE_PAGE_SIZE);
    commitInfoTablePager.setPagerVisible(commitInfoDataProvider.getList().size() > DEFAULT_HISTORY_CHANGE_PAGE_SIZE);
  }

  @Override
  public HasActionHandler<VcsCommitInfoDto> getActions() {
    return Columns.ACTIONS;
  }

  private void createTableColumns() {
    commitInfoTable.addColumn(Columns.DATE, translations.commitInfoMap().get("Date"));
    commitInfoTable.addColumn(Columns.AUTHOR, translations.commitInfoMap().get("Author"));
    commitInfoTable.addColumn(Columns.ACTIONS, translations.actionsLabel());
    commitInfoDataProvider.addDataDisplay(commitInfoTable);
    commitInfoTable.setEmptyTableWidget(new Label(translations.noVcsCommitHistoryAvailable()));
  }
  private void initializeVocabulariesTable() {
    dataProvider.addDataDisplay(table);

    table.addColumn(new VocabularyNameColumn(), translations.nameLabel());
    table.addColumn(new LocaleTextColumn<VocabularyDto>() {
      @Override
      protected JsArray<LocaleTextDto> getLocaleText(VocabularyDto term) {
        return term.getTitleArray();
      }
    }, translations.titleLabel());
    table.addColumn(new LocaleTextColumn<VocabularyDto>() {
      @Override
      protected JsArray<LocaleTextDto> getLocaleText(VocabularyDto term) {
        return term.getDescriptionArray();
      }
    }, translations.descriptionLabel());
    table.addColumn(new TextColumn<VocabularyDto>() {
      @Override
      public String getValue(VocabularyDto object) {
        return object.getTermsCount() + "";
      }
    }, translations.termsLabel());

    actions = new ActionsSortableColumn<VocabularyDto>(new Supplier<List<VocabularyDto>>() {
      @Override
      public List<VocabularyDto> get() {
        return Strings.isNullOrEmpty(filter.getText()) ? dataProvider.getList() : Lists.<VocabularyDto>newArrayList();
      }
    });
    actions.setActionHandler(new ActionHandler<VocabularyDto>() {
      @Override
      public void doAction(VocabularyDto object, String actionName) {
        switch (actionName) {
          case ActionsSortableColumn.MOVE_UP_ACTION:
            getUiHandlers().onMoveUpVocabulary(object);
            break;
          case ActionsSortableColumn.MOVE_DOWN_ACTION:
            getUiHandlers().onMoveDownVocabulary(object);
            break;
          default:
            throw new IllegalArgumentException(actionName);
        }
      }
    });

    table.addColumnSortHandler(new ColumnSortEvent.Handler() {
      @Override
      public void onColumnSort(ColumnSortEvent columnSortEvent) {
        getUiHandlers().onSortVocabularies(columnSortEvent.isSortAscending());
      }
    });

    table.getHeader(0).setHeaderStyleNames("sortable-header-column");
    table.setPageSize(DEFAULT_PAGE_SIZE);
    table.setEmptyTableWidget(new com.google.gwt.user.client.ui.InlineLabel(translationMessages.vocabularyCount(0)));
    pager.setDisplay(table);
  }

  private void initializeFilter() {
    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterVocabularies());
    filter.getTextBox().addStyleName("input-xlarge");
    filter.getClear().setTitle(translations.clearFilter());
  }

  @Override
  public void setTaxonomy(@Nullable TaxonomyDto taxonomy) {
    if(taxonomy != null) {
      renderTaxonomy(taxonomy);
    }
    detailsPanel.setVisible(taxonomy != null);
  }

  @Override
  public void setVocabularies(JsArray<VocabularyDto> vocabularies) {
    dataProvider.setList(JsArrays.toList(vocabularies));
    dataProvider.refresh();
    pager.setPagerVisible(table.getRowCount() > DEFAULT_PAGE_SIZE);
  }

  @Override
  public void setEditable(boolean editable) {
    remove.setVisible(editable);
    edit.setVisible(editable);
    addVocabulary.setVisible(editable);
    //saveChanges.setVisible(false);

    if (table.getColumnIndex(actions)>0) table.removeColumn(actions);
    if (editable) table.addColumn(actions, translations.actionsLabel());
  }

  @Override
  public void setDirty(boolean isDirty) {
    saveChangesAlert.setVisible(isDirty);
  }

  @Override
  public HasAuthorization getCommitsAuthorizer() {
    return new WidgetAuthorizer(commitInfoPanel);
  }

  private void renderTaxonomy(TaxonomyDto taxonomy) {
    taxonomyName.setText(taxonomy.getName());
    author.setText(taxonomy.hasAuthor() ? taxonomy.getAuthor() : "");
    renderLicense(taxonomy);
    renderText(titlePanel, taxonomy.getTitleArray());
    renderText(descriptionPanel, taxonomy.getDescriptionArray());
    setVocabularies(taxonomy.getVocabulariesArray());
  }

  private void renderLicense(TaxonomyDto taxonomy) {
    license.setText("");
    licenseLink.setText("");
    if (!taxonomy.hasLicense()) return;

    if (taxonomy.getLicense().startsWith("CC ")) {
      renderCreativeCommonsLicense(taxonomy);
    } else {
      license.setText(taxonomy.getLicense());
    }
  }

  private void renderCreativeCommonsLicense(TaxonomyDto taxonomy) {
    String[] licenseParts = taxonomy.getLicense().split(" ");
    if (licenseParts.length == 3) {
      licenseLink.setText(taxonomy.getLicense());
      licenseLink.setHref(
          "https://creativecommons.org/licenses/" + licenseParts[1].toLowerCase() + "/" + licenseParts[2] + "/");
      licenseLink.setTarget("_blank");
    } else {
      license.setText(taxonomy.getLicense());
    }
  }

  private void renderText(Panel panel, JsArray<LocaleTextDto> texts) {
    panel.clear();
    for(LocaleTextDto text : JsArrays.toIterable(texts)) {
      FlowPanel localePanel = new FlowPanel();
      localePanel.setStyleName("small-bottom-margin");
      panel.add(localePanel);
      InlineLabel locale = new InlineLabel(text.getLocale());
      locale.setStyleName("label small-right-indent");
      localePanel.add(locale);
      InlineLabel label = new InlineLabel(text.getText());
      localePanel.add(label);
    }
  }

  @UiHandler("edit")
  public void onEdit(ClickEvent event) {
    getUiHandlers().onEdit();
  }

  @UiHandler("remove")
  public void onDelete(ClickEvent event) {
    getUiHandlers().onDelete();
  }

  @UiHandler("download")
  public void onDownload(ClickEvent event) {
    getUiHandlers().onDownload();
  }

  @UiHandler("addVocabulary")
  public void onAddVocabulary(ClickEvent event) {
    getUiHandlers().onAddVocabulary();
  }

  @UiHandler("saveChanges")
  public void onSaveChanges(ClickEvent event) {
    getUiHandlers().onSaveChanges();
  }

  @UiHandler("resetChanges")
  public void onResetChanges(ClickEvent event) {
    getUiHandlers().onResetChanges();
  }

  @UiHandler("filter")
  void onFilterUpdate(KeyUpEvent event) {
    getUiHandlers().onFilterUpdate(filter.getText());
  }

  private class VocabularyNameColumn extends ClickableColumn<VocabularyDto> {

    private VocabularyNameColumn() {
      setFieldUpdater(new FieldUpdater<VocabularyDto, String>() {
        @Override
        public void update(int rowIndex, VocabularyDto dto, String value) {
          getUiHandlers().onVocabularySelection(dto.getName());
        }
      });
      setDefaultSortAscending(false);
      setSortable(true);
    }

    @Override
    public String getValue(VocabularyDto object) {
      return object.getName();
    }
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

    static final ActionsColumn<VcsCommitInfoDto> ACTIONS = new ActionsColumn<VcsCommitInfoDto>(
        new ActionsProvider<VcsCommitInfoDto>() {

          @Override
          public String[] allActions() {
            return new String[] { DIFF_ACTION, DIFF_CURRENT_ACTION, RESTORE_ACTION };
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
