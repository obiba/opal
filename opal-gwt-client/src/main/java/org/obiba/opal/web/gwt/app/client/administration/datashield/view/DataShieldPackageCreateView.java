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
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.workbench.view.ResizeHandle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class DataShieldPackageCreateView extends PopupViewImpl implements Display {

  @UiTemplate("DataShieldPackageCreateView.ui.xml")
  interface DataShieldPackageCreateViewUiBinder extends UiBinder<DialogBox, DataShieldPackageCreateView> {}

  private static final DataShieldPackageCreateViewUiBinder uiBinder = GWT
      .create(DataShieldPackageCreateViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  DialogBox dialog;

  @UiField
  DockLayoutPanel contentLayout;

  @UiField
  ResizeHandle resizeHandle;

  @UiField
  Button installButton;

  @UiField
  Button cancelButton;

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
    dialog.hide();
    resizeHandle.makeResizable(contentLayout);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
  }

  @Override
  public void show() {
    dialog.setText(translations.addDataShieldPackage());
    name.setFocus(true);
    super.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HasClickHandlers getInstallButton() {
    return installButton;
  }

  @Override
  public HasClickHandlers getCancelButton() {
    return cancelButton;
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
  public HasText getReference() {
    return reference;
  }

  @Override
  public void clear() {
    name.setText("");
    reference.setText("");
    installButton.setEnabled(true);
    cancelButton.setEnabled(true);
  }

  @Override
  public void setInstallButtonEnabled(boolean b) {
    installButton.setEnabled(b);
  }

  @Override
  public void setCancelButtonEnabled(boolean b) {
    cancelButton.setEnabled(b);
  }
}
