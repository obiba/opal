package org.obiba.opal.web.gwt.app.client.administration.configuration.view;

import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.OpalNavLink;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.model.client.opal.GeneralConf;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ConfigurationView extends ViewWithUiHandlers<ConfigurationUiHandlers>
    implements ConfigurationPresenter.Display {

  private final Translations translations;

  interface Binder extends UiBinder<Widget, ConfigurationView> {}

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  OpalNavLink taxonomies;

  @UiField
  NavLink generalNavLink;

  @UiField
  Panel generalPanel;

  @UiField
  PropertiesTable generalProperties;

  @UiField
  IconAnchor editGeneralSettings;

  @Inject
  public ConfigurationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget contentWidget) {
    if(ConfigurationPresenter.CONTENT == slot) {
      generalPanel.clear();
      generalPanel.add(contentWidget.asWidget());
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

  @Override
  public void renderGeneralProperties(GeneralConf resource) {
    generalProperties.clearProperties();
    generalProperties.addProperty(translations.nameLabel(), resource.getName());
    generalProperties.addProperty(translations.defaultCharsetLabel(), resource.getDefaultCharSet());
    generalProperties.addProperty(translations.publicUrl(), resource.getPublicURL());
    generalProperties.addProperty(new Label(translations.languageLabel()), getLanguages(resource));

    generalNavLink.setActive(true);
  }

  @UiHandler("editGeneralSettings")
  public void onEdit(ClickEvent event) {
    getUiHandlers().onEditGeneralSettings();
  }

  private Label getLanguages(GeneralConf resource) {
    if(resource.getLanguagesArray().length() > 0) {
      return new Label(resource.getLanguagesArray().join(", "));
    }
    return new Label("");
  }

}
