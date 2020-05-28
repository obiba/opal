/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui.celltable;

import org.obiba.opal.web.model.client.opal.BookmarkDto;

import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class BookmarksColumn extends Column<BookmarkDto, BookmarkDto> {

  public BookmarksColumn(PlaceManager placeManager) {
    super(new BookmarksCell(placeManager));
    setCellStyleNames("row-bookmarks");
    setSortable(true);
    setDefaultSortAscending(true);
  }

  @Override
  public BookmarkDto getValue(BookmarkDto bookmarkDto) {
    return bookmarkDto;
  }
}
