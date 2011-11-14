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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DataShieldMethodView extends Composite implements Display {

  @UiTemplate("DataShieldMethodView.ui.xml")
  interface DataShieldMethodViewUiBinder extends UiBinder<DialogBox, DataShieldMethodView> {
  }

  private static DataShieldMethodViewUiBinder uiBinder = GWT.create(DataShieldMethodViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel contentLayout;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox name;

  @UiField
  ListBox typeList;

  @UiField
  Label scriptLabel;

  @UiField
  TextArea script;

  @UiField
  Label functionLabel;

  @UiField
  TextBox function;

  //
  // Constructors
  //

  public DataShieldMethodView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWidgets();
  }

  private void initWidgets() {
    dialog.hide();
    resizeHandle.makeResizable(contentLayout);
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
    script.setVisible(type.equals(MethodType.RSCRIPT));
    scriptLabel.setVisible(type.equals(MethodType.RSCRIPT));
    function.setVisible(type.equals(MethodType.RFUNCTION));
    functionLabel.setVisible(type.equals(MethodType.RFUNCTION));
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
    name.setFocus(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setDialogMode(Mode dialogMode) {
    name.setEnabled(Mode.CREATE.equals(dialogMode));
    typeList.setEnabled(Mode.CREATE.equals(dialogMode));
    if(Mode.CREATE.equals(dialogMode)) {
      dialog.setText(translations.addDataShieldMethod());
    } else {
      dialog.setText(translations.editDataShieldMethod());
    }
  }

  @Override
  public HasClickHandlers getSaveButton() {
    return saveButton;
  }

  @Override
  public HasClickHandlers getCancelButton() {
    return cancelButton;
  }

  @Override
  public HasCloseHandlers getDialog() {
    return dialog;
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
    this.script.setText(method);
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
        return getType().equals(MethodType.RFUNCTION);
      }
    };
  }

  @Override
  public HasValue<Boolean> isScript() {
    return new HasBooleanValue() {
      @Override
      public Boolean getValue() {
        return getType().equals(MethodType.RSCRIPT);
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
