/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.add;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePanel;

public class TaxonomyFileImportModalView extends ModalPopupViewWithUiHandlers<TaxonomyFileImportModalUiHandlers>
    implements TaxonomyFileImportModalPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, TaxonomyFileImportModalView> {
  }

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal modal;

  @UiField
  OpalSimplePanel filePanel;

  @UiField
  CheckBox overrideExisting;

  @Inject
  public TaxonomyFileImportModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.importFileTaxonomy());
  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @UiHandler("importFile")
  void onSave(ClickEvent event) {
    clearErrors();
    getUiHandlers().onImportFile(overrideExisting.getValue());
  }

  @UiHandler("cancel")
  void onCancel(ClickEvent event) {
    modal.hide();
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display) {
    filePanel.setWidget(display.asWidget());
    display.setFieldWidth("20em");
  }

  @Override
  public void showError(String messageKey) {
    modal.addAlert(translations.userMessageMap().get(messageKey), AlertType.ERROR);
  }

  @Override
  public void clearErrors() {
    modal.clearAlert();
  }
}

