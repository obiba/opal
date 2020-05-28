
/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.view;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.presenter.AddVariablesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.AddVariablesModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePanel;
import org.obiba.opal.web.gwt.app.client.ui.VariablesTemplateDownloadPanel;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AddVariablesModalView extends ModalPopupViewWithUiHandlers<AddVariablesModalUiHandlers>
    implements AddVariablesModalPresenter.Display {

  @UiField
  Modal modal;

  @UiField
  OpalSimplePanel fileSelectionPanel;

  @UiField
  ControlGroup fileSelectionGroup;

  @UiField
  VariablesTemplateDownloadPanel variableTemplatePanel;

  interface Binder extends UiBinder<Widget, AddVariablesModalView> {}

  @Inject
  AddVariablesModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.addVariables());
  }

  @Override
  public void setFileSelectionDisplay(FileSelectionPresenter.Display display) {
    fileSelectionPanel.setWidget(display.asWidget());
    display.setFieldWidth("20em");
  }

  @Override
  public void closeDialog() {
    modal.hide();
  }

  @Override
  public void clearErrors() {
    modal.closeAlerts();
  }

  @UiHandler("saveButton")
  public void onSaveButton(ClickEvent event) {
    getUiHandlers().save();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    closeDialog();
  }

  @Override
  public void showError(FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case FILE_SELECTION:
          group = fileSelectionGroup;
          break;
      }
    }

    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  @UiHandler("variableTemplatePanel")
  public void onClickEvent(ClickEvent event) {
    getUiHandlers().downloadTemplate();
  }
}
