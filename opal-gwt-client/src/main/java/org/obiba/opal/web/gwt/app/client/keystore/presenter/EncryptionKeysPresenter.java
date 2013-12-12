/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.presenter;

import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class EncryptionKeysPresenter extends PresenterWidget<EncryptionKeysPresenter.Display>
    implements EncryptionKeysUiHandlers {

  private final ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider;

  @Inject
  public EncryptionKeysPresenter(Display display, EventBus eventBus,
      ModalProvider<CreateKeyPairModalPresenter> createKeyPairModalProvider) {
    super(eventBus, display);
    this.createKeyPairModalProvider = createKeyPairModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  public void initialize() {

  }

  public interface Display extends View, HasUiHandlers<EncryptionKeysUiHandlers> {
  }
}
