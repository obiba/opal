/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import org.obiba.opal.web.gwt.app.client.navigator.event.GeoValueDisplayEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class ValueMapPopupPresenter extends PresenterWidget<ValueMapPopupPresenter.Display> {

  /**
   * @param eventBus
   * @param view
   * @param proxy
   */
  @Inject
  public ValueMapPopupPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addHandler();
  }

  public void handle(GeoValueDisplayEvent event) {
    getView().initialize(event);
  }

  //
  // Private methods
  //

  private void addHandler() {
    registerHandler(getView().getButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getView().hide();
      }
    }));
  }

  //
  // Inner classes and Interfaces
  //

  public interface Display extends PopupView {

    void initialize(GeoValueDisplayEvent event);

    HasClickHandlers getButton();

  }

}
