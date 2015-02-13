/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.view;

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldMethodPresenter.Display;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldMethodPresenter.MethodType;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldMethodPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldMethodUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class DataShieldMethodView extends ModalPopupViewWithUiHandlers<DataShieldMethodUiHandlers> implements Display {

  interface Binder extends UiBinder<Widget, DataShieldMethodView> {}

  @UiField
  Modal dialog;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox name;

  @UiField
  ListBox typeList;

  @UiField
  ControlLabel scriptLabel;

  @UiField
  TextArea script;

  @UiField
  ControlLabel functionLabel;

  @UiField
  TextBox function;

  private final Translations translations;

  //
  // Constructors
  //

  @Inject
  public DataShieldMethodView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initWidgets();
  }

  private void initWidgets() {
    dialog.hide();
    typeList.addItem(translations.rFunctionLabel(), MethodType.RFUNCTION.toString());
    typeList.addItem(translations.rScriptLabel(), MethodType.RSCRIPT.toString());
    script.setVisible(false);
    typeList.setSelectedIndex(0);
    typeList.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        updateForm(getType());
      }
    });
  }

  private void updateForm(MethodType type) {
    script.setVisible(type == MethodType.RSCRIPT);
    scriptLabel.setVisible(type == MethodType.RSCRIPT);
    function.setVisible(type == MethodType.RFUNCTION);
    functionLabel.setVisible(type == MethodType.RFUNCTION);
  }

  @Override
  public void onShow() {
    name.setFocus(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDialogMode(Mode dialogMode) {
    name.setEnabled(Mode.CREATE == dialogMode);
    typeList.setEnabled(Mode.CREATE == dialogMode);
    if(Mode.CREATE == dialogMode) {
      dialog.setTitle(translations.addDataShieldMethod());
    } else {
      dialog.setTitle(translations.editDataShieldMethod());
    }
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancel(ClickEvent event) {
    getUiHandlers().cancel();
  }

  @Override
  public void setName(String name) {
    this.name.setText(name != null ? name : "");
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public HasText getScript() {
    return script;
  }

  @Override
  public void setType(MethodType type) {
    for(int i = 0; i < typeList.getItemCount(); i++) {
      if(typeList.getValue(i).equals(type.toString())) {
        typeList.setSelectedIndex(i);
        break;
      }
    }
    updateForm(type);
  }

  @Override
  public void setScript(String method) {
    script.setText(method);
  }

  @Override
  public void clear() {
    name.setText("");
    typeList.setSelectedIndex(0);
    script.setText("");
    function.setText("");
    updateForm(MethodType.RFUNCTION);
    typeList.setSelectedIndex(0);
  }

  @Override
  public HasValue<Boolean> isFunction() {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        return getType() == MethodType.RFUNCTION;
      }
    };
  }

  @Override
  public HasValue<Boolean> isScript() {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        return getType() == MethodType.RSCRIPT;
      }
    };
  }

  @Override
  public void setFunction(String func) {
    function.setText(func);
  }

  @Override
  public HasText getFunction() {
    return function;
  }

  private MethodType getType() {
    return MethodType.valueOf(typeList.getValue(typeList.getSelectedIndex()));
  }

}
