package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomyUiHandlers;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedLabel;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.TextDto;
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
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class TaxonomyView extends ViewWithUiHandlers<TaxonomyUiHandlers> implements TaxonomyPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, TaxonomyView> {}

  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  Heading taxonomyName;

  @UiField
  FlowPanel titlePanel;

  @UiField
  FlowPanel descriptionPanel;

  @UiField
  FlowPanel vocabulariesPanel;

  private TaxonomyDto taxonomy;

  @Inject
  public TaxonomyView(ViewUiBinder viewUiBinder) {
    initWidget(viewUiBinder.createAndBindUi(this));
  }

  @Override
  public void setTaxonomy(TaxonomyDto taxonomy) {
    this.taxonomy = taxonomy;
    if(taxonomy == null) {
      titlePanel.clear();
      descriptionPanel.clear();
      vocabulariesPanel.clear();
    } else {
      redraw();
    }
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("edit")
  void editTaxonomy(ClickEvent event) {
    getUiHandlers().showEditTaxonomy();
  }

  @UiHandler("add")
  void onShowAddVocabulary(ClickEvent event) {
    getUiHandlers().showAddVocabulary(taxonomy);
  }

  private void redraw() {
    titlePanel.clear();
    descriptionPanel.clear();
    vocabulariesPanel.clear();

    taxonomyName.setText(taxonomy.getName());

    setTitleOrDescription(taxonomy.getTitlesArray(), titlePanel);
    setTitleOrDescription(taxonomy.getDescriptionsArray(), descriptionPanel);

    JsArray<TaxonomyDto.VocabularyDto> vocabularies = JsArrays.toSafeArray(taxonomy.getVocabulariesArray());
    if(vocabularies.length() > 0) {
      NavPills pills = new NavPills();
      for(int i = 0; i < vocabularies.length(); i++)
        pills.add(newVocabularyLink(vocabularies.get(i)));
      vocabulariesPanel.add(pills);
    }
  }

  private void setTitleOrDescription(JsArray<TextDto> array, FlowPanel panel) {
    for(TaxonomyDto.TextDto text : JsArrays.toIterable(array)) {
      LocalizedLabel label = new LocalizedLabel();
      label.setLocale(text.getLocale());
      label.setText(text.getText());
      panel.add(label);
    }
  }

  private Widget newVocabularyLink(final VocabularyDto vocabulary) {
    NavLink link = new NavLink(vocabulary.getName());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        getUiHandlers().onVocabularySelection(taxonomy, vocabulary);
      }
    });
    return link;
  }
}
