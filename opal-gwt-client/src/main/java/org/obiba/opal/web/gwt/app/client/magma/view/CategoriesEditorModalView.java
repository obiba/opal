/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.presenter.CategoriesEditorModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.magma.presenter.CategoriesEditorModalPresenter.Display;

public class CategoriesEditorModalView extends ModalPopupViewWithUiHandlers<CategoriesEditorModalUiHandlers>
    implements Display {

  private static final int PAGE_SIZE = 20;

  private static final int MIN_WIDTH = 580;

  private static final int MIN_HEIGHT = 500;

  private final Widget widget;

  private final Translations translations = GWT.create(Translations.class);

  interface Binder extends UiBinder<Widget, CategoriesEditorModalView> {}

  private static final Binder uiBinder = GWT.create(CategoriesEditorModalView.class);

  @UiField
  Modal dialog;

  @UiField
  Button closeButton;

  @Inject
  public CategoriesEditorModalView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    dialog.setTitle(translations.editCategories());
    dialog.setMinWidth(MIN_WIDTH);
    dialog.setMinHeight(MIN_HEIGHT);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }
}
