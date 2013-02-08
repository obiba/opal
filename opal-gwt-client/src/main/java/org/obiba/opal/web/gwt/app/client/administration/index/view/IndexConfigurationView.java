/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.index.view;

import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.workbench.view.NumericTextBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class IndexConfigurationView extends PopupViewImpl implements IndexConfigurationPresenter.Display {

  @UiTemplate("IndexConfigurationView.ui.xml")
  interface ViewUiBinder extends UiBinder<DialogBox, IndexConfigurationView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel contentLayout;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  HasText clusterName;

  @UiField
  HasText indexName;

  @UiField
  HasValue<Boolean> dataNode;

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
    resizeHandle.makeResizable(contentLayout);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
  }

  @Override
  public void show() {
    //name.setFocus(true);
    super.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDialogMode(IndexConfigurationPresenter.Mode dialogMode) {
    //name.setEnabled(IndexPresenter.Mode.UPDATE.equals(dialogMode));
    dialog.setText(translations.esConfigurationLabel());
  }

  @Override
  public HasClickHandlers getSaveButton() {
    return saveButton;
  }

  @Override
  public HasClickHandlers getCancelButton() {
    return cancelButton;
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
  public HasText getClusterName() {
    return clusterName;
  }

  @Override
  public HasText getIndexName() {
    return indexName;
  }

  @Override
  public HasText getSettings() {
    return settings;
  }

  @Override
  public HasValue<Boolean> isDataNode() {
    return dataNode;
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
  public void setDataNode(Boolean b) {
    dataNode.setValue(b);
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
