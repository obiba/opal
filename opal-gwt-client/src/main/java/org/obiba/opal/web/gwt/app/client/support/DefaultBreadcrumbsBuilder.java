package org.obiba.opal.web.gwt.app.client.support;

import org.obiba.opal.web.gwt.app.client.ui.OpalNavLink;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.SetPlaceTitleHandler;

public class DefaultBreadcrumbsBuilder implements BreadcrumbsBuilder {

  private final PlaceManager placeManager;

  private HasWidgets breadcrumbsView;

  private int startingDepth = 0;

  @Inject
  public DefaultBreadcrumbsBuilder(final PlaceManager placeManager) {
    this.placeManager = placeManager;
  }

  public DefaultBreadcrumbsBuilder setStartingDepth(int startingDepth) {
    if(startingDepth < 0) {
      throw new IndexOutOfBoundsException("Starting depth cannot be negative");
    } else if(startingDepth > placeManager.getHierarchyDepth()) {
      throw new IndexOutOfBoundsException("Starting depth cannot exceed the current number of depths");
    }

    this.startingDepth = startingDepth;

    return this;
  }

  public DefaultBreadcrumbsBuilder setBreadcrumbView(final HasWidgets breadcrumbsView) {
    this.breadcrumbsView = breadcrumbsView;
    return this;
  }

  public void build() {
    if(breadcrumbsView == null) {
      throw new NullPointerException("Breadcrumbs view cannot be NULL");
    }

    breadcrumbsView.clear();

    final int size = placeManager.getHierarchyDepth();

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

