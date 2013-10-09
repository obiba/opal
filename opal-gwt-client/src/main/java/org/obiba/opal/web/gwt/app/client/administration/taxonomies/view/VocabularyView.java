package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyUiHandlers;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.EditableTabableColumn;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.TermDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.VocabularyDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import static org.obiba.opal.web.model.client.opal.TaxonomyDto.TextDto;

public class VocabularyView extends ViewWithUiHandlers<VocabularyUiHandlers> implements VocabularyPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, VocabularyView> {}

  private ListDataProvider<JsArray<TextDto>> vocTitlesProvider = new ListDataProvider<JsArray<TextDto>>();

  private ListDataProvider<JsArray<TextDto>> vocDescriptionsProvider = new ListDataProvider<JsArray<TextDto>>();

  private ListDataProvider<JsArray<TextDto>> termTitlesProvider = new ListDataProvider<JsArray<TextDto>>();

  private ListDataProvider<JsArray<TextDto>> termDescriptionsProvider = new ListDataProvider<JsArray<TextDto>>();

  private boolean isEditMode = false;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  Button saveButton;

  @UiField
  Button editButton;

  @UiField
  Heading vocabularyName;

//  @UiField
//  FlowPanel titlePanel;

//  @UiField
//  FlowPanel descriptionPanel;

  @UiField
  FlowPanel vocabularyPanel;

  @UiField
  FlowPanel termPanel;

//  @UiField
//  InlineLabel taxonomyName;

  @UiField
  FlowPanel termsPanel;

  private TaxonomyDto taxonomy;

  private VocabularyDto vocabulary;

  private TextBox name;

  private TextBox termName;

  private Table tTitles = new Table();

  private Table tDescriptions = new Table();

  private Table termTitles = new Table();

  private Table termDescriptions = new Table();

  @Inject
  public VocabularyView(ViewUiBinder viewUiBinder) {
    initWidget(viewUiBinder.createAndBindUi(this));
    tTitles.addColumn(new TextDtoColumn("en"), "Text (en)");
    tTitles.addColumn(new TextDtoColumn("fr"), "Text (fr)");

    tDescriptions.addColumn(new TextDtoColumn("en"), "Text (en)");
    tDescriptions.addColumn(new TextDtoColumn("fr"), "Text (fr)");

    termTitles.addColumn(new TextDtoColumn("en"), "Text (en)");
    termTitles.addColumn(new TextDtoColumn("fr"), "Text (fr)");

    termDescriptions.addColumn(new TextDtoColumn("en"), "Text (en)");
    termDescriptions.addColumn(new TextDtoColumn("fr"), "Text (fr)");

    vocTitlesProvider.addDataDisplay(tTitles);
    vocDescriptionsProvider.addDataDisplay(tDescriptions);

    termTitlesProvider.addDataDisplay(termTitles);
    termDescriptionsProvider.addDataDisplay(termDescriptions);
  }

  @Override
  public void setTaxonomyAndVocabulary(TaxonomyDto taxonomy, VocabularyDto vocabulary) {
    this.taxonomy = taxonomy;
    this.vocabulary = vocabulary;
    if(taxonomy == null) {
      vocabularyPanel.clear();
      termsPanel.clear();
    } else {
      draw();
    }
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("editButton")
  void onEdit(ClickEvent event) {
    if(isEditMode) {
      // Save
      vocabulary.setName(name.getText());

//      GWT.log(vocTitlesProvider.getList().get(0).toString() + "");
      for(int i = 0; i < vocTitlesProvider.getList().get(0).length(); i++) {
//        for(int j = 0; j < vocTitlesProvider.getList().get(i).length(); j++) {
        GWT.log("" + vocTitlesProvider.getList().get(0).get(i).getText());
        GWT.log("" + vocTitlesProvider.getList().get(0).get(i).getLocale());
      }

    }

    isEditMode = !isEditMode;
    draw();
  }

  @UiHandler("saveButton")
  void onSave(ClickEvent event) {
    isEditMode = !isEditMode;
    draw();
  }

  private void draw() {
    vocabularyName.setText(vocabulary.getName());
    vocabularyPanel.clear();
    termsPanel.clear();
    saveButton.setVisible(isEditMode);
    editButton.setVisible(!isEditMode);

    if(!isEditMode) {
      PropertiesTable vocabularyProperties = new PropertiesTable();
      vocabularyProperties.clearProperties();
      vocabularyProperties.addProperty("Name", vocabulary.getName());
      vocabularyProperties.addProperty(new Label("Title"), getLocalizedText(vocabulary.getTitlesArray()));
      vocabularyProperties.addProperty(new Label("Description"), getLocalizedText(vocabulary.getDescriptionsArray()));
      vocabularyProperties.addProperty("Taxonomy", taxonomy.getName());
      vocabularyProperties.addProperty("Repeatable", Boolean.toString(vocabulary.getRepeatable())); // Translations

      vocabularyPanel.add(vocabularyProperties);

    } else {
      vocTitlesProvider.getList().clear();
      vocDescriptionsProvider.getList().clear();
      vocTitlesProvider.getList().add(vocabulary.getTitlesArray());
      vocDescriptionsProvider.getList().add(vocabulary.getDescriptionsArray());

      PropertiesTable vocabularyProperties = new PropertiesTable();

      name = new TextBox();
      name.setText(vocabulary.getName());
      vocabularyProperties.addProperty(new Label("Name"), name);
      vocabularyProperties.addProperty(new Label("Titles"), tTitles);
      vocabularyProperties.addProperty(new Label("Descriptin"), tDescriptions);

      vocabularyPanel.add(vocabularyProperties);
    }

    termsPanel.add(getTermsList(JsArrays.toSafeArray(vocabulary.getTermsArray()), getUiHandlers()));
  }

  private UnorderedList getTermsList(JsArray<TermDto> terms, final VocabularyUiHandlers uiHandlers) {
    UnorderedList list = new UnorderedList();

    int nb = terms.length();
    if(nb > 0) {

      for(int i = 0; i < nb; i++) {
        final TermDto term = terms.get(i);
        NavLink link = new NavLink(term.getName());

        link.addStyleName("list-child");
        list.add(link);
        link.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent clickEvent) {
            uiHandlers.onTermSelection(taxonomy, vocabulary, term);
          }
        });

        if(term.getTermsCount() > 0) {
          list.add(getTermsList(JsArrays.toSafeArray(term.getTermsArray()), uiHandlers));
        }
      }
      return list;
    }

    return null;
  }

  @Override
  public void displayTerm(TermDto termDto) {
    termPanel.clear();

    Heading title = new Heading(5, termDto.getName());
    title.addStyleName("no-top-margin");
    termPanel.add(title);

    PropertiesTable termProps = new PropertiesTable();

    if(!isEditMode) {
      termProps.addProperty("Name", termDto.getName());
      termProps.addProperty(new Label("Title"), getLocalizedText(termDto.getTitlesArray()));
      termProps.addProperty(new Label("Description"), getLocalizedText(termDto.getDescriptionsArray()));
    } else {
      termTitlesProvider.getList().clear();
      termDescriptionsProvider.getList().clear();
      termTitlesProvider.getList().add(termDto.getTitlesArray());
      termDescriptionsProvider.getList().add(termDto.getDescriptionsArray());

      termName = new TextBox();
      termName.setText(termDto.getName());
      termProps.addProperty(new Label("Name"), name);
      termProps.addProperty(new Label("Titles"), termTitles);
      termProps.addProperty(new Label("Descriptin"), termDescriptions);
//      vocabularyPanel.add(vocabularyProperties);
    }

    termPanel.add(termProps);

  }

  private Widget getLocalizedText(JsArray<TextDto> texts) {
    UnorderedList textList = new UnorderedList();

    int nb = texts.length();
    if(nb > 0) {
      for(int i = 0; i < texts.length(); i++) {
        textList.add(getTextValue(texts.get(i)));
      }
    }

    return textList;
  }

  private InlineHTML getTextValue(TextDto textDto) {
    StringBuilder value = new StringBuilder();

    if(textDto.hasText()) {
      if(textDto.hasLocale()) {
        value.append("<div class=\"attribute-value\"><span class=\"label\">").append(textDto.getLocale())
            .append("</span> ");

      }
      String safeText = SafeHtmlUtils.fromString(textDto.getText()).asString().replaceAll("\\n", "<br />");
      value.append(safeText);
    }

    return new InlineHTML(SafeHtmlUtils.fromTrustedString(value.toString()));
  }

  public class TermDescriptionsColumn extends EditableTabableColumn<VocabularyDto> {

    private final String locale;

    public TermDescriptionsColumn(String locale) {
      this.locale = locale;
    }

    @Override
    public String getValue(VocabularyDto object) {
      for(int i = 0; i < object.getDescriptionsCount(); i++) {
        if(object.getDescriptionsArray().get(i).getLocale().equals(locale)) {
          return object.getDescriptionsArray().get(i).getText();
        }
      }

      return null;
    }
  }

  public class TextDtoColumn extends EditableTabableColumn<JsArray<TextDto>> {

    private final String locale;

    public TextDtoColumn(String locale) {
      this.locale = locale;
    }

    @Override
    public String getValue(JsArray<TextDto> object) {
      for(int i = 0; i < object.length(); i++) {
        if(object.get(i).getLocale().equals(locale)) {
          return object.get(i).getText();
        }
      }

      return null;
    }
  }
}
