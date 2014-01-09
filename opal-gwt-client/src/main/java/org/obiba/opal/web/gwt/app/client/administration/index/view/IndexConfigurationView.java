/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.view;

import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class IndexConfigurationView extends ModalPopupViewWithUiHandlers<IndexConfigurationUiHandlers>
    implements IndexConfigurationPresenter.Display {

  interface Binder extends UiBinder<Widget, IndexConfigurationView> {}

  @UiField
  Modal dialog;

  @UiField
  TextBox clusterName;

  @UiField
  NumericTextBox nbShards;

  @UiField
  NumericTextBox nbReplicas;

  @UiField
  HasText settings;

  private final Translations translations;

  @Inject
  public IndexConfigurationView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initWidgets();
  }

  private void initWidgets() {
    dialog.hide();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDialogMode(IndexConfigurationPresenter.Mode dialogMode) {
    dialog.setTitle(translations.esConfigurationLabel());
  }

  @UiHandler("saveButton")
  public void onSaveButton(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @Override
  public void setClusterName(String clusterName) {
    this.clusterName.setText(clusterName);
  }

  @Override
  public void setSettings(String settings) {
    this.settings.setText(settings);
  }

  @Override
  public String getClusterName() {
    return clusterName.getText();
  }

  @Override
  public String getSettings() {
    return settings.getText();
  }

  @Override
  public Number getNbShards() {
    return nbShards.getNumberValue();
  }

  @Override
  public Number getNbReplicas() {
    return nbReplicas.getNumberValue();
  }

  @Override
  public void setNbShards(int nb) {
    nbShards.setValue(String.valueOf(nb));
  }

  @Override
  public void setNbReplicas(int nb) {
    nbReplicas.setValue(String.valueOf(nb));
  }
}
