/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.apps;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.administration.apps.event.AppsTokenUpdateEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;

public class AppsTokenModalPresenter extends ModalPresenterWidget<AppsTokenModalPresenter.Display>
    implements AppsTokenModalUiHandlers {

  private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final double PASSWORD_CHARACTERS_LENGTH = PASSWORD_CHARACTERS.length();
  private static final int PASSWORD_MX_LENGTH = 32;

  @Inject
  public AppsTokenModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void onUpdateToken(String token) {
    fireEvent(new AppsTokenUpdateEvent(token));
    getView().hideDialog();
  }

  @Override
  public String onGenerateToken() {
    String generated = "";

    for (int i = 0; i < PASSWORD_MX_LENGTH; i++) {
      generated += PASSWORD_CHARACTERS.charAt((int) Math.floor(Math.random() * PASSWORD_CHARACTERS_LENGTH));
    }

    return generated;
  }

  public void setToken(String token) {
    getView().setToken(token);
  }

  public interface Display extends PopupView, HasUiHandlers<AppsTokenModalUiHandlers> {

    void hideDialog();

    void showError(String message);

    void setToken(String token);
  }

}
