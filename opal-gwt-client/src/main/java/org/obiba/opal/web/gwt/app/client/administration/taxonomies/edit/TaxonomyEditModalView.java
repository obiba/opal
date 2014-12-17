package org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedEditor;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class TaxonomyEditModalView extends ModalPopupViewWithUiHandlers<TaxonomyEditModalUiHandlers>
    implements TaxonomyEditModalPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, TaxonomyEditModalView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  Modal modal;

  @UiField
  TextBox name;

  @UiField
  LocalizedEditor taxonomyTitles;

  @UiField
  LocalizedEditor taxonomyDescriptions;

  @Inject
  public TaxonomyEditModalView(EventBus eventBus) {
    super(eventBus);
    uiBinder.createAndBindUi(this);
    modal.setTitle(translations.addTaxonomy());
  }

  @Override
  public Widget asWidget() {
    return modal;
  }

  @Override
  public void setMode(TaxonomyEditModalPresenter.EDIT_MODE editionMode) {
    modal.setTitle(editionMode == TaxonomyEditModalPresenter.EDIT_MODE.CREATE
        ? translations.addTaxonomy()
        : translations.editTaxonomy());
  }

  @Override
  public void setTaxonomy(TaxonomyDto taxonomy, JsArrayString locales) {
    name.setText(taxonomy.getName());
    taxonomyTitles.setLocaleTexts(taxonomy.getTitleArray(), JsArrays.toList(locales));
    taxonomyDescriptions.setLocaleTexts(taxonomy.getDescriptionArray(), JsArrays.toList(locales));
  }

  @UiHandler("save")
  void onSave(ClickEvent event) {
    getUiHandlers().onSave(name.getText(), taxonomyTitles.getLocaleTexts(), taxonomyDescriptions.getLocaleTexts());
  }

  @UiHandler("cancel")
  void onCancel(ClickEvent event) {
    modal.hide();
  }


  @Override
  public void showError(@Nullable TaxonomyEditModalPresenter.Display.FormField formField, String message) {
    modal.addAlert(message, AlertType.ERROR);
  }



}
