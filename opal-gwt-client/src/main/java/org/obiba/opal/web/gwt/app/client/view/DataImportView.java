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
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.FileDto;
import org.obiba.opal.web.model.client.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
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
  Label errors;

  @UiField
  Tree files;

  @UiField
  ListBox datasources;

  @UiField
  CheckBox shouldArchive;

  @UiField
  TextBox archiveDirectory;

  @UiField
  ListBox units;

  @UiField
  Button start;

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
    archiveDirectory.setEnabled(shouldArchive.getValue());
    shouldArchive.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        archiveDirectory.setEnabled(shouldArchive.getValue());
      }
    });
  }

  @Override
  public HasCloseHandlers<PopupPanel> getDialogBox() {
    return dialog;
  }

  @Override
  public void showErrors(String text) {
    this.errors.setText(text);
    this.errors.setVisible(true);
  }

  @Override
  public void setFiles(FileDto root) {
    this.files.clear();
    this.files.addItem(createItem(root));
  }

  @Override
  public JsArrayString getFiles() {
    return JavaScriptObject.createArray().cast();
  }

  @Override
  public String getArchiveDirectory() {
    return this.shouldArchive.getValue() ? this.archiveDirectory.getValue() : null;
  }

  @Override
  public HasClickHandlers getImport() {
    return start;
  }

  @Override
  public String getSelectedUnit() {
    return this.units.getValue(this.units.getSelectedIndex());
  }

  @Override
  public void setUnits(JsArray<FunctionalUnitDto> units) {
    this.units.clear();
    for(int i = 0; i < units.length(); i++) {
      this.units.addItem(units.get(i).getName());
    }
  }

  public TreeItem createItem(FileDto fileItem) {
    TreeItem item = new TreeItem(fileItem.getName());
    for(int i = 0; i < fileItem.getChildrenArray().length(); i++) {
      item.addItem(createItem(fileItem.getChildrenArray().get(i)));
    }
    return item;
  }

  @Override
  public String getSelectedDatasource() {
    return this.datasources.getValue(this.datasources.getSelectedIndex());
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    this.datasources.clear();
    for(int i = 0; i < datasources.length(); i++) {
      this.datasources.addItem(datasources.get(i).getName(), datasources.get(i).getName());
    }
  }

  @Override
  public void hideDialog() {
    getDialog().hide();
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
