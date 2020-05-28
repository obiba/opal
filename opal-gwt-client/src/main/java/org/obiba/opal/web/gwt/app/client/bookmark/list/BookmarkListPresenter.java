/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.bookmark.list;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.BookmarkDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.bookmark.list.BookmarkListPresenter.Mode.VIEW_AND_DELETE;

public class BookmarkListPresenter extends PresenterWidget<BookmarkListPresenter.Display>
    implements BookmarkListUiHandlers {

  public enum Mode {
    VIEW_ONLY,
    VIEW_AND_DELETE
  }

  @Inject
  public BookmarkListPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
    getView().setMode(VIEW_AND_DELETE);
  }

  public BookmarkListPresenter setMode(Mode mode) {
    getView().setMode(mode);
    return this;
  }

  @Override
  public ActionHandler<BookmarkDto> getActionHandler() {
    return new ActionHandler<BookmarkDto>() {
      @Override
      public void doAction(BookmarkDto bookmarkDto, String actionName) {
        if(ActionsColumn.REMOVE_ACTION.equals(actionName)) {
          deleteBookmark(bookmarkDto);
        }
      }
    };
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    refreshTable();
  }

  private void deleteBookmark(BookmarkDto bookmarkDto) {
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.BOOKMARK.create().build(bookmarkDto.getResource())) //
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            refreshTable();
          }
        }) //
        .delete().send();
  }

  private void refreshTable() {
    ResourceRequestBuilderFactory.<JsArray<BookmarkDto>>newBuilder() //
        .forResource(UriBuilders.BOOKMARKS.create().build()) //
        .withCallback(new ResourceCallback<JsArray<BookmarkDto>>() {
          @Override
          public void onResource(Response response, JsArray<BookmarkDto> resource) {
            getView().renderRows(JsArrays.toList(resource));
          }
        }) //
        .get().send();
  }

  public interface Display extends View, HasUiHandlers<BookmarkListUiHandlers> {

    void renderRows(List<BookmarkDto> rows);

    HasActionHandler<BookmarkDto> getActions();

    void setMode(Mode mode);
  }
}
