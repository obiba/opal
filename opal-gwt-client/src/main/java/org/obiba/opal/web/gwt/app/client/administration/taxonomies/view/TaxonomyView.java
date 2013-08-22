package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomyUiHandlers;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.LocalizedLabel;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavPills;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class TaxonomyView extends ViewWithUiHandlers<TaxonomyUiHandlers> implements TaxonomyPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, TaxonomyView> {}

  @UiField
  FlowPanel panel;

  @UiField
  FlowPanel titlePanel;

  private TaxonomyDto taxonomy;

  @Inject
  public TaxonomyView(ViewUiBinder viewUiBinder) {
    initWidget(viewUiBinder.createAndBindUi(this));
  }

  @Override
  public void setTaxonomy(TaxonomyDto taxonomy) {
    this.taxonomy = taxonomy;
    if(taxonomy == null) {
      panel.clear();
    } else {
      redraw();
    }
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
    panel.clear();

    panel.setTitle(taxonomy.getName());

    LocalizedLabel label = new LocalizedLabel();
//    label.setWidgetTitle("Title");
    label.setLocale("en");
    label.setText("Enfin:)");

    titlePanel.add(label);
    panel.add(titlePanel);
//    for(TaxonomyDto.TextDto title : JsArrays.toIterable(taxonomy.getTitlesArray())) {
//      Label locale = new Label(title.getLocale());
//      InlineLabel text = new InlineLabel(title.getText());
//      panel.add(locale);
//      panel.add(text);
//    }

    FlowPanel vocabulariesPanel = new FlowPanel();
    vocabulariesPanel.addStyleName("item");

    JsArray<TaxonomyDto.VocabularyDto> vocabularies = JsArrays.toSafeArray(taxonomy.getVocabulariesArray());
    if(vocabularies.length() > 0) {
      NavPills pills = new NavPills();
      for(int i = 0; i < vocabularies.length(); i++)
        pills.add(newVocabularyLink(getUiHandlers(), vocabularies.get(i)));
      vocabulariesPanel.add(pills);
    }
    panel.add(vocabulariesPanel);
  }

  private Widget newVocabularyLink(TaxonomyUiHandlers uiHandlers, TaxonomyDto.VocabularyDto vocabulary) {
    NavLink link = new NavLink(vocabulary.getName());
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        getUiHandlers().showAddVocabulary(taxonomy);
//        uiHandlers.onVocabularySelection(taxonomy, vocabulary);//TODO need to create links to taxonomies UiHandlers
      }
    });
    return link;
  }
}
