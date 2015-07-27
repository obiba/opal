package org.obiba.opal.web.gwt.app.client.administration.taxonomies.git;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class TaxonomyGitImportModalView extends ModalPopupViewWithUiHandlers<TaxonomyGitImportModalUiHandlers>
    implements TaxonomyGitImportModalPresenter.Display {

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

  @UiHandler("importRepo")
  void onSave(ClickEvent event) {
    getUiHandlers().onImport(user.getText(), repository.getText(), reference.getText(), file.getText());
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
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public void showError(String messageKey) {
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

