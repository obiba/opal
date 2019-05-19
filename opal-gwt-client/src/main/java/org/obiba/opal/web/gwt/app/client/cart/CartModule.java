/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import org.obiba.opal.web.gwt.app.client.cart.edit.CartVariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.cart.edit.CartVariableAttributeModalView;
import org.obiba.opal.web.gwt.app.client.cart.service.CartService;

public class CartModule extends AbstractPresenterModule {
  @Override
  protected void configure() {
    bindPresenter(CartPresenter.class, CartPresenter.Display.class, CartView.class, CartPresenter.Proxy.class);
    bindPresenterWidget(CartVariableAttributeModalPresenter.class, CartVariableAttributeModalPresenter.Display.class,
        CartVariableAttributeModalView.class);
    bind(CartService.class).in(Singleton.class);
  }
}
