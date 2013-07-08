package org.obiba.opal.web.gwt.app.client.administration.presenter;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.support.BreadcrumbsBuilder;

public interface BreadcrumbDisplay {
  void setBreadcrumbItems(List<BreadcrumbsBuilder.Item> items);
}
