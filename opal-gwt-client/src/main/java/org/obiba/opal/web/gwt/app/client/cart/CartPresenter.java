/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.obiba.opal.web.gwt.app.client.cart.event.CartCountsUpdateEvent;
import org.obiba.opal.web.gwt.app.client.cart.service.CartService;
import org.obiba.opal.web.gwt.app.client.cart.service.CartVariableItem;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;

import java.util.List;

public class CartPresenter extends Presenter<CartPresenter.Display, CartPresenter.Proxy> implements HasPageTitle, CartUiHandlers {

  private final Translations translations;

  private final CartService cartService;

  private final PlaceManager placeManager;

  @Inject
  public CartPresenter(EventBus eventBus, Display view, Proxy proxy, Translations translations, CartService cartService, PlaceManager placeManager) {
    super(eventBus, view, proxy, ApplicationPresenter.WORKBENCH);
    this.translations = translations;
    this.cartService = cartService;
    this.placeManager = placeManager;
    getView().setUiHandlers(this);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageCartTitle();
  }

  @Override
  protected void onReveal() {
    getView().showVariables(cartService.getVariables());
  }

  @Override
  public void onClearVariables() {
    cartService.clearVariables();
    updateView();
  }

  @Override
  public void onRemoveVariable(String variableFullName) {
    cartService.removeVariable(variableFullName);
    updateView();
  }

  @Override
  public void onSearchEntities(List<CartVariableItem> selectedVariables) {
    if (selectedVariables.isEmpty()) return;
    List<String> queries = Lists.newArrayList();
    String entityType = "";
    boolean entityTypeError = false;
    for (CartVariableItem var : selectedVariables) {
      queries.add("in(" + var.getIdentifier() + ",*)");
      if (Strings.isNullOrEmpty(entityType)) entityType = var.getEntityType();
      else if (!entityType.equals(var.getEntityType())) {
        entityTypeError = true;
        break;
      }
    }
    if (entityTypeError) fireEvent(NotificationEvent.newBuilder().warn("CannotMixVariableEntityTypes").build());
    else placeManager.revealPlace(ProjectPlacesHelper.getSearchEntitiesPlace(entityType, queries));
  }

  private void updateView() {
    getView().showVariables(cartService.getVariables());
    fireEvent(new CartCountsUpdateEvent(cartService.getVariablesCount()));
  }

  @ProxyStandard
  @NameToken(Places.CART)
  public interface Proxy extends ProxyPlace<CartPresenter> {
  }

  public interface Display extends View, HasUiHandlers<CartUiHandlers> {

    void showVariables(List<CartVariableItem> variables);

  }
}
