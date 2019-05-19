/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import org.obiba.opal.web.gwt.app.client.ui.OpalNavLink;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.SetPlaceTitleHandler;

public class DefaultBreadcrumbsBuilder implements BreadcrumbsBuilder {

  private final PlaceManager placeManager;

  private HasWidgets breadcrumbsView;

  private int startingDepth;

  @Inject
  public DefaultBreadcrumbsBuilder(PlaceManager placeManager) {
    this.placeManager = placeManager;
  }

  @Override
  public DefaultBreadcrumbsBuilder setStartingDepth(
      @SuppressWarnings("ParameterHidesMemberVariable") int startingDepth) {

    if(startingDepth < 0) {
      throw new IndexOutOfBoundsException("Starting depth cannot be negative");
    }
    if(startingDepth > placeManager.getHierarchyDepth()) {
      throw new IndexOutOfBoundsException("Starting depth cannot exceed the current number of depths");
    }
    this.startingDepth = startingDepth;
    return this;
  }

  @Override
  public DefaultBreadcrumbsBuilder setBreadcrumbView(
      @SuppressWarnings("ParameterHidesMemberVariable") HasWidgets breadcrumbsView) {
    this.breadcrumbsView = breadcrumbsView;
    return this;
  }

  @Override
  public void build() {
    if(breadcrumbsView == null) {
      throw new NullPointerException("Breadcrumbs view cannot be NULL");
    }

    breadcrumbsView.clear();

    int size = placeManager.getHierarchyDepth();

    for(int i = startingDepth; i < size; i++) {
      final int index = i;
      placeManager.getTitle(i, new SetPlaceTitleHandler() {
        @Override
        public void onSetPlaceTitle(String title) {
          breadcrumbsView.add(new OpalNavLink(title, placeManager.buildRelativeHistoryToken(index + 1)));
        }
      });
    }
  }
}

