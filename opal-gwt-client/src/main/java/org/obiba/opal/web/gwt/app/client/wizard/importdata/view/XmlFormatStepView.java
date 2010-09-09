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
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class XmlFormatStepView extends Composite implements XmlFormatStepPresenter.Display {

  @UiTemplate("XmlFormatStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, XmlFormatStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private FileSelectionPresenter.Display fileSelection;

  @UiField
  Button nextButton;

  @UiField
  ListBox units;

  @UiField
  SimplePanel selectXmlFilePanel;

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
  public HandlerRegistration addNextClickHandler(ClickHandler handler) {
    return nextButton.addClickHandler(handler);
  }

  @Override
  public void setNextEnabled(boolean enabled) {
    nextButton.setEnabled(enabled);
  }

  @Override
  public void setUnits(JsArray<FunctionalUnitDto> units) {
    this.units.clear();
    this.units.addItem("");
    for(int i = 0; i < units.length(); i++) {
      this.units.addItem(units.get(i).getName());
    }
  }

  @Override
  public String getSelectedUnit() {
    return units.getItemText(units.getSelectedIndex());
  }

}
