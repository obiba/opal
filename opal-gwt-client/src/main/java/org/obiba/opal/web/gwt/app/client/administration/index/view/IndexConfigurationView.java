/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.view;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;
import org.obiba.opal.web.model.client.opal.ESCfgDto;

import javax.annotation.Nullable;

/**
 *
 */
public class IndexConfigurationView extends ModalPopupViewWithUiHandlers<IndexConfigurationUiHandlers>
    implements IndexConfigurationPresenter.Display {

  interface Binder extends UiBinder<Widget, IndexConfigurationView> {
  }

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

  @UiField
  ControlGroup clusterGroup;

  @UiField
  ControlGroup shardsGroup;

  @UiField
  ControlGroup replicasGroup;

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
    getUiHandlers().save(clusterName.getText(), nbShards.getNumberValue().intValue(), nbReplicas.getNumberValue().intValue(), settings.getText());
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @Override
  public void setConfiguration(ESCfgDto cfg) {
    clusterName.setText(cfg.getClusterName());
    nbShards.setValue(cfg.getShards());
    nbReplicas.setValue(cfg.getReplicas());
    settings.setText(cfg.getSettings());
  }

  @Override
  public HasText getClusterName() {
    return clusterName;
  }

  @Override
  public HasText getSettings() {
    return settings;
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
  public void clearErrors() {
    dialog.closeAlerts();
  }

  @Override
  public void showError(@Nullable IndexConfigurationPresenter.Display.FormField formField, String message) {
    ControlGroup group = null;
    if (formField != null) {
      switch (formField) {
        case CLUSTER_NAME:
          group = clusterGroup;
          break;
        case SHARDS:
          group = shardsGroup;
          break;
        case REPLICAS:
          group = replicasGroup;
          break;
      }
    }
    if (group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }
}
