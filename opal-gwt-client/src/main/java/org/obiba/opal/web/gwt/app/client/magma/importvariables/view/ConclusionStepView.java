/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importvariables.view;

import org.obiba.opal.web.gwt.app.client.presenter.ResourceRequestPresenter.Display;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.ConclusionStepPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class ConclusionStepView extends ViewImpl implements ConclusionStepPresenter.Display {
  //
  // Static Variables
  //

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private final Widget uiWidget;

  //
  // Instance Variables
  //

  @UiField
  Label tableListLabel;

  @UiField
  FlowPanel tableListPanel;

  //
  // Constructors
  //

  public ConclusionStepView() {
    uiWidget = uiBinder.createAndBindUi(this);
  }

  //
  // UploadVariablesStepPresenter.Display Methods
  //

  @Override
  public void clearResourceRequests() {
    tableListLabel.setVisible(false);
    tableListPanel.clear();
  }

  @Override
  public void addResourceRequest(Display resourceRequestDisplay) {
    tableListLabel.setVisible(true);
    tableListPanel.add(resourceRequestDisplay.asWidget());
  }

  @Override
  public Widget asWidget() {
    return uiWidget;
  }

  @Override
  public void addToSlot(Object slot, IsWidget content) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void removeFromSlot(Object slot, IsWidget content) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  //
  // Methods
  //

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("ConclusionStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, ConclusionStepView> {}
}
