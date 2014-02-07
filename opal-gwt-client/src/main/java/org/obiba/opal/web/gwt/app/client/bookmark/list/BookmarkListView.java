package org.obiba.opal.web.gwt.app.client.bookmark.list;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsProvider;
import org.obiba.opal.web.gwt.app.client.ui.celltable.BookmarksColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.datetime.client.Moment;
import org.obiba.opal.web.model.client.opal.BookmarkDto;

import com.github.gwtbootstrap.client.ui.CellTable;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import static org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter.Mode;

public class BookmarkListView extends ViewWithUiHandlers<BookmarkListUiHandlers>
    implements BookmarkListPresenter.Display {

  private final PlaceManager placeManager;

  interface Binder extends UiBinder<Widget, BookmarkListView> {}

  @UiField
  SimplePager pager;

  @UiField
  CellTable<BookmarkDto> table;

  private final Translations translations;

  private final ListDataProvider<BookmarkDto> dataProvider = new ListDataProvider<BookmarkDto>();

  private DeleteActionColumn deleteActionColumn;

  @Inject
  public BookmarkListView(Binder uiBinder, Translations translations, PlaceManager placeManager) {
    this.translations = translations;
    this.placeManager = placeManager;
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  @Override
  public void renderRows(List<BookmarkDto> rows) {
    dataProvider.setList(rows);
    pager.firstPage();
    dataProvider.refresh();
    pager.setVisible(dataProvider.getList().size() > pager.getPageSize());
  }

  @Override
  public HasActionHandler<BookmarkDto> getActions() {
    return deleteActionColumn == null ? null : deleteActionColumn;
  }

  @Override
  public void setMode(Mode mode) {
    switch (mode) {
      case VIEW_ONLY:
        if (deleteActionColumn != null) {
          table.removeColumn(deleteActionColumn);
          deleteActionColumn = null;
        }
        break;
      case VIEW_DELETE:
        deleteActionColumn = new DeleteActionColumn();
        table.addColumn(deleteActionColumn, translations.actionsLabel());
        deleteActionColumn.setActionHandler(getUiHandlers().getActionHandler());
        break;
    }
  }

  private void initTable() {
    table.setVisibleRange(0, 10);
    table.addColumn(new BookmarksColumn(placeManager), translations.resourceLabel());
    table.addColumn(new CreateColumn(), translations.createdLabel());

    table.setEmptyTableWidget(new Label(translations.noDataAvailableLabel()));
    pager.setDisplay(table);
    dataProvider.addDataDisplay(table);
  }

  private static final class CreateColumn extends TextColumn<BookmarkDto> {

    private CreateColumn() {
//      setSortable(true);
//      setDefaultSortAscending(true);
    }

    @Override
    public String getValue(BookmarkDto bookmarkDto) {
      return bookmarkDto.hasCreated()//
          ? Moment.create(bookmarkDto.getCreated()).fromNow() //
          : "";

    }
  }

  private final class DeleteActionColumn extends ActionsColumn<BookmarkDto> {

    public DeleteActionColumn() {
      super(new ActionsProvider<BookmarkDto>() {
        @Override
        public String[] allActions() {
          return new String[] { ActionsColumn.DELETE_ACTION };
        }

        @Override
        public String[] getActions(BookmarkDto value) {
          return allActions();
        }
      });
    }
  }
}
