/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class FileSelectionView extends Composite implements FileSelectionPresenter.Display {

  @UiTemplate("FileSelectionView.ui.xml")
  interface FileSelectionViewUiBinder extends UiBinder<Panel, FileSelectionView> {
  }

  //
  // Constants
  //

  private static FileSelectionViewUiBinder uiBinder = GWT.create(FileSelectionViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  TextBox fileField;

  @UiField
  Button browseButton;

  //
  // Constructors
  //

  public FileSelectionView() {
    super();
    initWidget(uiBinder.createAndBindUi(this));

    fileField.setReadOnly(true);
  }

  //
  // FileSelectionPresenter.Display methods
  //

  @Override
  public HandlerRegistration addBrowseClickHandler(ClickHandler handler) {
    return browseButton.addClickHandler(handler);
  }

  @Override
  public String getFile() {
    return fileField.getText();
  }

  @Override
  public void setFieldWidth(String width) {
    fileField.setWidth(width);
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

  @Override
  public void setEnabled(boolean enabled) {
    browseButton.setEnabled(enabled);
  }

  @Override
  public void clearFile() {
    fileField.setText("");
  }

  @Override
  public void setFile(String text) {
    fileField.setText(text);
  }

}
