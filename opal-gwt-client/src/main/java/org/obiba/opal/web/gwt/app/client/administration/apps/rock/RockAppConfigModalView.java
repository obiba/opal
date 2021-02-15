/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.apps.rock;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.administration.idproviders.edit.IDProviderPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.RockAppConfigDto;

import javax.annotation.Nullable;

public class RockAppConfigModalView extends ModalPopupViewWithUiHandlers<RockAppConfigModalUiHandlers> implements RockAppConfigModalPresenter.Display {

  interface Binder extends UiBinder<Widget, RockAppConfigModalView> {
  }

  @UiField
  Modal dialog;

  @UiField
  ControlGroup urlGroup;

  @UiField
  Button cancelButton;

  @UiField
  TextBox urlText;

  @Inject
  protected RockAppConfigModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.updateTokenModalTitle());
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  public void onSave(ClickEvent event) {
    getUiHandlers().onSave(urlText.getText().trim());
  }

  @Override
  public void renderConfig(RockAppConfigDto rockConfig) {
    urlText.setText(rockConfig.getHost());
    urlText.setEnabled(false);
  }

  @Override
  public void showError(@Nullable FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case HOST:
          group = urlGroup;
          break;
      }
    }
    if(group == null) {
      dialog.addAlert(message, AlertType.ERROR);
    } else {
      dialog.addAlert(message, AlertType.ERROR, group);
    }
  }
}
