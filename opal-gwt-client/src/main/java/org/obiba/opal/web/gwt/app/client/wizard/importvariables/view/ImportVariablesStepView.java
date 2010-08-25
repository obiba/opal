/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter.Display;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ImportVariablesStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ImportVariablesStepView extends Composite implements ImportVariablesStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  FlowPanel tableListPanel;

  @UiField
  Button returnButton;

  //
  // Constructors
  //

  public ImportVariablesStepView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // UploadVariablesStepPresenter.Display Methods
  //

  public void clearResourceRequests() {
    tableListPanel.clear();
  }

  public void addResourceRequest(Display resourceRequestDisplay) {
    tableListPanel.add(resourceRequestDisplay.asWidget());
  }

  public void setReturnButtonEnabled(boolean enabled) {
    returnButton.setEnabled(enabled);
  }

  public HandlerRegistration addReturnClickHandler(ClickHandler handler) {
    return returnButton.addClickHandler(handler);
  }

  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  //
  // Methods
  //

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("ImportVariablesStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ImportVariablesStepView> {
  }
}
