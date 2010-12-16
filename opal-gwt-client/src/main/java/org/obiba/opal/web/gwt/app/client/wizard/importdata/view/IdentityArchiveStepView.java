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
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.IdentityArchiveStepPresenter;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class IdentityArchiveStepView extends Composite implements IdentityArchiveStepPresenter.Display {

  @UiTemplate("IdentityArchiveStepView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, IdentityArchiveStepView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  InlineLabel noUnitLabel;

  @UiField
  InlineLabel unitLabel;

  @UiField
  RadioButton identifierAsIs;

  @UiField
  RadioButton identifierSharedWithUnit;

  @UiField
  ListBox units;

  @UiField
  RadioButton archiveLeave;

  @UiField
  RadioButton archiveMove;

  @UiField
  SimplePanel archivePanel;

  @UiField
  HTMLPanel help;

  private FileSelectionPresenter.Display archiveSelection;

  public IdentityArchiveStepView() {
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
  public boolean isIdentifierAsIs() {
    return units.getItemCount() == 0 || identifierAsIs.getValue();
  }

  @Override
  public boolean isIdentifierSharedWithUnit() {
    return units.getItemCount() > 0 && identifierSharedWithUnit.getValue();
  }

  @Override
  public void setUnits(JsArray<FunctionalUnitDto> units) {
    this.units.clear();
    for(int i = 0; i < units.length(); i++) {
      this.units.addItem(units.get(i).getName());
    }
    this.units.setEnabled(isIdentifierSharedWithUnit());
    noUnitLabel.setVisible(units.length() == 0);
    unitLabel.setVisible(units.length() > 0);
    this.units.setVisible(units.length() > 0);
    identifierAsIs.setVisible(units.length() > 0);
    identifierSharedWithUnit.setVisible(units.length() > 0);
  }

  @Override
  public String getSelectedUnit() {
    return units.getItemText(units.getSelectedIndex());
  }

  @Override
  public boolean isArchiveLeave() {
    return archiveLeave.getValue();
  }

  @Override
  public boolean isArchiveMove() {
    return archiveMove.getValue();
  }

  @Override
  public void setArchiveWidgetDisplay(Display display) {
    archivePanel.setWidget(display.asWidget());
    archiveSelection = display;
    archiveSelection.setEnabled(isArchiveMove());
    archiveSelection.setFieldWidth("20em");
  }

  @Override
  public String getArchiveDirectory() {
    return isArchiveMove() ? this.archiveSelection.getFile() : null;
  }

  @Override
  public HandlerRegistration addIdentifierAsIsClickHandler(ClickHandler handler) {
    return identifierAsIs.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addIdentifierSharedWithUnitClickHandler(ClickHandler handler) {
    return identifierSharedWithUnit.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addArchiveLeaveClickHandler(ClickHandler handler) {
    return archiveLeave.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addArchiveMoveClickHandler(ClickHandler handler) {
    return archiveMove.addClickHandler(handler);
  }

  @Override
  public void setUnitEnabled(boolean enabled) {
    units.setEnabled(enabled);
  }

  @Override
  public void setIdentityEnabled(boolean enabled) {
    identifierAsIs.setEnabled(enabled);
    identifierSharedWithUnit.setEnabled(enabled);
    units.setEnabled(enabled);
  }

  @Override
  public void setIdentifierAsIs(boolean checked) {
    identifierAsIs.setValue(checked);
  }

  @Override
  public void setIdentifierSharedWithUnit(boolean checked) {
    identifierSharedWithUnit.setValue(checked);
  }

  @Override
  public void setSelectedUnit(String unit) {
    for(int i = 0; i < units.getItemCount(); i++) {
      if(units.getItemText(i).equals(unit)) {
        units.setSelectedIndex(i);
        break;
      }
    }
  }

  @Override
  public Widget getStepHelp() {
    return help;
  }

}
