/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.server.view;

import org.obiba.opal.web.gwt.app.client.administration.server.presenter.ServerPresenter;
import org.obiba.opal.web.gwt.app.client.administration.server.presenter.ServerUiHandlers;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.CharacterSetView;
import org.obiba.opal.web.gwt.app.client.ui.LocaleChooser;
import org.obiba.opal.web.model.client.opal.GeneralConf;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ServerView extends ViewWithUiHandlers<ServerUiHandlers> implements ServerPresenter.Display {

  @UiTemplate("ServerView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ServerView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget uiWidget;

  @UiField
  Panel breadcrumbs;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox name;

  @UiField(provided = true)
  LocaleChooser locales;

  @UiField
  CharacterSetView characterSet;

  public ServerView() {
    locales = new LocaleChooser(true);
    uiWidget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public void renderProperties(GeneralConf resource) {
    name.setText(resource.getName());
    characterSet.setDefaultCharset(resource.getDefaultCharSet());

    JsArrayString languages = JsArrays.toSafeArray(resource.getLanguagesArray());
    locales.selectLocales(languages);
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public String getName() {
    return name.getText();
  }

  @Override
  public String getDefaultCharSet() {
    return characterSet.getCharsetText().getText();
  }

  @Override
  public JsArrayString getLanguages() {
    JsArrayString languages = JsArrayString.createArray().cast();
    for(String locale : locales.getSelectedLocales()) {
      languages.push(locale);
    }

    return languages;
  }
}