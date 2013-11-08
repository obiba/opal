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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.CollapsiblePanel;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateModalPresenter.Display;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateModalPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateModalUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class FunctionalUnitUpdateModalView extends ModalPopupViewWithUiHandlers<FunctionalUnitUpdateModalUiHandlers>
    implements Display {

  interface FunctionalUnitUpdateModalUiBinder extends UiBinder<Widget, FunctionalUnitUpdateModalView> {}

  private static final FunctionalUnitUpdateModalUiBinder uiBinder = GWT.create(FunctionalUnitUpdateModalUiBinder
      .class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal dialog;

  @UiField
  Button updateFunctionalUnitButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox functionalUnitName;

  @UiField
  TextArea functionalUnitDescription;

  @UiField
  TextArea select;

  @UiField
  CheckBox selectEnabled;

  @UiField
  CollapsiblePanel options;

  @Inject
  public FunctionalUnitUpdateModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    selectEnabled.setValue(false);
    select.setEnabled(false);
  }

  @Override
  public Widget asWidget() {
    return dialog;
  }

  @Override
  public void show() {
    super.show();
    functionalUnitName.setFocus(true);
    options.setOpen(!select.getText().isEmpty());
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().onDialogHide();
  }

  @UiHandler("selectEnabled")
  public void onValueChanged(ValueChangeEvent<Boolean> event) {
    select.setEnabled(event.getValue());
    if(!event.getValue()) {
      select.setText("");
    }
  }

  @UiHandler("updateFunctionalUnitButton")
  public void onUpdateFunctionalUnit(ClickEvent event) {
    getUiHandlers().updateFunctionaUnit();
  }

  @Override
  public HasText getName() {
    return functionalUnitName;
  }

  @Override
  public void setName(String name) {
    functionalUnitName.setText(name != null ? name : "");
  }

  @Override
  public void setEnabledFunctionalUnitName(boolean enabled) {
    functionalUnitName.setEnabled(enabled);

  }

  @Override
  public HasText getDescription() {
    return functionalUnitDescription;
  }

  @Override
  public void setDescription(String description) {
    functionalUnitDescription.setText(description != null ? description : "");
  }

  @Override
  public void clear() {
    setName("");
    setDescription("");
    setSelect("");
  }

  @Override
  public void setSelect(String select) {
    if(select == null || select.trim().isEmpty()) {
      selectEnabled.setValue(false);
      this.select.setEnabled(false);
      this.select.setText("");
    } else {
      selectEnabled.setValue(true);
      this.select.setEnabled(true);
      this.select.setText(select);
    }
  }

  @Override
  public void setDialogMode(Mode dialogMode) {
    functionalUnitName.setEnabled(Mode.CREATE == dialogMode);
    if(Mode.CREATE == dialogMode) {
      dialog.setTitle(translations.addUnit());
    } else {
      dialog.setTitle(translations.editUnit());
    }
  }

  @Override
  public HasText getSelect() {
    return select;
  }

}
