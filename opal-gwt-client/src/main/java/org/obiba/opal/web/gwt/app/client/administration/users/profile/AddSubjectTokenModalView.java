/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.users.profile;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.SubjectTokenDto;

public class AddSubjectTokenModalView extends ModalPopupViewWithUiHandlers<AddSubjectTokenModalUiHandlers> implements AddSubjectTokenModalPresenter.Display {

  interface Binder extends UiBinder<Widget, AddSubjectTokenModalView> {
  }

  @UiField
  Modal dialog;

  @UiField
  Button cancelButton;

  @UiField
  ControlGroup nameGroup;

  @UiField
  TextBox nameText;

  @UiField
  TextBox tokenText;

  private String tokenValue;

  @Inject
  public AddSubjectTokenModalView(EventBus eventBus, Binder binder, Translations translations) {
    super(eventBus);
    initWidget(binder.createAndBindUi(this));
    dialog.setTitle(translations.addTokenModalTitle());
    tokenText.setStyleName("password-vertical-align");
    tokenText.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        tokenText.setText(tokenValue);
      }
    });
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    dialog.hide();
  }


  @UiHandler("saveButton")
  public void onDownload(ClickEvent event) {
    SubjectTokenDto token = SubjectTokenDto.create();
    token.setName(nameText.getText());
    token.setToken(tokenText.getText());
    getUiHandlers().onCreateToken(token);
  }

  @UiHandler("copyTokenButton")
  public void onGenerateToken(ClickEvent event) {
    tokenText.setFocus(true);
    tokenText.selectAll();
    copyToClipboard();
  }

  @Override
  public void onShow() {
    tokenValue = getUiHandlers().onGenerateToken();
    tokenText.setText(tokenValue);
  }

  @Override
  public void showError(String message) {
    dialog.closeAlerts();
    dialog.addAlert(message, AlertType.ERROR, nameGroup);
  }

  private static native boolean copyToClipboard() /*-{
      return $doc.execCommand('copy');
  }-*/;
}
