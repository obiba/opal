package org.obiba.opal.web.gwt.app.client.bookmark.list;

import org.obiba.opal.web.model.client.opal.BookmarkDto;

import com.google.gwt.view.client.HasData;
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

  public interface Display extends View, HasUiHandlers<BookmarkListUiHandlers> {

    HasData<BookmarkDto> getTable();

  }
}
