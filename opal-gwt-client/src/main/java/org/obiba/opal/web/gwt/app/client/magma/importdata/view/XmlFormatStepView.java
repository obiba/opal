/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.XmlFormatStepPresenter;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class XmlFormatStepView extends ViewImpl implements XmlFormatStepPresenter.Display {

  interface Binder extends UiBinder<Widget, XmlFormatStepView> {}

  private FileSelectionPresenter.Display fileSelection;

  @UiField
  SimplePanel selectXmlFilePanel;

  @UiField
  ControlGroup selectFileGroup;

  @Inject
  public XmlFormatStepView(Binder uiBinder) {
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
  public HasType<ControlGroupType> getGroupType(String id) {
    return selectFileGroup;
  }

}
