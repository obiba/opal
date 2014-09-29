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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.github.gwtbootstrap.client.ui.Badge;
import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.github.gwtbootstrap.client.ui.constants.BadgeType;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
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
  Panel titlePanel;

  @UiField
  Panel descriptionPanel;

  @UiField
  Panel vocabulariesPanel;

  private TaxonomyDto taxonomy;

  @Inject
  public TaxonomyView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public void setTaxonomy(@Nullable TaxonomyDto taxonomy) {
    if(taxonomy != null) {
      renderTaxonomy(taxonomy);
    }
    detailsPanel.setVisible(taxonomy != null);
  }

  private void renderTaxonomy(TaxonomyDto taxonomy) {
    this.taxonomy = taxonomy;
    taxonomyName.setText(taxonomy.getName());
    renderText(titlePanel, taxonomy.getTitleArray());
    renderText(descriptionPanel, taxonomy.getDescriptionArray());
    vocabulariesPanel.clear();
    for(VocabularyDto vocabulary : JsArrays.toIterable(taxonomy.getVocabulariesArray())) {
      renderVocabulary(vocabulary);
    }
  }

  private void renderVocabulary(VocabularyDto vocabulary) {
    Badge name = new Badge(vocabulary.getName());
    name.setType(BadgeType.SUCCESS);
    name.addStyleName("top-margin bottom-margin");
    vocabulariesPanel.add(name);
    PropertiesTable properties = new PropertiesTable();
    properties.setKeyStyleNames("span3");
    renderPropertyText(properties, "Title", vocabulary.getTitleArray());
    renderPropertyText(properties, "Description", vocabulary.getDescriptionArray());
    vocabulariesPanel.add(properties);

    Table<TermDto> termTable = new Table<TermDto>();
    termTable.addColumn(new TextColumn<TermDto>() {
      @Override
      public String getValue(TermDto object) {
        return object.getName();
      }
    }, translations.nameLabel());
    termTable.addColumn(new LocaleTextColumn() {
      @Override
      protected JsArray<LocaleTextDto> getLocaleText(TermDto term) {
        return term.getTitleArray();
      }
    }, translations.titleLabel());
    termTable.addColumn(new LocaleTextColumn() {
      @Override
      protected JsArray<LocaleTextDto> getLocaleText(TermDto term) {
        return term.getDescriptionArray();
      }
    }, translations.descriptionLabel());
    ListDataProvider<TermDto> provider = new ListDataProvider<TermDto>();
    provider.setList(JsArrays.toList(vocabulary.getTermsArray()));
    provider.addDataDisplay(termTable);
    vocabulariesPanel.add(termTable);
  }

  private void renderPropertyText(PropertiesTable properties, String label, JsArray<LocaleTextDto> texts) {
    FlowPanel textPanel = new FlowPanel();
    renderText(textPanel, texts);
    properties.addProperty(new Label(label), textPanel);
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

  private static abstract class LocaleTextColumn extends Column<TermDto, String> {

    private LocaleTextColumn() {
      super(new TextCell(new SafeHtmlRenderer<String>() {

        @Override
        public SafeHtml render(String object) {
          return object == null ? SafeHtmlUtils.EMPTY_SAFE_HTML : SafeHtmlUtils.fromTrustedString(object);
        }

        @Override
        public void render(String object, SafeHtmlBuilder appendable) {
          appendable.append(SafeHtmlUtils.fromTrustedString(object));
        }
      }));
    }

    @Override
    public String getValue(TermDto object) {
      return getLabels(object);
    }

    protected abstract JsArray<LocaleTextDto> getLocaleText(TermDto term);

    private String getLabels(TermDto object) {
      JsArray<LocaleTextDto> texts = JsArrays.toSafeArray(getLocaleText(object));
      LocaleTextDto text;
      StringBuilder labels = new StringBuilder();

      for(int i = 0; i < texts.length(); i++) {
        text = texts.get(i);
        appendLabel(text, labels);
      }

      return labels.toString();
    }

    private void appendLabel(LocaleTextDto attr, StringBuilder labels) {
      labels.append("<div class=\"attribute-value\">");
      if(attr.hasLocale() && attr.getLocale().trim().length() > 0) {
        labels.append("<span class=\"label\">").append(attr.getLocale()).append("</span> ");
      }
      String value = attr.getText();
      String safeValue = SafeHtmlUtils.fromString(value).asString().replaceAll("\\n", "<br />");
      try {
        if(UriUtils.extractScheme(value) != null && UriUtils.isSafeUri(value)) {
          labels.append("<a href=").append(value).append(" target=\"_blank\">").append(safeValue).append("</a>");
        } else {
          labels.append(safeValue);
        }
      } catch(Exception e) {
        labels.append(safeValue);
      }
      labels.append("</div>");
    }
  }
}
