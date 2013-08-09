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

import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageCreatePresenter.Display;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageCreateUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 *
 */
public class DataShieldPackageCreateView extends ModalPopupViewWithUiHandlers<DataShieldPackageCreateUiHandlers>
    implements Display {

  @UiTemplate("DataShieldPackageCreateView.ui.xml")
  interface DataShieldPackageCreateViewUiBinder extends UiBinder<Modal, DataShieldPackageCreateView> {}

  private static final DataShieldPackageCreateViewUiBinder uiBinder = GWT
      .create(DataShieldPackageCreateViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  Modal dialog;

  @UiField
  Button installButton;

  @UiField
  Button cancelButton;

  @UiField
  RadioButton allPkg;

  @UiField
  RadioButton namedPkg;

  @UiField
  TextBox name;

  @UiField
  TextBox reference;

  //
  // Constructors
  //

  @Inject
  public DataShieldPackageCreateView(EventBus eventBus) {
    super(eventBus);
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
  }

  private void initWidgets() {
    allPkg.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        name.setEnabled(!event.getValue());
      }
    });
    namedPkg.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        name.setEnabled(event.getValue());
      }
    });
    allPkg.setValue(true, true);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void show() {
    dialog.setTitle(translations.addDataShieldPackage());
    name.setFocus(true);
    super.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    hideDialog();
  }

  @UiHandler("installButton")
  public void onInstallButton(ClickEvent event) {
    getUiHandlers().installPackage();
  }

  @Override
  public void setName(String name) {
    this.name.setText(name != null ? name : "");
  }

  @Override
  public HasText getName() {
    return new HasText() {
      @Override
      public String getText() {
        return allPkg.getValue() ? DATASHIELD_ALL_PKG : name.getText();
      }

      @Override
      public void setText(String text) {
      }
    };
  }

  @Override
  public HasText getReference() {
    return reference;
  }

  @Override
  public void clear() {
    name.setText("");
    reference.setText("");
    setInstallButtonEnabled(true);
    setCancelButtonEnabled(true);
    allPkg.setValue(true, true);
    namedPkg.setValue(false, true);
  }

  @Override
  public void setInstallButtonEnabled(boolean b) {
    installButton.setEnabled(b);
    if (b) {
      dialog.removeStyleName("progress");
    } else {
      dialog.addStyleName("progress");
    }
  }

  @Override
  public void setCancelButtonEnabled(boolean b) {
    cancelButton.setEnabled(b);
  }
}
