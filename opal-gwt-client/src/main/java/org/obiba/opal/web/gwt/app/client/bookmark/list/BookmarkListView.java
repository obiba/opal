package org.obiba.opal.web.gwt.app.client.bookmark.list;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class BookmarkListView extends ViewWithUiHandlers<BookmarkListUiHandlers>
    implements BookmarkListPresenter.Display {

  interface Binder extends UiBinder<Widget, BookmarkListView> {}

  @Inject
  public BookmarkListView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

}
