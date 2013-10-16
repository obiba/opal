package org.obiba.opal.web.gwt.app.client.support;

import com.google.gwt.user.client.ui.HasWidgets;

public interface BreadcrumbsBuilder {

  BreadcrumbsBuilder setStartingDepth(int startingDepth);

  BreadcrumbsBuilder setBreadcrumbView(HasWidgets breadcrumbsView);

  void build();
}
