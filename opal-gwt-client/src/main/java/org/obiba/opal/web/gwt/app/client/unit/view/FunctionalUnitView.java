/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.view;

import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class FunctionalUnitView extends Composite implements FunctionalUnitPresenter.Display {

  @UiField
  Button functionalUnitButton;

  @UiField
  Button exportButton;

  @UiField
  ScrollPanel functionalUnitDetailsPanel;

  @UiField
  ScrollPanel functionalUnitListPanel;

  @UiTemplate("FunctionalUnitView.ui.xml")
  interface FunctionalUnitViewUiBinder extends UiBinder<Widget, FunctionalUnitView> {
  }

  private static FunctionalUnitViewUiBinder uiBinder = GWT.create(FunctionalUnitViewUiBinder.class);

  public FunctionalUnitView() {
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
  public ScrollPanel getFunctionalUnitDetailsPanel() {
    return functionalUnitDetailsPanel;
  }

  @Override
  public ScrollPanel getFunctionalUnitListPanel() {
    return functionalUnitListPanel;
  }

  @Override
  public HandlerRegistration addFunctionalUnitClickHandler(ClickHandler handler) {
    return functionalUnitButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addExportIdentifiersClickHandler(ClickHandler handler) {
    return exportButton.addClickHandler(handler);
  }

}
