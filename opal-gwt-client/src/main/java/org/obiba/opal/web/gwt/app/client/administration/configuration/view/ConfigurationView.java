package org.obiba.opal.web.gwt.app.client.administration.configuration.view;

import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.model.client.opal.GeneralConf;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
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
  PropertiesTable generalProperties;

  @UiField
  IconAnchor editGeneralSettings;

  @Inject
  public ConfigurationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public void renderGeneralProperties(GeneralConf resource) {
    generalProperties.clearProperties();
    generalProperties.addProperty(translations.nameLabel(), resource.getName());
    generalProperties.addProperty(translations.defaultCharsetLabel(), resource.getDefaultCharSet());
    generalProperties.addProperty(translations.publicUrl(), resource.getPublicURL());
    generalProperties.addProperty(new Label(translations.languageLabel()), getLanguages(resource));
  }

  @UiHandler("editGeneralSettings")
  public void onEdit(ClickEvent event) {
    getUiHandlers().onEditGeneralSettings();
  }

  @UiHandler("createKeyPair")
  public void onCreateKeyPair(ClickEvent event) {
    getUiHandlers().onCreateKeyPair();
  }

  @UiHandler("downloadCertificate")
  public void onDownload(ClickEvent event) {
    getUiHandlers().onDownloadCertificate();
  }

  private Label getLanguages(GeneralConf resource) {
    if(resource.getLanguagesArray().length() > 0) {
      return new Label(resource.getLanguagesArray().join(", "));
    }
    return new Label("");
  }

}
