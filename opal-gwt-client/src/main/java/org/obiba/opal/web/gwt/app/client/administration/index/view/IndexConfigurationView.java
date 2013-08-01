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
import com.google.gwt.core.client.GWT;
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

  interface ViewUiBinder extends UiBinder<Widget, IndexConfigurationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Modal dialog;

  @UiField
  TextBox clusterName;

  @UiField
  TextBox indexName;

  @UiField
  NumericTextBox nbShards;

  @UiField
  NumericTextBox nbReplicas;

  @UiField
  HasText settings;

  @Inject
  public IndexConfigurationView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
  }

  private void initWidgets() {
    dialog.hide();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void show() {
    indexName.setFocus(true);
    super.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDialogMode(IndexConfigurationPresenter.Mode dialogMode) {
    //name.setEnabled(IndexPresenter.Mode.UPDATE.equals(dialogMode));
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
  public void setIndexName(String indexName) {
    this.indexName.setText(indexName);
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
  public String getIndexName() {
    return indexName.getText();
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
