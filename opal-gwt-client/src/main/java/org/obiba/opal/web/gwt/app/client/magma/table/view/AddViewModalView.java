/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.table.view;

import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.AddViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.AddViewModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePanel;
import org.obiba.opal.web.gwt.app.client.ui.TableChooser;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class AddViewModalView extends ModalPopupViewWithUiHandlers<AddViewModalUiHandlers>
    implements AddViewModalPresenter.Display {

  interface Binder extends UiBinder<Widget, AddViewModalView> {}

  @UiField
  Modal modal;

  @UiField
  ControlGroup viewNameGroup;

  @UiField
  TextBox viewName;

  @UiField
  ControlGroup tablesGroup;

  @UiField(provided = true)
  final TableChooser tables;

  @UiField
  ControlGroup fileSelectionGroup;

  @UiField
  OpalSimplePanel fileSelectionPanel;

  private final Translations translations;

  @Inject
  public AddViewModalView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    this.translations = translations;
    tables = new TableChooser(true);
    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle(translations.addViewTitle());
  }

  @Override
  protected Modal asModal() {
    return modal;
  }

  @Override
  public void setFileSelectionDisplay(FileSelectionPresenter.Display display) {
    fileSelectionPanel.setWidget(display.asWidget());
    display.setFieldWidth("20em");
  }

  @Override
  public HasText getViewName() {
    return viewName;
  }

  @Override
  public void addTableSelections(JsArray<TableDto> tableDtos) {
    tables.addTableSelections(tableDtos);
  }

  @Override
  public List<TableDto> getSelectedTables() {
    return tables.getSelectedTables();
  }

  @Override
  public void closeDialog() {
    modal.hide();
  }

  @Override
  public void clearErrors() {
    modal.clearAlert(viewNameGroup);
    modal.clearAlert(tablesGroup);
    modal.clearAlert(fileSelectionGroup);
  }

  @Override
  public void showError(String messageKey) {
    modal.addAlert(translations.userMessageMap().get(messageKey), AlertType.ERROR);
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case VIEW_NAME:
          group = viewNameGroup;
          break;
        case TABLES:
          group = tablesGroup;
          break;
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

  @UiHandler("cancelButton")
  public void onCancelButtonClicked(ClickEvent event) {
    closeDialog();
  }

  @UiHandler("saveButton")
  public void onCreateButtonClicked(ClickEvent event) {
    getUiHandlers().createView();
  }
}
