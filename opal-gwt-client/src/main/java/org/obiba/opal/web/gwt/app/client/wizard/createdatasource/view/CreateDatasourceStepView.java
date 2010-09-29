/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createdatasource.view;

import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourceStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class CreateDatasourceStepView extends Composite implements CreateDatasourceStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  Button cancelButton;

  @UiField
  Button createButton;

  @UiField
  TextBox datasourceName;

  @UiField
  ListBox datasourceType;

  //
  // Constructors
  //

  public CreateDatasourceStepView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // CreateViewStepPresenter.Display Methods
  //

  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return cancelButton.addClickHandler(handler);
  }

  public HandlerRegistration addCreateClickHandler(ClickHandler handler) {
    return createButton.addClickHandler(handler);
  }

  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("CreateDatasourceStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, CreateDatasourceStepView> {
  }

  @Override
  public String getDatasourceName() {
    return datasourceName.getText();
  }

  @Override
  public String getDatasourceType() {
    return datasourceType.getItemText(datasourceType.getSelectedIndex());
  }
}
