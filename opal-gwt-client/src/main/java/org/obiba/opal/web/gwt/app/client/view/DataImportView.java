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
import org.obiba.opal.web.model.client.FileDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

/**
 * View of the dialog used to import data into Opal.
 */
public class DataImportView extends DataCommonView implements DataImportPresenter.Display {

  @UiTemplate("DataImportView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DataImportView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  Tree files;

  @UiField
  CheckBox shouldArchive;

  @UiField
  TextBox archiveDirectory;

  private JsArrayString selectedFiles = JavaScriptObject.createArray().cast();

  public DataImportView() {
    initWidget(uiBinder.createAndBindUi(this));
    archiveDirectory.setEnabled(shouldArchive.getValue());
    shouldArchive.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        archiveDirectory.setEnabled(shouldArchive.getValue());
      }
    });
    files.addSelectionHandler(new SelectionHandler<TreeItem>() {

      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        FileDto dto = (FileDto) event.getSelectedItem().getUserObject();

        selectedFiles.shift();
        selectedFiles.push(dto.getPath());
      }

    });
  }

  @Override
  public void setFiles(FileDto root) {
    this.files.clear();
    for(int i = 0; i < root.getChildrenArray().length(); i++) {
      this.files.addItem(createItem(root.getChildrenArray().get(i)));
    }
  }

  @Override
  public JsArrayString getSelectedFiles() {
    return selectedFiles;
  }

  @Override
  public String getArchiveDirectory() {
    return this.shouldArchive.getValue() ? this.archiveDirectory.getValue() : null;
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

  private TreeItem createItem(FileDto fileItem) {
    final TreeItem item = new TreeItem(fileItem.getName());
    item.setUserObject(fileItem);
    for(int i = 0; i < fileItem.getChildrenArray().length(); i++) {
      item.addItem(createItem(fileItem.getChildrenArray().get(i)));
    }
    return item;
  }
}
