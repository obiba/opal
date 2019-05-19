/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.shared.proxy.TokenFormatter;

import java.util.List;

/**
 * Cell that reveals a place hierarchy.
 * 
 * @param <C>
 */
public abstract class PlacesRequestCell<C> extends LinkCell<C> {

  private final TokenFormatter tokenFormatter;

  public PlacesRequestCell(TokenFormatter tokenFormatter) {
    this.tokenFormatter = tokenFormatter;
  }

  @Override
  public String getLink(C value) {
    return "#" + tokenFormatter.toHistoryToken(getPlaceRequest(value));
  }

  public abstract List<PlaceRequest> getPlaceRequest(C value);
}
