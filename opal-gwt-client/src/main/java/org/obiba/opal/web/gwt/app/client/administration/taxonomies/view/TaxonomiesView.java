package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomiesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomiesUiHandlers;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class TaxonomiesView extends ViewWithUiHandlers<TaxonomiesUiHandlers> implements TaxonomiesPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, TaxonomiesView> {}

  @UiField
  FlowPanel panel;

  @UiField
  Breadcrumbs breadcrumbs;

  private JsArray<TaxonomyDto> taxonomies;

  @Inject
  public TaxonomiesView(ViewUiBinder viewUiBinder) {
    initWidget(viewUiBinder.createAndBindUi(this));
  }

  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public void setTaxonomies(JsArray<TaxonomyDto> taxonomies) {
    this.taxonomies = taxonomies;
    if(taxonomies.length() == 0) {
      panel.clear();
    } else {
      redraw();
    }

  }

  @UiHandler("add")
  void onShowAddTaxonomy(ClickEvent event) {
    getUiHandlers().showAddTaxonomy();
  }

  private void redraw() {
    panel.clear();
    for(TaxonomyDto taxonomy : JsArrays.toIterable(taxonomies)) {
      FlowPanel panelTaxonomy = new FlowPanel();
      panelTaxonomy.addStyleName("item");

      Widget taxonomyLink = newTaxonomyLink(getUiHandlers(), taxonomy);
      panelTaxonomy.add(taxonomyLink);

      for(int i = 0; i < taxonomy.getDescriptionsCount(); i++) {
        panelTaxonomy.add(new Label(taxonomy.getDescriptions(i).getText()));
      }

      JsArrayString vocabularies = JsArrays.toSafeArray(taxonomy.getVocabulariesArray());
      if(vocabularies.length() > 0) {
        FlowPanel vocabulariesPanel = new FlowPanel();
        for(int i = 0; i < vocabularies.length(); i++) {
          vocabulariesPanel.add(newVocabularyLink(getUiHandlers(), taxonomy, vocabularies.get(i)));
        }
        panelTaxonomy.add(vocabulariesPanel);
      }

      panel.add(panelTaxonomy);
    }
  }

  protected Widget newTaxonomyLink(final TaxonomiesUiHandlers handlers, final TaxonomyDto taxonomy) {
    FlowPanel titlePanel = new FlowPanel();
    Label taxonomyTitle = new Label(taxonomy.getName());
    taxonomyTitle.addStyleName("inline-block");
    taxonomyTitle.setTitle(taxonomy.getName());
    taxonomyTitle.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        handlers.onTaxonomySelection(taxonomy);
      }
    });

    titlePanel.add(taxonomyTitle);
    IconAnchor edit = new IconAnchor();
    edit.setIcon(IconType.EDIT);
    edit.addStyleName("inline-block");
    titlePanel.add(edit);

    Heading head = new Heading(4);
    head.addStyleName("inline-block small-right-indent");
    head.add(titlePanel);
    return head;
  }

  private Widget newVocabularyLink(final TaxonomiesUiHandlers uiHandlers, final TaxonomyDto taxonomy,
      final String vocabulary) {
    NavLink link = new NavLink(vocabulary);
//    link.setIcon(IconType.TAG);
    link.addStyleName("small-dual-indent");
    link.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        uiHandlers.onVocabularySelection(taxonomy.getName(), vocabulary);
      }
    });
    return link;
  }

}
