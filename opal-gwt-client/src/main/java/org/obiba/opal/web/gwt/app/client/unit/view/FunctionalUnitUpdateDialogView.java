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
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateDialogPresenter.Display;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateDialogPresenter.Mode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class FunctionalUnitUpdateDialogView extends Composite implements Display {

  @UiTemplate("FunctionalUnitUpdateDialogView.ui.xml")
  interface FunctionalUnitUpdateDialogUiBinder extends UiBinder<DialogBox, FunctionalUnitUpdateDialogView> {
  }

  private static FunctionalUnitUpdateDialogUiBinder uiBinder = GWT.create(FunctionalUnitUpdateDialogUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  DialogBox dialog;

  @UiField
  Button updateFunctionalUnitButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox functionalUnitName;

  @UiField
  TextArea select;

  @UiField
  CheckBox selectEnabled;

  public FunctionalUnitUpdateDialogView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    dialog.hide();
    selectEnabled.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

      @Override
      public void onValueChange(ValueChangeEvent<Boolean> evt) {
        select.setEnabled(evt.getValue());
        if(!evt.getValue()) {
          select.setText("");
        }
      }
    });
    selectEnabled.setValue(false);
    select.setEnabled(false);
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
  public void showDialog() {
    dialog.center();
    dialog.show();
    functionalUnitName.setFocus(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public Button getCancelButton() {
    return cancelButton;
  }

  @Override
  public Button getUpdateFunctionalUnitButton() {
    return updateFunctionalUnitButton;
  }

  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
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
  public void clear() {
    setName("");
    setSelect("");
  }

  @Override
  public void setSelect(String select) {
    if(select == null || select.trim().length() == 0) {
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
    functionalUnitName.setEnabled(Mode.CREATE.equals(dialogMode));
    if(Mode.CREATE.equals(dialogMode)) {
      dialog.setText(translations.addUnit());
    } else {
      dialog.setText(translations.editUnit());
    }
  }

  @Override
  public HasText getSelect() {
    return select;
  }

}
