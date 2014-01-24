/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.bookmark.presenter;

import org.obiba.opal.web.gwt.app.client.bookmark.rest.BookmarkRestService;
import org.obiba.opal.web.gwt.app.client.bookmark.rest.BookmarksRestService;
import org.obiba.opal.web.model.client.opal.BookmarkDto;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.rest.shared.RestDispatch;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class BookmarkIconPresenter extends PresenterWidget<BookmarkIconPresenter.Display>
    implements BookmarkIconUiHandlers {

  private final RestDispatch dispatcher;

  private final BookmarksRestService bookmarksRestService;

  private final BookmarkRestService bookmarkRestService;

  private String path;

  private boolean bookmarked;

  @Inject
  public BookmarkIconPresenter(EventBus eventBus, Display view, RestDispatch dispatcher,
      BookmarksRestService bookmarksRestService, BookmarkRestService bookmarkRestService) {
    super(eventBus, view);
    this.dispatcher = dispatcher;
    this.bookmarksRestService = bookmarksRestService;
    this.bookmarkRestService = bookmarkRestService;
  }

  @Override
  protected void onReveal() {
    refresh();
  }

  private void refresh() {
    if(path == null) return;

    dispatcher.execute(bookmarkRestService.getBookmark(path), new AsyncCallback<BookmarkDto>() {
      @Override
      public void onSuccess(BookmarkDto dto) {
        setBookmarked(dto != null);
      }

      @Override
      public void onFailure(Throwable caught) {
        GWT.log("onFailure", caught);
        //TODO display error
      }
    });
  }

  @Override
  public void toggleBookmark() {
    if(bookmarked) {
      dispatcher.execute(bookmarkRestService.deleteBookmark(path), new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          setBookmarked(false);
        }

        @Override
        public void onFailure(Throwable caught) {
          GWT.log("onFailure", caught);
          //TODO display error
        }
      });
    } else {
      dispatcher.execute(bookmarksRestService.addBookmarks(Lists.newArrayList(path)), new AsyncCallback<Void>() {
        @Override
        public void onSuccess(Void result) {
          setBookmarked(true);
        }

        @Override
        public void onFailure(Throwable caught) {
          GWT.log("onFailure", caught);
          //TODO display error
        }
      });
    }
  }

  private void setBookmarked(boolean bookmarked) {
    this.bookmarked = bookmarked;
    getView().setBookmark(bookmarked);
  }

  public void setBookmarkable(String path) {
    this.path = path;
  }

  public interface Display extends View, HasUiHandlers<BookmarkIconUiHandlers> {

    void setBookmark(boolean isBookmarked);

  }

}
