/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

/**
 *
 */
public class CategoriesEditorModalPresenter extends ModalPresenterWidget<CategoriesEditorModalPresenter.Display>
    implements CategoriesEditorModalUiHandlers {

  private static final TranslationMessages translationMessages = GWT.create(TranslationMessages.class);

  @Inject
  public CategoriesEditorModalPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  public void initialize() {
    getView().setUiHandlers(this);
  }

  public interface Display extends PopupView, HasUiHandlers<CategoriesEditorModalUiHandlers> {}
}
