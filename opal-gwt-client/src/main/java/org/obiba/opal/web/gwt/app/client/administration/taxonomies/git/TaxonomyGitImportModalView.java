/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.git;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class TaxonomyGitImportModalView extends ModalPopupViewWithUiHandlers<TaxonomyGitImportModalUiHandlers>
    implements TaxonomyGitImportModalPresenter.Display {

  private boolean tagMode = false;

  interface ViewUiBinder extends UiBinder<Widget, TaxonomyGitImportModalView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal modal;

  @UiField
  TextBox user;

  @UiField
  TextBox repository;

  @UiField
  TextBox reference;

  @UiField
  TextBox file;

  @UiField
  ControlGroup userGroup;

  @UiField
  ControlGroup repositoryGroup;

  @UiField
  ControlGroup downloadKeyGroup;

  @UiField
  ControlGroup overrideGroup;

  @UiField
  CheckBox overrideExisting;

  @UiField
  Chooser tags;

  @UiField
  FlowPanel filePanel;

  @UiField
  FlowPanel mrPanel;

  @UiField
  TextBox downloadKey;

  @UiField
  FlowPanel tagPanel;

  @UiField
  FlowPanel acceptedPanel;

  @UiField
  CheckBox accepted;

  @UiField
  Image fetchingTagsProgress;

  @UiField
  Button importRepo;

  @Inject
  public TaxonomyGitImportModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.importGitTaxonomy());
  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @UiHandler("accepted")
  void onAccepted(ClickEvent event) {
    importRepo.setEnabled(accepted.getValue());
  }

  @UiHandler("importRepo")
  void onSave(ClickEvent event) {
    String ref = tagMode ? tags.getSelectedValue() : reference.getText();
    getUiHandlers().onImport(user.getText(), repository.getText(), ref, file.getText(),
        overrideExisting.getValue(), downloadKey.getValue());
  }

  @UiHandler("cancel")
  void onCancel(ClickEvent event) {
    modal.hide();
  }

  @Override
  public HasText getUser() {
    return user;
  }

  @Override
  public HasText getRepository() {
    return repository;
  }

  @Override
  public HasText getDownloadKey() {
    return downloadKey;
  }

  @Override
  public void addTags(JsArrayString tagNames) {
    fetchingTagsProgress.setVisible(false);
    boolean latest = true;
    for(String name : JsArrays.toIterable(tagNames)) {
      tags.addItem(name + (latest ? " (latest)" : ""), name);
      latest = false;
    }
    tags.setEnabled(tagNames.length() > 1);
  }

  @Override
  public void showMaelstromForm(String user, String repo) {
    tagMode = true;
    modal.setTitle(translations.importMaelstromTaxonomies());
    mrPanel.setVisible(true);
    overrideExisting.setValue(true);
    overrideExisting.setVisible(false);
    acceptedPanel.setVisible(true);
    this.user.setText(user);
    repository.setText(repo);
    filePanel.setVisible(!tagMode);
    importRepo.setEnabled(!tagMode);
    tagPanel.setVisible(tagMode);
  }

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void showError(String messageKey) {
    fetchingTagsProgress.setVisible(false);
    showError(null, translations.userMessageMap().get(messageKey));
  }

  @Override
  public void showError(@Nullable TaxonomyGitImportModalPresenter.Display.FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case USER:
          group = userGroup;
          break;
        case REPOSITORY:
          group = repositoryGroup;
          break;
        case DOWNLOAD_KEY:
          group = downloadKeyGroup;
      }
    }
    if(group == null) {
      modal.addAlert(message, AlertType.ERROR);
    } else {
      modal.addAlert(message, AlertType.ERROR, group);
    }
  }

  @Override
  public void clearErrors() {
    modal.clearAlert();
  }
}

