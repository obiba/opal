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

import org.obiba.opal.web.gwt.app.client.bookmark.event.ToggleBookmarkEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class BookmarkIconPresenter extends PresenterWidget<BookmarkIconPresenter.Display>
    implements BookmarkIconUiHandlers {

  private String path;

  private boolean bookmarked;

  @Inject
  public BookmarkIconPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(ToggleBookmarkEvent.getType(), new ToggleBookmarkEvent.ToggleBookmarkHandler() {
      @Override
      public void onToggleBookmark(ToggleBookmarkEvent event) {
        GWT.log("ToggleBookmarkEvent for " + path + ", isBookmarked: " + event.isBookmarked());
        getView().setBookmark(bookmarked = event.isBookmarked());
      }
    });
  }

  @Override
  protected void onReveal() {
    refresh();
  }

  private void refresh() {
    GWT.log("refresh for path: " + path);
    if(path == null) return;

    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.BOOKMARK.create().build(path)) //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getEventBus().fireEvent(new ToggleBookmarkEvent(true));
          }
        }) //
        .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getEventBus().fireEvent(new ToggleBookmarkEvent(false));
          }
        }) //
        .get().send();
  }

  @Override
  public void toggleBookmark() {
    GWT.log("toggleBookmark for path: " + path);

    if(bookmarked) {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.BOOKMARK.create().build(path)) //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getEventBus().fireEvent(new ToggleBookmarkEvent(false));
            }
          }) //
          .delete().send();
    } else {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.BOOKMARKS.create().query("resource", path).build()) //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getEventBus().fireEvent(new ToggleBookmarkEvent(true));
            }
          }) //
          .post().send();
    }
  }

  public void setBookmarkable(String path) {
    this.path = path;
    refresh();
  }

  public interface Display extends View, HasUiHandlers<BookmarkIconUiHandlers> {

    void setBookmark(boolean isBookmarked);

  }

}
