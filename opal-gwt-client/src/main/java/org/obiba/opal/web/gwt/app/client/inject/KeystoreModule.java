/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.keystore.presenter.CreateKeyPairModalPresenter;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.EncryptionKeysPresenter;
import org.obiba.opal.web.gwt.app.client.keystore.view.CreateKeyPairModalView;
import org.obiba.opal.web.gwt.app.client.keystore.view.EncryptionKeysView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;


public class KeystoreModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenterWidget(EncryptionKeysPresenter.class, EncryptionKeysPresenter.Display.class,
        EncryptionKeysView.class);
    bindPresenterWidget(CreateKeyPairModalPresenter.class, CreateKeyPairModalPresenter.Display.class,
        CreateKeyPairModalView.class);
  }

}

