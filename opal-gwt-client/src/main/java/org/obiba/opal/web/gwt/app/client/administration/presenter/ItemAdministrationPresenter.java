/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;

/**
 * Base class for presenting administration widgets.
 */
public abstract class ItemAdministrationPresenter<D extends View, P extends Proxy<? extends Presenter<?, ?>>>
    extends Presenter<D, P> implements RequestAdministrationPermissionEvent.Handler, HasPageTitle {

  protected static final Translations translations = GWT.create(Translations.class);

  public ItemAdministrationPresenter(EventBus eventBus, D display, P p) {
    super(eventBus, display, p);
  }

  public abstract String getName();

  public abstract void authorize(HasAuthorization authorizer);

  @Override
  protected void onBind() {
    super.onBind();
    BreadcrumbDisplay view = (BreadcrumbDisplay)getView();

    view.setBreadcrumbItems(
        new BreadcrumbsBuilder.ItemsBuilder().addItem(translations.pageAdministrationTitle(), new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            getEventBus().fireEvent(new PlaceChangeEvent(Places.administrationPlace));
          }
        }).addItem(getTitle()).build());
  }
}