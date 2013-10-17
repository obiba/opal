package org.obiba.opal.web.gwt.app.client.administration.configuration.view;

import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalNavLink;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers>
    implements ConfigurationPresenter.Display {

  interface Binder extends UiBinder<Widget, ConfigurationView> {}

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  OpalNavLink taxonomies;

  @UiField
  Panel content;

  @Inject
  public ConfigurationView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
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
