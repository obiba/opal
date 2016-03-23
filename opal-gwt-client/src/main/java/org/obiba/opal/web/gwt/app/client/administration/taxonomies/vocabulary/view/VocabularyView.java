package org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.LocaleTextColumn;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class VocabularyView extends ViewWithUiHandlers<VocabularyUiHandlers> implements VocabularyPresenter.Display {

  private final TranslationMessages translationMessages;

  private final Translations translations;

  interface ViewUiBinder extends UiBinder<Widget, VocabularyView> {}

  @UiField
  IconAnchor back;

  @UiField
  Button previous;

  @UiField
  Button next;

  @UiField
  Heading vocabularyName;

  @UiField
  IconAnchor edit;

  @UiField
  Button remove;

  @UiField
  Button addTerm;

  @UiField
  Panel titlePanel;

  @UiField
  Panel descriptionPanel;

  @UiField
  Label repeatable;

  @UiField
  Table<TermDto> table;

  @UiField
  OpalSimplePager pager;

  @UiField
  TextBoxClearable filter;

  private ActionsColumn<TermDto> actions;

  private final ListDataProvider<TermDto> dataProvider = new ListDataProvider<>();

  @Inject
  public VocabularyView(ViewUiBinder viewUiBinder, Translations translations, TranslationMessages translationMessages) {
    this.translationMessages = translationMessages;
    this.translations = translations;
    initWidget(viewUiBinder.createAndBindUi(this));
    initializeTermsTable();
    initializeFilter();
  }

  private void initializeTermsTable() {
    dataProvider.addDataDisplay(table);


    table.addColumn(new LocaleTextColumn<TermDto>() {
      @Override
      protected JsArray<LocaleTextDto> getLocaleText(TermDto term) {
        return term.getKeywordsArray();
      }

      @Override
      public String getValue(TermDto object) {
        return object.getName() + super.getValue(object);
      }
    }, translations.nameKeywordsLabel());
    table.addColumn(new LocaleTextColumn<TermDto>() {
      @Override
      protected JsArray<LocaleTextDto> getLocaleText(TermDto term) {
        return term.getTitleArray();
      }
    }, translations.titleLabel());
    table.addColumn(new LocaleTextColumn<TermDto>() {
      @Override
      protected JsArray<LocaleTextDto> getLocaleText(TermDto term) {
        return term.getDescriptionArray();
      }
    }, translations.descriptionLabel());
    actions = new ActionsColumn<>(new ActionHandler<TermDto>() {
      @Override
      public void doAction(TermDto object, String actionName) {
        if (ActionsColumn.EDIT_ACTION.equals(actionName)) {
          getUiHandlers().onEditTerm(object);
        } else {
          getUiHandlers().onDeleteTerm(object);
        }
      }
    });

    //table.setSelectionModel(new SingleSelectionModel<VocabularyDto>());
    table.setPageSize(Table.DEFAULT_PAGESIZE);
    table.setEmptyTableWidget(new com.google.gwt.user.client.ui.InlineLabel(translationMessages.termCount(0)));
    pager.setDisplay(table);
  }

  private void initializeFilter() {
    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterTerms());
    filter.getTextBox().addStyleName("input-xlarge");
    filter.getClear().setTitle(translations.clearFilter());
  }

  @UiHandler("remove")
  public void onDelete(ClickEvent event) {
    getUiHandlers().onDelete();
  }

  @UiHandler("previous")
  public void onPrevious(ClickEvent event) {
    getUiHandlers().onPrevious();
  }

  @UiHandler("next")
  public void onNext(ClickEvent event) {
    getUiHandlers().onNext();
  }

  @UiHandler("edit")
  void onEdit(ClickEvent event) {
    getUiHandlers().onEdit();
  }

  @UiHandler("back")
  void onBack(ClickEvent event) {
    getUiHandlers().onTaxonomySelected();
  }

  @UiHandler("filter")
  void onFilterUpdate(KeyUpEvent event) {
    getUiHandlers().onFilterUpdate(filter.getText());
  }

  @UiHandler("addTerm")
  void onAddTerm(ClickEvent event) {
    getUiHandlers().onAddTerm();
  }

  @Override
  public void renderVocabulary(TaxonomyDto taxonomy, VocabularyDto vocabulary) {
    back.setTitle(taxonomy.getName());
    vocabularyName.setText(vocabulary.getName());
    renderText(titlePanel, vocabulary.getTitleArray());
    renderText(descriptionPanel, vocabulary.getDescriptionArray());
    repeatable.setText(vocabulary.getRepeatable() ? translations.yesLabel() : translations.noLabel());
    renderTerms(vocabulary.getTermsArray());

    int idx = getVocabularyIndex(taxonomy, vocabulary);
    previous.setEnabled(idx > 0);
    next.setEnabled(idx < taxonomy.getVocabulariesCount() - 1);
  }

  @Override
  public void renderTerms(JsArray<TermDto> terms) {
    dataProvider.setList(JsArrays.toList(terms));
    dataProvider.refresh();
    pager.setPagerVisible(table.getRowCount() > Table.DEFAULT_PAGESIZE);
  }

  @Override
  public void setEditable(boolean editable) {
    remove.setVisible(editable);
    edit.setVisible(editable);
    addTerm.setVisible(editable);
    if (table.getColumnIndex(actions)>0) table.removeColumn(actions);
    if (editable) table.addColumn(actions, translations.actionsLabel());
  }

  private int getVocabularyIndex(TaxonomyDto taxonomy, VocabularyDto vocabulary) {
    for(int i = 0; i < taxonomy.getVocabulariesCount(); i++) {
      VocabularyDto current = taxonomy.getVocabularies(i);
      if(current.getName().equals(vocabulary.getName())) {
        return i;
      }
    }
    return -1;
  }

  private void renderText(Panel panel, JsArray<LocaleTextDto> texts) {
    panel.clear();
    for(LocaleTextDto text : JsArrays.toIterable(texts)) {
      FlowPanel localePanel = new FlowPanel();
      localePanel.setStyleName("small-bottom-margin");
      panel.add(localePanel);
      InlineLabel locale = new InlineLabel(text.getLocale());
      locale.setStyleName("label small-right-indent");
      localePanel.add(locale);
      InlineLabel label = new InlineLabel(text.getText());
      localePanel.add(label);
    }
  }

}