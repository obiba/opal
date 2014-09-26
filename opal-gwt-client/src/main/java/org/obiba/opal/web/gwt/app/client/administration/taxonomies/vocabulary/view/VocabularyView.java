package org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.TermArrayUtils;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedLabel;
import org.obiba.opal.web.gwt.app.client.ui.PropertiesTable;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TermDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavList;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class VocabularyView extends ViewWithUiHandlers<VocabularyUiHandlers> implements VocabularyPresenter.Display {

  private final Translations translations;

  interface ViewUiBinder extends UiBinder<Widget, VocabularyView> {}

  JsArrayString locales;

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  IconAnchor editVocabulary;

  @UiField
  Heading vocabularyName;

  @UiField
  PropertiesTable vocabularyProperties;

  @UiField
  PropertiesTable termProperties;

  @UiField
  Heading termTitle;

  @UiField
  FlowPanel termsLinks;

  @Inject
  public VocabularyView(ViewUiBinder viewUiBinder, Translations translations) {
    this.translations = translations;
    initWidget(viewUiBinder.createAndBindUi(this));
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("editVocabulary")
  void onEdit(ClickEvent event) {
    getUiHandlers().onEditVocabulary();
  }

  @Override
  public void displayVocabulary(VocabularyDto vocabulary, String taxonomy, String term) {

    vocabularyName.setText(vocabulary.getName());
    vocabularyProperties.clearProperties();
    vocabularyProperties.addProperty(translations.nameLabel(), vocabulary.getName());
    vocabularyProperties.addProperty(translations.taxonomyLabel(), taxonomy);

    vocabularyProperties.addProperty(new Label(translations.titleLabel()),
        getLocalizedText(JsArrays.toSafeArray(vocabulary.getTitleArray())));
    vocabularyProperties.addProperty(new Label(translations.descriptionLabel()),
        getLocalizedText(JsArrays.toSafeArray(vocabulary.getDescriptionArray())));

    vocabularyProperties.addProperty(translations.repeatableLabel(), Boolean.toString(vocabulary.getRepeatable()));

    termsLinks.clear();
    NavList navList = new NavList();
    getTermsLinks(navList, JsArrays.toSafeArray(vocabulary.getTermsArray()), 0);
    termsLinks.add(navList);

    displayTerm(TermArrayUtils.findTerm(vocabulary.getTermsArray(), term));
  }

  private void getTermsLinks(NavList navList, final JsArray<TermDto> terms, int level) {

    for(int i = 0; i < terms.length(); i++) {
      final int finalI = i;
      NavLink link = new NavLink();

      NavLink linkTitle = new NavLink(terms.get(i).getName());
      linkTitle.addStyleName("inline-block");

      link.add(getInlineHTMLSpacer(level));
      link.add(linkTitle);
      link.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          getUiHandlers().onTermSelection(terms.get(finalI));
        }
      });
      link.addStyleName("inline");

      navList.add(link);
    }
  }

  @Override
  public void displayTerm(TermDto termDto) {
    termProperties.clearProperties();
    if(termDto != null) {
      termTitle.setVisible(true);
      termTitle.setText(termDto.getName());
      termProperties.addProperty(translations.nameLabel(), termDto.getName());
      termProperties.addProperty(new Label(translations.titleLabel()),
          getLocalizedText(JsArrays.toSafeArray(termDto.getTitleArray())));
      termProperties.addProperty(new Label(translations.descriptionLabel()),
          getLocalizedText(JsArrays.toSafeArray(termDto.getDescriptionArray())));
      termProperties.setVisible(true);
    } else {
      termTitle.setVisible(false);
      termProperties.setVisible(false);
    }
  }

  @Override
  public void setAvailableLocales(JsArrayString locales) {
    this.locales = locales;
  }

  private InlineHTML getInlineHTMLSpacer(int level) {
    SafeHtml indent = SafeHtmlUtils.fromString("");
    for(int i = 0; i < level; i++) {
      indent = SafeHtmlUtils.fromTrustedString(indent.asString() + "&nbsp;&nbsp;&nbsp;&nbsp;");
    }
    InlineHTML spacer = new InlineHTML(indent);
    spacer.addStyleName("inline-block");
    return spacer;
  }

  private Widget getLocalizedText(JsArray<LocaleTextDto> texts) {
    FlowPanel textList = new FlowPanel();

    for(int i = 0; i < locales.length(); i++) {
      String textValue = "";
      for(int j = 0; j < texts.length(); j++) {
        if(texts.get(j).getLocale().equals(locales.get(i))) {
          textValue = texts.get(j).getText();
          break;
        }
      }
      textList.add(new LocalizedLabel(locales.get(i), textValue));
    }
    return textList;
  }

}