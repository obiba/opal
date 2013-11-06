package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TermArrayUtils;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
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

  @UiField
  FlowPanel termPropertiesPanel;

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
  public void displayVocabulary(VocabularyDto vocabulary, String taxonomyName, String termName) {

    vocabularyName.setText(vocabulary.getName());
    vocabularyProperties.clearProperties();
    vocabularyProperties.addProperty(translations.nameLabel(), vocabulary.getName());
    vocabularyProperties
        .addProperty(new Label(translations.titleLabel()), getLocalizedText(vocabulary.getTitlesArray()));
    vocabularyProperties
        .addProperty(new Label(translations.descriptionLabel()), getLocalizedText(vocabulary.getDescriptionsArray()));
    vocabularyProperties.addProperty(translations.taxonomyLabel(), taxonomyName);
    vocabularyProperties
        .addProperty(translations.repeatableLabel(), Boolean.toString(vocabulary.getRepeatable())); // Translations

    termsLinks.clear();
    NavList navList = new NavList();
    getTermsLinks(navList, vocabulary.getTermsArray(), 0);
    termsLinks.add(navList);

    displayTerm(TermArrayUtils.findTerm(vocabulary.getTermsArray(), termName));
  }

  private void getTermsLinks(NavList navList, final JsArray<TermDto> terms, int level) {

    for(int i = 0; i < terms.length(); i++) {
      SafeHtml indent = SafeHtmlUtils.fromString("");
      for(int j = 0; j < level; j++) {
        indent = SafeHtmlUtils.fromTrustedString(indent.asString() + "&nbsp;&nbsp;&nbsp;&nbsp;");
      }
      InlineHTML spacer = new InlineHTML(indent);
      spacer.addStyleName("inline-block");

      NavLink link = new NavLink();

      link.add(spacer);
      NavLink linkTitle = new NavLink(terms.get(i).getName());
      linkTitle.addStyleName("inline-block");
      link.add(linkTitle);
      final int finalI = i;
      link.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent clickEvent) {
          getUiHandlers().onTermSelection(terms.get(finalI));
        }
      });
      link.addStyleName("inline");

      navList.add(link);

      if(terms.get(i).getTermsCount() > 0) {
        getTermsLinks(navList, terms.get(i).getTermsArray(), level + 1);
      }
    }
  }

  @Override
  public void displayTerm(TermDto termDto) {
    termProperties.clearProperties();
    if(termDto != null) {
      termPropertiesPanel.setVisible(true);
      termTitle.setText(termDto.getName());
      termProperties.addProperty(translations.nameLabel(), termDto.getName());
      termProperties.addProperty(new Label(translations.titleLabel()), getLocalizedText(termDto.getTitlesArray()));
      termProperties
          .addProperty(new Label(translations.descriptionLabel()), getLocalizedText(termDto.getDescriptionsArray()));
    } else {
      termPropertiesPanel.setVisible(false);
    }
  }

  private Widget getLocalizedText(JsArray<LocaleTextDto> texts) {
    FlowPanel textList = new FlowPanel();

    int nb = texts.length();
    if(nb > 0) {
      for(int i = 0; i < texts.length(); i++) {
        textList.add(getTextValue(texts.get(i)));
      }
    }

    return textList;
  }

  private Widget getTextValue(LocaleTextDto textDto) {
    LocalizedLabel l = new LocalizedLabel();

    l.setText(textDto.getText());
    l.setLocale(textDto.getLocale());

    return l;
  }
}
