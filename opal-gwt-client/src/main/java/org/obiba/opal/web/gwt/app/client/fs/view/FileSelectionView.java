/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.fs.view;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 *
 */
public class FileSelectionView extends ViewWithUiHandlers<FileSelectionUiHandlers>
    implements FileSelectionPresenter.Display {

  interface Binder extends UiBinder<Panel, FileSelectionView> {}

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

  @Inject
  public FileSelectionView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    fileField.setReadOnly(true);
  }

  @UiHandler("browseButton")
  public void onBrowse(ClickEvent event) {
    getUiHandlers().onBrowse();
  }

  //
  // FileSelectionPresenter.Display methods
  //

  @Override
  public String getFile() {
    return fileField.getText();
  }

  @Override
  public void setFieldWidth(String width) {
    fileField.setWidth(width);
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

  @Override
  public boolean isEnabled() {
    return browseButton.isEnabled();
  }

  @Override
  public HasText getFileText() {
    return fileField;
  }

}
