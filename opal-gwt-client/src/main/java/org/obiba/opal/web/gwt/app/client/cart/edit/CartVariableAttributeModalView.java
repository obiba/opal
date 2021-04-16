/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart.edit;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.github.gwtbootstrap.client.ui.ProgressBar;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.TaxonomySelector;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import java.util.List;

public class CartVariableAttributeModalView extends ModalPopupViewWithUiHandlers<CartVariableAttributeModalUiHandlers>
    implements CartVariableAttributeModalPresenter.Display {

  interface Binder extends UiBinder<Widget, CartVariableAttributeModalView> {
  }

  private final Translations translations;

  @UiField
  Modal dialog;

  @UiField
  Paragraph information;

  @UiField
  ProgressBar progress;

  @UiField
  HelpBlock progressHelp;

  @UiField
  TaxonomySelector taxonomySelector;

  @UiField
  Button closeButton;

  @UiField
  Button saveButton;

  @Inject
  public CartVariableAttributeModalView(Binder uiBinder, EventBus eventBus, Translations translations) {
    super(eventBus);
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    dialog.setTitle("");
    information.setText("");
  }

  @UiHandler("closeButton")
  void onClose(ClickEvent event) {
    dialog.hide();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    if (taxonomySelector.hasVocabularyTerm())
      getUiHandlers().onSubmit(taxonomySelector.getTaxonomy(), taxonomySelector.getVocabulary(), taxonomySelector.getTerm());
    else
      getUiHandlers().onSubmit(taxonomySelector.getTaxonomy(), taxonomySelector.getVocabulary(), taxonomySelector.getValues());
  }

  @Override
  public void setMode(boolean apply) {
    taxonomySelector.setTermSelectionOptional(!apply);
    if (apply) {
      dialog.setTitle(translations.applyAnnotation());
      information.setText(translations.applyAnnotationHelp());
    }
    else {
      dialog.setTitle(translations.removeAnnotation());
      information.setText(translations.removeAnnotationsHelp());
    }
  }

  @Override
  public void setTaxonomies(List<TaxonomyDto> taxonomies) {
    taxonomySelector.setTaxonomies(taxonomies);
  }

  @Override
  public void setLocales(JsArrayString locales) {
    taxonomySelector.setLocales(locales);
  }

  @Override
  public void setProgress(String messageKey, String tableRef, int count, int percent) {
    String message = translations.userMessageMap().get(messageKey).replace("{0}", "" + count).replace("{1}", tableRef);
    progress.setPercent(percent);
    progressHelp.setText(message);
  }

  @Override
  public void showError(String message) {
    setBusy(false);
    if (Strings.isNullOrEmpty(message)) return;

    String msg = message;
    try {
      ClientErrorDto errorDto = JsonUtils.unsafeEval(message);
      msg = errorDto.getStatus();
      if (translations.userMessageMap().containsKey(msg)) msg = translations.userMessageMap().get(errorDto.getStatus());
    } catch (Exception ignored) {
    }
    dialog.addAlert(msg, AlertType.ERROR);
  }

  @Override
  public void setBusy(boolean busy) {
    dialog.setBusy(busy);
    dialog.setCloseVisible(!busy);
    saveButton.setEnabled(!busy);
    closeButton.setEnabled(!busy);
    taxonomySelector.setBusy(busy);
    progress.setVisible(busy);
    progressHelp.setVisible(busy);
    if (busy) {
      progress.setPercent(0);
      progressHelp.setText("");
    }
  }

}
