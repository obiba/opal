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
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class FunctionalUnitView extends ViewImpl implements FunctionalUnitPresenter.Display {

  private final Widget widget;

  @UiField
  Button functionalUnitButton;

  @UiField
  Button exportButton;

  @UiField
  Button importButton;

  @UiField
  Button syncButton;

  @UiField
  ScrollPanel functionalUnitDetailsPanel;

  @UiField
  ScrollPanel functionalUnitListPanel;

  @UiTemplate("FunctionalUnitView.ui.xml")
  interface FunctionalUnitViewUiBinder extends UiBinder<Widget, FunctionalUnitView> {}

  private static final FunctionalUnitViewUiBinder uiBinder = GWT.create(FunctionalUnitViewUiBinder.class);

  public FunctionalUnitView() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    HasWidgets panel;
    if(slot == SplitPaneWorkbenchPresenter.Slot.LEFT) {
      panel = functionalUnitListPanel;
    } else {
      panel = functionalUnitDetailsPanel;
    }
    panel.clear();
    if(content != null) {
      panel.add(content);
    }
  }

  @Override
  public HandlerRegistration addFunctionalUnitClickHandler(ClickHandler handler) {
    return functionalUnitButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addExportIdentifiersClickHandler(ClickHandler handler) {
    return exportButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addImportIdentifiersClickHandler(ClickHandler handler) {
    return importButton.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addSyncIdentifiersClickHandler(ClickHandler handler) {
    return syncButton.addClickHandler(handler);
  }

  @Override
  public HasAuthorization getAddFunctionalUnitAuthorizer() {
    return new UIObjectAuthorizer(functionalUnitButton);
  }

  @Override
  public HasAuthorization getExportIdentifiersAuthorizer() {
    return new UIObjectAuthorizer(exportButton);
  }

  @Override
  public HasAuthorization getImportIdentifiersAuthorizer() {
    return new UIObjectAuthorizer(importButton);
  }

  @Override
  public HasAuthorization getSyncIdentifiersAuthorizer() {
    return new UIObjectAuthorizer(syncButton);
  }

}
