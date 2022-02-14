/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.project.resources;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.magma.ResourceViewDto;
import org.obiba.opal.web.model.client.magma.ViewDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import javax.annotation.Nullable;

public class ResourceViewModalView extends ModalPopupViewWithUiHandlers<ResourceViewModalUiHandlers>
    implements ResourceViewModalPresenter.Display {

  interface Binder extends UiBinder<Widget, ResourceViewModalView> {
  }

  @UiField
  Modal dialog;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox name;

  @UiField
  TextBox idColumn;

  @UiField
  TextBox entityType;

  @UiField
  Button saveButton;

  @UiField
  Button closeButton;

  private Translations translations;

  private TranslationMessages translationMessages;

  @Inject
  public ResourceViewModalView(Binder uiBinder, EventBus eventBus, Translations translations, TranslationMessages translationMessages) {
    super(eventBus);
    this.translations = translations;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle(translations.addViewTitle());
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    getUiHandlers().onSave(getName().getText(),
        Strings.isNullOrEmpty(entityType.getText()) ? "Participant" : entityType.getText(),
        idColumn.getText());
  }

  @Override
  public void setName(String resourceName) {
    name.setText(resourceName);
  }

  @Override
  public void renderProperties(ViewDto view) {
    name.setText(view.getName());
    ResourceViewDto resDto = (ResourceViewDto) view.getExtension(ResourceViewDto.ViewDtoExtensions.view);
    idColumn.setText(resDto.getIdColumn());
    entityType.setText(resDto.getEntityType());
    dialog.setTitle(translations.editProperties());
  }

  @Override
  public void showError(String message, @Nullable FormField group) {
    if (Strings.isNullOrEmpty(message)) return;

    dialog.closeAlerts();
    String msg = message;
    try {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(message);
      msg = errorDto.getStatus();
    } catch (Exception ignored) {
    }

    if (group == null) {
      dialog.addAlert(msg, AlertType.ERROR);
    } else if (group.equals(FormField.NAME)) {
      dialog.addAlert(msg, AlertType.ERROR, nameGroup);
    }
  }

  @Override
  public void setInProgress(boolean progress) {
    dialog.setBusy(progress);
    saveButton.setEnabled(!progress);
    closeButton.setEnabled(!progress);
  }

  @Override
  public HasText getName() {
    return name;
  }

}
