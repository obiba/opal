/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.place;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

@Singleton
public class OpalPlaceManager extends PlaceManagerImpl {

  private final PlaceRequest defaultPlaceRequest;

  @Inject
  public OpalPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter, @DefaultPlace String defaultNameToken) {
    super(eventBus, tokenFormatter);
    defaultPlaceRequest = new PlaceRequest.Builder().nameToken(defaultNameToken).build();
  }

  @Override
  public void revealDefaultPlace() {
    revealPlace(defaultPlaceRequest, true);
  }

  @Override
  public void revealErrorPlace(String invalidHistoryToken) {
    GWT.log("invalid token " + invalidHistoryToken);
    revealPlace(defaultPlaceRequest, true);
  }

  @Override
  public void revealUnauthorizedPlace(String unauthorizedHistoryToken) {
    revealPlace(new PlaceRequest.Builder().nameToken(Places.LOGIN).build(), false);
  }
}