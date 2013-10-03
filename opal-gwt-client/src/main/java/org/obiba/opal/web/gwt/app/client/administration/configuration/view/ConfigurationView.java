package org.obiba.opal.web.gwt.app.client.administration.configuration.view;

import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalNavLink;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers>
    implements ConfigurationPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, ConfigurationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  OpalNavLink taxonomies;

  @UiField
  Panel content;

  private final Widget widget;

  public ConfigurationView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setInSlot(Object slot, IsWidget contentWidget) {
    if(ConfigurationPresenter.CONTENT == slot) {
      content.clear();
      content.add(contentWidget.asWidget());
    }
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public void setTaxonomiesHistoryToken(String historyToken) {
    taxonomies.setHistoryToken(historyToken);
  }

}
