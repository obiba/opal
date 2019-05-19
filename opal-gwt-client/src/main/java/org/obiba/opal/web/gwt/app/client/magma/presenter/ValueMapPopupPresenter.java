/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.obiba.opal.web.gwt.app.client.magma.event.GeoValueDisplayEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class ValueMapPopupPresenter extends ModalPresenterWidget<ValueMapPopupPresenter.Display> {

  /**
   * @param eventBus
   * @param view
   * @param proxy
   */
  @Inject
  public ValueMapPopupPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  public void handle(GeoValueDisplayEvent event) {
    getView().initialize(event);
  }

  //
  // Private methods
  //

  //
  // Inner classes and Interfaces
  //

  public interface Display extends PopupView, HasUiHandlers<ModalUiHandlers> {
    void initialize(GeoValueDisplayEvent event);
  }

}
