/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.configuration.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.opal.GeneralConf;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.DropdownButton;
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
  PropertiesTable generalProperties;

  @UiField
  IconAnchor editGeneralSettings;

  @UiField
  Panel permissionsPanel;

  @UiField
  Panel permissions;

  @UiField
  DropdownButton keyPairButton;

  @Inject
  public ConfigurationView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));

    keyPairButton.setText(translations.setKeyPair());
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if(slot == ConfigurationPresenter.Display.Slots.Permissions) {
      permissions.clear();
      permissions.add(content);
    }
  }

  @Override
  public void renderGeneralProperties(GeneralConf resource) {
    generalProperties.clearProperties();
    generalProperties.addProperty(translations.nameLabel(), resource.getName());
    generalProperties.addProperty(translations.defaultCharsetLabel(), resource.getDefaultCharSet());
    generalProperties.addProperty(translations.publicUrl(), resource.getPublicURL());
    generalProperties.addProperty(translations.logoutUrl(), resource.getLogoutURL());
    generalProperties.addProperty(new Label(translations.languageLabel()), getLanguages(resource));
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new WidgetAuthorizer(permissionsPanel);
  }

  @UiHandler("editGeneralSettings")
  public void onEdit(ClickEvent event) {
    getUiHandlers().onEditGeneralSettings();
  }

  @UiHandler("importKeyPair")
  public void onImportKeyPair(ClickEvent event) {
    getUiHandlers().onImportKeyPair();
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
    return resource.getLanguagesArray().length() > 0
        ? new Label(resource.getLanguagesArray().join(", "))
        : new Label("");
  }

}
