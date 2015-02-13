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

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldROptionModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldROptionModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import static org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldROptionModalPresenter.Mode;

/**
 *
 */
public class DataShieldROptionModalView extends ModalPopupViewWithUiHandlers<DataShieldROptionModalUiHandlers>
    implements DataShieldROptionModalPresenter.Display {

  interface Binder extends UiBinder<Widget, DataShieldROptionModalView> {}

  @UiField
  Modal dialog;

  @UiField
  Button saveButton;

  @UiField
  Button cancelButton;

  @UiField
  TextBox name;

  @UiField
  TextBox value;

  @UiField
  ControlGroup nameGroup;

  @UiField
  ControlGroup valueGroup;

  private final Translations translations;

  //
  // Constructors
  //

  @Inject
  public DataShieldROptionModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
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
    if(Mode.CREATE == dialogMode) {
      dialog.setTitle(translations.addROptionTitle());
    } else {
      dialog.setTitle(translations.editROptionTitle());
    }
  }

  @Override
  public void clearErrors() {
    dialog.closeAlerts();
  }

  @Override
  public void showError(String message, FormField field) {
    ControlGroup group = null;
    if(field != null) {
      switch(field) {
        case NAME:
          group = nameGroup;
          break;
        case VALUE:
          group = valueGroup;
          break;
      }
    }
    if(group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
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
  public void setName(String optionName) {
    name.setText(optionName != null ? optionName : "");
  }

  @Override
  public void setValue(String optionValue) {
    value.setText(optionValue != null ? optionValue : "");
  }

  @Override
  public HasText getName() {
    return name;
  }

  @Override
  public HasText getValue() {
    return value;
  }

  @Override
  public void clear() {
    name.setText("");
    value.setText("");
  }

}
