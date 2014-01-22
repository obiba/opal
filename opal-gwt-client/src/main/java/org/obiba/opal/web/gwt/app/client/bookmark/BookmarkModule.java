/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.bookmark;

import org.obiba.opal.web.gwt.app.client.bookmark.presenter.BookmarkIconPresenter;
import org.obiba.opal.web.gwt.app.client.bookmark.view.BookmarkIconView;
import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;

import com.gwtplatform.dispatch.rest.client.RestApplicationPath;
import com.gwtplatform.dispatch.rest.client.gin.RestDispatchAsyncModule;

public class BookmarkModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    install(new RestDispatchAsyncModule.Builder().build());
    bindConstant().annotatedWith(RestApplicationPath.class).to("/ws");

    bindPresenterWidget(BookmarkIconPresenter.class, BookmarkIconPresenter.Display.class, BookmarkIconView.class);
  }
}
