/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.view;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.CodingViewModalPresenter.Display;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class CodingViewModalView extends ViewImpl implements Display {

  interface CodingViewModalUiBinder extends UiBinder<DialogBox, CodingViewModalView> {}

  private static final CodingViewModalUiBinder uiBinder = GWT.create(CodingViewModalUiBinder.class);

  private final Widget uiWidget;

  @UiField
  DialogBox dialog;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  ListBox datasourceNameBox;

  @UiField
  TextBox viewNameBox;

  @UiField
  CheckBox duplicateCheck;

  public CodingViewModalView() {
    uiWidget = uiBinder.createAndBindUi(this);
    uiBinder.createAndBindUi(this);
    dialog.hide();

    cancelButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        dialog.hide();
      }
    });

    duplicateCheck.setValue(true);
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public void showDialog() {
    dialog.center();
    dialog.show();
    viewNameBox.setFocus(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HandlerRegistration addSaveHandler(ClickHandler handler) {
    return saveButton.addClickHandler(handler);
  }

  @Override
  public HasText getViewName() {
    return viewNameBox;
  }

  @Override
  public HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> closeHandler) {
    return dialog.addCloseHandler(closeHandler);
  }

  @Override
  public void populateDatasources(JsArray<DatasourceDto> datasources) {
    datasourceNameBox.clear();
    for(DatasourceDto ds : JsArrays.toIterable(datasources)) {
      datasourceNameBox.addItem(ds.getName());
    }
    datasourceNameBox.setSelectedIndex(0);
  }

  @Override
  public boolean getDuplicate() {
    return duplicateCheck.getValue();
  }

  @Override
  public String getDatasourceName() {
    return datasourceNameBox.getItemText(datasourceNameBox.getSelectedIndex());
  }

  @Override
  public void showProgress(boolean progress) {

    if(progress) dialog.addStyleName("progress");
    else dialog.removeStyleName("progress");
    datasourceNameBox.setEnabled(!progress);
    viewNameBox.setEnabled(!progress);
    duplicateCheck.setEnabled(!progress);
    saveButton.setEnabled(!progress);
    cancelButton.setEnabled(!progress);
  }

}
