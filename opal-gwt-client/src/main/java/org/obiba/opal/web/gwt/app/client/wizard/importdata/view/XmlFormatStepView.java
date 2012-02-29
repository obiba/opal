/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.XmlFormatStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class XmlFormatStepView extends Composite implements XmlFormatStepPresenter.Display {

  @UiTemplate("XmlFormatStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, XmlFormatStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private FileSelectionPresenter.Display fileSelection;

  @UiField
  SimplePanel selectXmlFilePanel;

  @UiField
  CheckBox oldOnyx;

  @UiField
  HTMLPanel help;

  public XmlFormatStepView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setXmlFileSelectorWidgetDisplay(Display display) {
    selectXmlFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public String getSelectedFile() {
    return fileSelection.getFile();
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
  public Widget getStepHelp() {
    help.removeFromParent();
    return help;
  }

  @Override
  public boolean isOldOnyxFormat() {
    return oldOnyx.getValue();
  }

}
