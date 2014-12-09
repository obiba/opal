/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public abstract class PlaceRequestCell<C> extends LinkCell<C> {

  private final PlaceManager placeManager;

  public PlaceRequestCell(PlaceManager placeManager) {
    this.placeManager = placeManager;
  }

  @Override
  public String getLink(C value) {
    return "#" + placeManager.buildHistoryToken(getPlaceRequest(value));
  }

  public abstract PlaceRequest getPlaceRequest(C value);
}
