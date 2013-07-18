package org.obiba.opal.web.gwt.app.client.support;

import com.google.gwt.user.client.ui.HasWidgets;

public interface BreadcrumbsBuilder {
  DefaultBreadcrumbsBuilder setStartingDepth(int startingDepth);
  DefaultBreadcrumbsBuilder setBreadcrumbView(final HasWidgets breadcrumbsView);
  void build();
}
