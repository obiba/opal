/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.DataImportPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DataImportView extends Composite implements DataImportPresenter.Display {

  @UiTemplate("DataImportView.ui.xml")
  interface ViewUiBinder extends UiBinder<DialogBox, DataImportView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  DialogBox dialog;

  @UiField
  Panel content;

  @UiField
  Button cancel;

  public DataImportView() {
    uiBinder.createAndBindUi(this);
    getDialog().setGlassEnabled(true);
    cancel.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getDialog().hide();
      }

    });
  }

  @Override
  public HasCloseHandlers<PopupPanel> getDialogBox() {
    return dialog;
  }

  @Override
  public void showDialog() {
    int height = (int) (Window.getClientHeight() * 0.9d);
    getDialog().setHeight(height + "px");
    content.setHeight(height + "px");

    getDialog().center();
    getDialog().show();
  }

  public DialogBox getDialog() {
    return dialog;
  }

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

}
