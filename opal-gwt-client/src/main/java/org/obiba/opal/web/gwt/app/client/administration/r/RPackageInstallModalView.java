/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.r;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

public class RPackageInstallModalView extends ModalPopupViewWithUiHandlers<RPackageInstallModalUiHandlers> implements RPackageInstallModalPresenter.Display {

  interface Binder extends UiBinder<Modal, RPackageInstallModalView> {
  }

  private static final String CRAN_REPO = "cran";
  private static final String GITHUB_REPO = "gh";
  private static final String BIOC_REPO = "bioc";


  @UiField
  Modal dialog;

  @UiField
  Chooser repository;

  @UiField
  Panel cranPanel;

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

  @UiField
  Panel biocPanel;

  @UiField
  ControlGroup biocNameGroup;

  @UiField
  TextBox biocName;

  @UiField
  Button installButton;
  @UiField
  Button cancelButton;

  private final Translations translations;

  @Inject
  protected RPackageInstallModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    repository.addItem("CRAN", CRAN_REPO);
    repository.addItem("GitHub", GITHUB_REPO);
    repository.addItem("Bioconductor", BIOC_REPO);
    repository.addChosenChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent event) {
        String repo = repository.getSelectedValue();
        cranPanel.setVisible(CRAN_REPO.equals(repo));
        ghPanel.setVisible(GITHUB_REPO.equals(repo));
        biocPanel.setVisible(BIOC_REPO.equals(repo));
      }
    });
  }

  @Override
  public void onShow() {
    dialog.setTitle(translations.installRPackage());
    cranName.setFocus(true);
    repository.setSelectedValue(CRAN_REPO);
  }

  @UiHandler("installButton")
  public void onInstall(ClickEvent event) {
    dialog.closeAlerts();
    String repo = repository.getSelectedValue();
    switch (repo) {
      case CRAN_REPO:
        if (Strings.isNullOrEmpty(cranName.getText())) {
          dialog.addAlert(translations.userMessageMap().get("NameIsRequired"), AlertType.ERROR, cranNameGroup);
          return;
        }
        getUiHandlers().installCRANPackage(cranName.getText());
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
      case BIOC_REPO:
        if (Strings.isNullOrEmpty(biocName.getText())) {
          dialog.addAlert(translations.userMessageMap().get("NameIsRequired"), AlertType.ERROR, biocNameGroup);
          return;
        }
        getUiHandlers().installBiocPackage(biocName.getText());
        break;
    }
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    hideDialog();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setInProgress(boolean progress) {
    dialog.setBusy(progress);
    installButton.setEnabled(!progress);
    cancelButton.setEnabled(!progress);
  }

}
