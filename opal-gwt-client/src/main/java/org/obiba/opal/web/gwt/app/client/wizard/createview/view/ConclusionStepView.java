/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter.Display;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.ConclusionStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ConclusionStepView extends Composite implements ConclusionStepPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  Label processingViewLabel;

  @UiField
  SimplePanel resourceRequestPanel;

  @UiField
  FlowPanel configureViewPanel;

  @UiField
  Button configureViewButton;

  //
  // Constructors
  //

  public ConclusionStepView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // ConclusionStepPresenter.Display Methods
  //

  public void setResourceRequest(Display resourceRequestDisplay) {
    resourceRequestPanel.setWidget(resourceRequestDisplay.asWidget());
  }

  public void showConfigureViewWidgets(boolean show) {
    configureViewPanel.setVisible(show);
  }

  public HandlerRegistration addConfigureViewClickHandler(ClickHandler handler) {
    return configureViewButton.addClickHandler(handler);
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

  @UiTemplate("ConclusionStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ConclusionStepView> {
  }
}
