/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

import java.util.List;

/**
 *
 */
public class DataShieldProfileModalView extends ModalPopupViewWithUiHandlers<DataShieldProfileModalUiHandlers> implements DataShieldProfileModalPresenter.Display {

  interface Binder extends UiBinder<Widget, DataShieldProfileModalView> {
  }

  @UiField
  Modal dialog;

  @UiField
  TextBox name;

  @UiField
  ListBox clusterList;

  private final Translations translations;

  //
  // Constructors
  //

  @Inject
  public DataShieldProfileModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initWidgets();
  }

  private void initWidgets() {
    dialog.hide();
    dialog.setTitle(translations.addDataShieldProfile());
  }

  @Override
  public void onShow() {
    name.setFocus(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    DataShieldProfileDto profile = DataShieldProfileDto.create();
    profile.setName(name.getText());
    profile.setCluster(clusterList.getSelectedItemText());
    profile.setEnabled(false);
    profile.setRestrictedAccess(false);
    getUiHandlers().save(profile);
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public void setClusters(List<RServerClusterDto> clusters) {
    clusterList.clear();
    for (RServerClusterDto cluster : clusters) {
      clusterList.addItem(cluster.getName());
    }
  }

  @Override
  public void clear() {
    name.setText("");
    clusterList.setSelectedIndex(0);
  }

}
