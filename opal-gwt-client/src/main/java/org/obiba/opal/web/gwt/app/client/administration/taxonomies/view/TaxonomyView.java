/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ClickableColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.LocaleTextColumn;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class TaxonomyView extends ViewWithUiHandlers<TaxonomyUiHandlers> implements TaxonomyPresenter.Display {

  interface Binder extends UiBinder<Widget, TaxonomyView> {}

  private final TranslationMessages translationMessages;

  private final Translations translations;

  @UiField
  IconAnchor edit;

  @UiField
  Button remove;

  @UiField
  Button download;

  @UiField
  Panel detailsPanel;

  @UiField
  Heading taxonomyName;

  @UiField
  Panel taxonomyPanel;

  @UiField
  Label author;

  @UiField
  Label license;

  @UiField
  Panel titlePanel;

  @UiField
  Panel descriptionPanel;

  @UiField
  Table<VocabularyDto> table;

  @UiField
  OpalSimplePager pager;

  @UiField
  TextBoxClearable filter;

  private final ListDataProvider<VocabularyDto> dataProvider = new ListDataProvider<VocabularyDto>();

  @Inject
  public TaxonomyView(Binder uiBinder, Translations translations, TranslationMessages translationMessages) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translationMessages = translationMessages;
    this.translations = translations;
    initializeVocabulariesTable();
    initializeFilter();
  }

  private void initializeVocabulariesTable() {
    dataProvider.addDataDisplay(table);

    table.addColumn(new VocabularyNameColumn(), translations.nameLabel());
    table.addColumn(new LocaleTextColumn<VocabularyDto>() {
      @Override
      protected JsArray<LocaleTextDto> getLocaleText(VocabularyDto term) {
        return term.getTitleArray();
      }
    }, translations.titleLabel());
    table.addColumn(new LocaleTextColumn<VocabularyDto>() {
      @Override
      protected JsArray<LocaleTextDto> getLocaleText(VocabularyDto term) {
        return term.getDescriptionArray();
      }
    }, translations.descriptionLabel());
    table.addColumn(new TextColumn<VocabularyDto>() {
      @Override
      public String getValue(VocabularyDto object) {
        return object.getTermsCount() + "";
      }
    }, translations.termsLabel());

    //table.setSelectionModel(new SingleSelectionModel<VocabularyDto>());
    table.setPageSize(Table.DEFAULT_PAGESIZE);
    table.setEmptyTableWidget(new com.google.gwt.user.client.ui.InlineLabel(translationMessages.vocabularyCount(0)));
    pager.setDisplay(table);
  }

  private void initializeFilter() {
    filter.setText("");
    filter.getTextBox().setPlaceholder(translations.filterVocabularies());
    filter.getTextBox().addStyleName("input-xlarge");
    filter.getClear().setTitle(translations.clearFilter());
  }

  @Override
  public void setTaxonomy(@Nullable TaxonomyDto taxonomy) {
    if(taxonomy != null) {
      renderTaxonomy(taxonomy);
    }
    detailsPanel.setVisible(taxonomy != null);
  }

  @Override
  public void setVocabularies(JsArray<VocabularyDto> vocabularies) {
    dataProvider.setList(JsArrays.toList(vocabularies));
    dataProvider.refresh();
    pager.setPagerVisible(table.getRowCount() > Table.DEFAULT_PAGESIZE);
  }

  private void renderTaxonomy(TaxonomyDto taxonomy) {
    taxonomyName.setText(taxonomy.getName());
    author.setText(taxonomy.hasAuthor() ? taxonomy.getAuthor() : "");
    license.setText(taxonomy.hasLicense() ? taxonomy.getLicense() : "");
    renderText(titlePanel, taxonomy.getTitleArray());
    renderText(descriptionPanel, taxonomy.getDescriptionArray());
    setVocabularies(taxonomy.getVocabulariesArray());
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

  @UiHandler("edit")
  public void onEdit(ClickEvent event) {
    getUiHandlers().onEdit();
  }

  @UiHandler("remove")
  public void onDelete(ClickEvent event) {
    getUiHandlers().onDelete();
  }

  @UiHandler("download")
  public void onDownload(ClickEvent event) {
    getUiHandlers().onDownload();
  }

  @UiHandler("addVocabulary")
  public void onAddVocabulary(ClickEvent event) {
    getUiHandlers().onAddVocabulary();
  }

  @UiHandler("filter")
  void onFilterUpdate(KeyUpEvent event) {
    getUiHandlers().onFilterUpdate(filter.getText());
  }

  private class VocabularyNameColumn extends ClickableColumn<VocabularyDto> {

    private VocabularyNameColumn() {
      setFieldUpdater(new FieldUpdater<VocabularyDto, String>() {
        @Override
        public void update(int rowIndex, VocabularyDto dto, String value) {
          getUiHandlers().onVocabularySelection(dto.getName());
        }
      });
      setDefaultSortAscending(false);
      setSortable(true);
    }

    @Override
    public String getValue(VocabularyDto object) {
      return object.getName();
    }
  }

}
