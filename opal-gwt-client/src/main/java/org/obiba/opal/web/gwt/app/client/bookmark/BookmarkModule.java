/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.bookmark;

import org.obiba.opal.web.gwt.app.client.bookmark.icon.BookmarkIconPresenter;
import org.obiba.opal.web.gwt.app.client.bookmark.icon.BookmarkIconView;
import org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter;
import org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListView;
import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;

public class BookmarkModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindPresenterWidget(BookmarkIconPresenter.class, BookmarkIconPresenter.Display.class, BookmarkIconView.class);
    bindPresenterWidget(BookmarkListPresenter.class, BookmarkListPresenter.Display.class, BookmarkListView.class);
  }
}
