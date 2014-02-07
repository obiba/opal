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

public class BookmarkListPresenter extends PresenterWidget<BookmarkListPresenter.Display>
    implements BookmarkListUiHandlers {

  @Inject
  public BookmarkListPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    getView().getActions().setActionHandler(new ActionHandler<BookmarkDto>() {

      @Override
      public void doAction(BookmarkDto bookmarkDto, String actionName) {
        if(ActionsColumn.DELETE_ACTION.equals(actionName)) {
          deleteBookmark(bookmarkDto);
        }
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
    });
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    refreshTable();
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
  }
}
