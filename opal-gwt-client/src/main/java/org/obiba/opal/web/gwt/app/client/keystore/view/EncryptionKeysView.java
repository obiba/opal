/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.view;

import org.obiba.opal.web.gwt.app.client.keystore.presenter.EncryptionKeysPresenter;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.EncryptionKeysUiHandlers;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class EncryptionKeysView extends ViewWithUiHandlers<EncryptionKeysUiHandlers>
    implements EncryptionKeysPresenter.Display {

  interface Binder extends UiBinder<Widget, EncryptionKeysView> {}

  @Inject
  public EncryptionKeysView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

}
