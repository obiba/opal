/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.UnitSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.IdentifiersMappingDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class UnitSelectionStepView extends ViewImpl implements UnitSelectionStepPresenter.Display {

  interface Binder extends UiBinder<Widget, UnitSelectionStepView> {}

  @UiField
  InlineLabel noUnitLabel;

  @UiField
  Panel unitPanel;

  @UiField
  RadioButton identifierAsIs;

  @UiField
  RadioButton identifierSharedWithUnit;

  @UiField
  ListBox units;

  @UiField
  CheckBox incremental;

  @UiField
  NumericTextBox limit;

  @UiField
  FlowPanel unitSection;

  @Inject
  public UnitSelectionStepView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
    identifierAsIs.setValue(true);
  }

  @Override
  public boolean isIdentifierSharedWithUnit() {
    return units.getItemCount() > 0 && identifierSharedWithUnit.getValue();
  }

  @Override
  public void setIdentifiersMappings(JsArray<IdentifiersMappingDto> mappings) {
    units.clear();
    for(int i = 0; i < mappings.length(); i++) {
      units.addItem(mappings.get(i).getName());
    }
    units.setEnabled(isIdentifierSharedWithUnit());
    noUnitLabel.setVisible(mappings.length() == 0);
    unitPanel.setVisible(mappings.length() > 0);
    units.setVisible(mappings.length() > 0);
    identifierAsIs.setVisible(mappings.length() > 0);
    identifierSharedWithUnit.setVisible(mappings.length() > 0);
  }

  @Override
  public String getSelectedIdentifiersMapping() {
    return units.getItemText(units.getSelectedIndex());
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
  public void setIdentifiersMappingEnabled(boolean enabled) {
    units.setEnabled(enabled);
  }

  @Override
  public void setUnitRadiosEnabled(boolean enabled) {
    identifierAsIs.setEnabled(enabled);
    identifierSharedWithUnit.setEnabled(enabled);
    if(enabled) {
      unitSection.removeStyleName("gwt-RadioButton-disabled");
    } else {
      unitSection.addStyleName("gwt-RadioButton-disabled");
    }
  }

  @Override
  public boolean isIncremental() {
    return incremental.getValue();
  }

  @Override
  public Integer getLimit() {
    if (!limit.isEnabled()) return null;

    Long value = limit.getNumberValue();
    return value == null ? null : value.intValue();
  }

  @UiHandler("limitCheck")
  public void onLimitCheck(ValueChangeEvent<Boolean> event) {
    limit.setEnabled(event.getValue());
  }

}
