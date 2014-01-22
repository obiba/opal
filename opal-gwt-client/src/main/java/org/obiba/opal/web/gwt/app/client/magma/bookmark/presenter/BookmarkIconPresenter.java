/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.bookmark.presenter;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class BookmarkIconPresenter extends PresenterWidget<BookmarkIconPresenter.Display>
    implements BookmarkIconUiHandlers {

  @Inject
  public BookmarkIconPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  protected void onReveal() {
    refresh();
  }

  private void refresh() {

  }

  @Override
  public void toggleBookmark() {

  }

  public void setBookmarkable(String uri) {

  }

  public interface Display extends View, HasUiHandlers<BookmarkIconUiHandlers> {

    void setBookmark(boolean isBookmarked);
  }

}
