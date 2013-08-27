package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyUiHandlers;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedLabel;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.TermDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.VocabularyDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavPills;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class VocabularyView extends ViewWithUiHandlers<VocabularyUiHandlers> implements VocabularyPresenter.Display {
  interface ViewUiBinder extends UiBinder<Widget, VocabularyView> {}

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  Heading vocabularyName;

  @UiField
  FlowPanel titlePanel;

  @UiField
  FlowPanel descriptionPanel;

  @UiField
  InlineLabel taxonomyName;

  @UiField
  FlowPanel termsPanel;

  private TaxonomyDto taxonomy;

  private VocabularyDto vocabulary;

  @Inject
  public VocabularyView(ViewUiBinder viewUiBinder) {
    initWidget(viewUiBinder.createAndBindUi(this));
  }

  @Override
  public void setTaxonomyAndVocabulary(TaxonomyDto taxonomy, VocabularyDto vocabulary) {
    this.taxonomy = taxonomy;
    this.vocabulary = vocabulary;
    if(taxonomy == null) {
      titlePanel.clear();
      descriptionPanel.clear();
      termsPanel.clear();
    } else {
      redraw();
    }
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("edit")
  void editVocabulary(ClickEvent event) {
    getUiHandlers().showEditVocabulary();
  }

  @UiHandler("add")
  void onShowAddVocabulary(ClickEvent event) {
    getUiHandlers().showAddTerm(taxonomy, vocabulary);
  }

  private void redraw() {
    titlePanel.clear();
    descriptionPanel.clear();
    termsPanel.clear();

    vocabularyName.setText(vocabulary.getName());
    taxonomyName.setText(taxonomy.getName());

    setTitleOrDescription(vocabulary.getTitlesArray(), titlePanel);
    setTitleOrDescription(vocabulary.getDescriptionsArray(), descriptionPanel);

    JsArray<TermDto> terms = JsArrays.toSafeArray(vocabulary.getTermsArray());
    if(terms.length() > 0) {
      NavPills pills = new NavPills();
      for(int i = 0; i < terms.length(); i++)
        pills.add(newTermLink(terms.get(i)));
      termsPanel.add(pills);
    }
  }

  private void setTitleOrDescription(JsArray<TaxonomyDto.TextDto> array, FlowPanel panel) {
    for(TaxonomyDto.TextDto text : JsArrays.toIterable(array)) {
      LocalizedLabel label = new LocalizedLabel();
      label.setLocale(text.getLocale());
      label.setText(text.getText());
      panel.add(label);
    }
  }

  private Widget newTermLink(final TermDto termDto) {
    NavLink link = new NavLink(termDto.getName());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        getUiHandlers().onTermSelection(taxonomy, vocabulary, termDto);
      }
    });
    return link;
  }

}
