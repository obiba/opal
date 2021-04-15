/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;


public class KeystoreModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenterWidget(EncryptionKeysPresenter.class, EncryptionKeysPresenter.Display.class,
        EncryptionKeysView.class);
    bindPresenterWidget(CreateKeyPairModalPresenter.class, CreateKeyPairModalPresenter.Display.class,
        CreateKeyPairModalView.class);
    bindPresenterWidget(ImportKeyPairModalPresenter.class, ImportKeyPairModalPresenter.Display.class,
        ImportKeyPairModalView.class);
  }

}

