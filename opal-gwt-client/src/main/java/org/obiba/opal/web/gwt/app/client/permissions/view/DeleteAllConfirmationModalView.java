/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.permissions.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.DeleteAllConfirmationModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.DeleteAllConfirmationModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.Subject;

import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class DeleteAllConfirmationModalView extends ModalPopupViewWithUiHandlers<DeleteAllConfirmationModalUiHandlers>
    implements DeleteAllConfirmationModalPresenter.Display {

  interface Binder extends UiBinder<Widget, DeleteAllConfirmationModalView> {}

  private final Translations translations;

  @UiField
  Modal dialog;

  @UiField
  Paragraph message;

  @Inject
  public DeleteAllConfirmationModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    dialog.setTitle(translations.removeAllSubjectPermissionsModalTile());
  }

  @Override
  public void setData(Subject subject) {
    message.setText(TranslationsUtils.replaceArguments(translations.removeAllSubjectPermissionsModalMessage(),
        translations.shortSubjectTypeMap().get(subject.getType().getName()).toLowerCase(), subject.getPrincipal()));
  }

  @Override
  public void close() {
    dialog.hide();
  }

  @UiHandler("yesButton")
  public void onOkButtonClicked(ClickEvent event) {
    getUiHandlers().deleteAll();
  }

  @UiHandler("noButton")
  public void onCloseButtonClicked(ClickEvent event) {
    close();
  }

}