/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.packages;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.packages.DataShieldPackageInstallModalPresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

/**
 *
 */
public class DataShieldPackageInstallModalView extends ModalPopupViewWithUiHandlers<DataShieldPackageInstallModalUiHandlers>
    implements Display {

  interface Binder extends UiBinder<Modal, DataShieldPackageInstallModalView> {
  }

  private static final String CRAN_REPO = "cran";
  private static final String GITHUB_REPO = "gh";

  @UiField
  Modal dialog;

  @UiField
  Chooser repository;

  @UiField
  Button installButton;

  @UiField
  Button cancelButton;

  @UiField
  RadioButton allPkg;

  @UiField
  Panel cranPanel;

  @UiField
  RadioButton namedPkg;

  @UiField
  ControlGroup cranNameGroup;

  @UiField
  TextBox cranName;

  @UiField
  Panel ghPanel;

  @UiField
  ControlGroup ghOrgGroup;

  @UiField
  TextBox ghOrg;

  @UiField
  ControlGroup ghNameGroup;

  @UiField
  TextBox ghName;

  @UiField
  TextBox reference;

  private final Translations translations;

  //
  // Constructors
  //

  @Inject
  public DataShieldPackageInstallModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initWidgets();
  }

  private void initWidgets() {
    repository.addItem("CRAN", CRAN_REPO);
    repository.addItem("GitHub", GITHUB_REPO);
    repository.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent event) {
        String repo = repository.getSelectedValue();
        cranPanel.setVisible(CRAN_REPO.equals(repo));
        ghPanel.setVisible(GITHUB_REPO.equals(repo));
      }
    });
    allPkg.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        cranName.setEnabled(!allPkg.getValue());
      }
    });
    namedPkg.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        cranName.setEnabled(namedPkg.getValue());
      }
    });
    allPkg.setValue(true, true);
    cranName.setEnabled(false);
  }

  @Override
  public void onShow() {
    dialog.setTitle(translations.addDataShieldPackage());
    cranName.setFocus(true);
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
    dialog.closeAlerts();
    String repo = repository.getSelectedValue();
    switch (repo) {
      case CRAN_REPO:
        if (allPkg.getValue()) {
          getUiHandlers().installCRANPackage(DATASHIELD_ALL_PKG);
        } else {
          if (Strings.isNullOrEmpty(cranName.getText())) {
            dialog.addAlert(translations.userMessageMap().get("NameIsRequired"), AlertType.ERROR, cranNameGroup);
            return;
          }
          getUiHandlers().installCRANPackage(cranName.getText());
        }
        break;
      case GITHUB_REPO:
        boolean hasError = false;
        if (Strings.isNullOrEmpty(ghOrg.getText())) {
          dialog.addAlert(translations.userMessageMap().get("GHOrganizationIsRequired"), AlertType.ERROR, ghOrgGroup);
          hasError = true;
        }
        if (Strings.isNullOrEmpty(ghName.getText())) {
          dialog.addAlert(translations.userMessageMap().get("NameIsRequired"), AlertType.ERROR, ghNameGroup);
          hasError = true;
        }
        if (hasError) return;
        getUiHandlers().installGithubPackage(ghOrg.getText() + "/" + ghName.getText(), reference.getText());
        break;
    }
  }

  @Override
  public void clear() {
    cranName.setText("");
    reference.setText("");
    setInProgress(false);
    allPkg.setValue(true, true);
    namedPkg.setValue(false, true);
  }

  @Override
  public void setInProgress(boolean progress) {
    dialog.setBusy(progress);
    installButton.setEnabled(!progress);
    cancelButton.setEnabled(!progress);
  }
}
