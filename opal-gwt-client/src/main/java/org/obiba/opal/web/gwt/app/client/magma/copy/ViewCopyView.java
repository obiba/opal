/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.copy;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import javax.annotation.Nullable;
import java.util.List;

/**
 * View of the dialog used to export data from Opal.
 */
public class ViewCopyView extends ModalPopupViewWithUiHandlers<CopyUiHandlers>
    implements ViewCopyPresenter.Display {

  interface Binder extends UiBinder<Widget, ViewCopyView> {}

  @UiField
  Modal modal;

  @UiField
  ListBox datasources;

  @UiField
  ControlGroup newTableNameGroup;

  @UiField
  TextBox newName;

  @Inject
  public ViewCopyView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.copyView());
  }

  @UiHandler("submitButton")
  public void onSubmit(ClickEvent event) {
    getUiHandlers().onSubmit(getSelectedDatasource(), newName.getText());
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public void setDatasources(List<String> datasourceNames) {
    this.datasources.clear();
    for(String datasource : datasourceNames) {
      this.datasources.addItem(datasource);
    }
  }

  @Override
  public void setView(TableDto view) {
    newName.setText(view.getName());
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NEW_TABLE_NAME:
          group = newTableNameGroup;
          break;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  private String getSelectedDatasource() {
    return datasources.getValue(datasources.getSelectedIndex());
  }
}
