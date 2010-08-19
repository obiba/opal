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

import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ConclusionStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

public class ConclusionStepView extends Composite implements ConclusionStepPresenter.Display {

  @UiTemplate("ConclusionStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ConclusionStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  Anchor jobLink;

  @UiField
  Button returnButton;

  public ConclusionStepView() {
    initWidget(uiBinder.createAndBindUi(this));
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
  public HandlerRegistration addReturnClickHandler(ClickHandler handler) {
    return returnButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addJobLinkClickHandler(ClickHandler handler) {
    return jobLink.addClickHandler(handler);
  }

  @Override
  public HasText getJobLinkText() {
    return jobLink;
  }

}
