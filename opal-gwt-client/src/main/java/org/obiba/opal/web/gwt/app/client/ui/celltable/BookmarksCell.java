/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui.celltable;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.support.BookmarkHelper;
import org.obiba.opal.web.model.client.opal.BookmarkDto;
import org.obiba.opal.web.model.client.opal.LinkDto;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class BookmarksCell extends AbstractCell<BookmarkDto> {

  private CompositeCell<BookmarkDto> bookmarkCells;

  private final PlaceManager placeManager;

  public BookmarksCell(PlaceManager placeManager) {
    this.placeManager = placeManager;
  }

  @Override
  public void render(Context context, BookmarkDto bookmarkDto, SafeHtmlBuilder sb) {
    bookmarkCells = createBookmarks(bookmarkDto);
    bookmarkCells.render(context, bookmarkDto, sb);
  }

  private CompositeCell<BookmarkDto> createBookmarks(BookmarkDto bookmarkDto) {
    List<LinkDto> linksDto = JsArrays.toList(bookmarkDto.getLinksArray());
    List<HasCell<BookmarkDto, ?>> hasCells = new ArrayList<HasCell<BookmarkDto, ?>>();
    int upper = linksDto.size() - 1;
    int count = 0;

    for(LinkDto linkDto : linksDto) {
      hasCells.add(new Bookmark(linkDto));
      if (count++ < upper) hasCells.add(new Separator());
    }

    return new CompositeCell<BookmarkDto>(hasCells);
  }

  private class Bookmark implements HasCell<BookmarkDto, String> {

    private final PlaceRequestCell placeCell;

    private final LinkDto link;

    public Bookmark(LinkDto linkDto) {
      link = linkDto;
      placeCell = createPlaceCell();
    }

    @Override
    public Cell<String> getCell() {
      return placeCell;
    }

    @Override
    public FieldUpdater<BookmarkDto, String> getFieldUpdater() {
      return null;
    }

    @Override
    public String getValue(BookmarkDto bookmarkDto) {
      return link.getLink();
    }

    private PlaceRequestCell<String> createPlaceCell() {
      return new PlaceRequestCell<String>(placeManager) {
        @Override
        public PlaceRequest getPlaceRequest(String value) {
          return BookmarkHelper.createPlaceRequest(link);
        }
      };
    }

  }

  private class Separator implements HasCell<BookmarkDto, String> {

    private final TextCell separatorCell;

    public Separator() {
      separatorCell = new TextCell();
    }

    @Override
    public Cell<String> getCell() {
      return separatorCell;
    }

    @Override
    public FieldUpdater<BookmarkDto, String> getFieldUpdater() {
      return null;
    }

    @Override
    public String getValue(BookmarkDto bookmarkDto) {
      return "/";
    }

  }

}
