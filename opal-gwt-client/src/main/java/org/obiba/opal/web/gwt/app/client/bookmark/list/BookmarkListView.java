package org.obiba.opal.web.gwt.app.client.bookmark.list;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.model.client.opal.BookmarkDto;

import com.github.gwtbootstrap.client.ui.SimplePager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class BookmarkListView extends ViewWithUiHandlers<BookmarkListUiHandlers>
    implements BookmarkListPresenter.Display {

  interface Binder extends UiBinder<Widget, BookmarkListView> {}

  @UiField
  SimplePager pager;

  @UiField
  Table<BookmarkDto> table;

  private final Translations translations;

  @Inject
  public BookmarkListView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initTable();
  }

  private void initTable() {
    pager.setDisplay(table);
    table.addColumn(Columns.NAME, translations.nameLabel());
  }

  @Override
  public HasData<BookmarkDto> getTable() {
    return table;
  }

  private static final class Columns {

    final static Column<BookmarkDto, String> NAME = new TextColumn<BookmarkDto>() {
      @Override
      public String getValue(BookmarkDto dto) {
        return dto.getName();
      }
    };

  }
}
