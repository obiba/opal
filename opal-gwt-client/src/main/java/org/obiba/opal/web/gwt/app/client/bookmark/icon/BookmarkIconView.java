/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.bookmark.icon;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class BookmarkIconView extends ViewWithUiHandlers<BookmarkIconUiHandlers>
    implements BookmarkIconPresenter.Display {

  interface Binder extends UiBinder<Widget, BookmarkIconView> {}

  private final Translations translations;

  @UiField
  IconAnchor icon;

  @Inject
  public BookmarkIconView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @UiHandler("icon")
  public void toggleBookmark(ClickEvent event) {
    getUiHandlers().toggleBookmark();
  }

  @Override
  public void showIcon(boolean isBookmarked) {
    if(isBookmarked) {
      icon.setTitle(translations.clickToRemoveFromBookmarks());
      icon.setIcon(IconType.STAR);
      icon.addStyleDependentName("on");
    } else {
      icon.setTitle(translations.clickToAddToBookmarks());
      icon.setIcon(IconType.STAR_EMPTY);
      icon.removeStyleDependentName("on");
    }
    icon.setVisible(true);
  }

  @Override
  public void hideIcon() {
    icon.setVisible(false);
  }

  @Override
  public void addStyleName(String style) {
    icon.addStyleName(style);
  }

}
