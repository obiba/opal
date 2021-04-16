/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.project.genotypes;

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
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePanel;

import javax.annotation.Nullable;

public class ProjectImportVcfFileModalView extends ModalPopupViewWithUiHandlers<ProjectImportVcfFileModalUiHandlers>
        implements ProjectImportVcfFileModalPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectImportVcfFileModalView> {}

  @UiField
  Modal dialog;

  @UiField
  Button cancelButton;

  @UiField
  OpalSimplePanel vcfFilePanel;

  @UiField
  ControlGroup fileGroup;

  @Inject
  public ProjectImportVcfFileModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.importVcfModalTitle());
  }

  @Override
  public void onShow() {
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("importButton")
  public void importButtonClick(ClickEvent event) {
    getUiHandlers().onImport();
  }

  @Override
  public void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display) {
    vcfFilePanel.setWidget(display.asWidget());
    display.setFieldWidth("20em");
  }

  @Override
  public void clearErrors() {
    dialog.clearAlert();
  }

  @Override
  public void showError(String message) {
    dialog.addAlert(message, AlertType.ERROR, fileGroup);
  }
}
