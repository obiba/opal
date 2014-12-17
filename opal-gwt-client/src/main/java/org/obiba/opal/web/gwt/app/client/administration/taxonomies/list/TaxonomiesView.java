package org.obiba.opal.web.gwt.app.client.administration.taxonomies.list;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.LocaleTextDto;
import org.obiba.opal.web.model.client.opal.TaxonomiesDto;

import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.NavList;
import com.github.gwtbootstrap.client.ui.NavWidget;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class TaxonomiesView extends ViewWithUiHandlers<TaxonomiesUiHandlers> implements TaxonomiesPresenter.Display {

  private final Translations translations;

  interface ViewUiBinder extends UiBinder<Widget, TaxonomiesView> {}

  @UiField
  DropdownButton addBtn;

  @UiField
  ScrollPanel taxonomyDetailsPanel;

  @UiField
  NavList taxonomyList;

  @Inject
  public TaxonomiesView(ViewUiBinder viewUiBinder, Translations translations) {
    this.translations = translations;
    initWidget(viewUiBinder.createAndBindUi(this));
    addBtn.setText(translations.addTaxonomy());
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    taxonomyDetailsPanel.clear();
    taxonomyDetailsPanel.add(content);
  }

  @Override
  public void setTaxonomies(JsArray<TaxonomiesDto.TaxonomySummaryDto> taxonomies, String selection) {
    taxonomyList.clear();
    TaxonomiesDto.TaxonomySummaryDto first = null;
    TaxonomiesDto.TaxonomySummaryDto toSelect = null;
    NavLink activeLink = null;
    for(TaxonomiesDto.TaxonomySummaryDto taxonomy : JsArrays.toIterable(taxonomies)) {

      NavLink link = new NavLink(taxonomy.getName());
      link.setTitle(asText(taxonomy.getTitleArray()));
      link.addClickHandler(new TaxonomyClickHandler(taxonomy, link));
      taxonomyList.add(link);
      if(first == null) {
        first = taxonomy;
        activeLink = link;
      }
      if(taxonomy.getName().equals(selection)) {
        toSelect = taxonomy;
        activeLink = link;
      }
    }

    if(toSelect != null) getUiHandlers().onTaxonomySelection(toSelect);
    else getUiHandlers().onTaxonomySelection(first);
    if(activeLink != null) activeLink.setActive(true);
  }

  private String asText(JsArray<LocaleTextDto> texts) {
    StringBuilder builder = new StringBuilder();
    for(LocaleTextDto text : JsArrays.toIterable(texts)) {
      builder.append("[").append(text.getLocale()).append("] ").append(text.getText()).append("\n");
    }
    return builder.toString();
  }

  @UiHandler("addTaxonomy")
  void onShowAddTaxonomy(ClickEvent event) {
    getUiHandlers().onAddTaxonomy();
  }

  @UiHandler("importDefault")
  void onImportDefaultTaxonomies(ClickEvent event) {
    getUiHandlers().onImportDefaultTaxonomies();
  }

  private class TaxonomyClickHandler implements ClickHandler {

    private final TaxonomiesDto.TaxonomySummaryDto taxonomy;

    private final NavLink link;

    TaxonomyClickHandler(TaxonomiesDto.TaxonomySummaryDto taxonomy, NavLink link) {
      this.taxonomy = taxonomy;
      this.link = link;
    }

    @Override
    public void onClick(ClickEvent event) {
      unActivateLinks();
      link.setActive(true);
      getUiHandlers().onTaxonomySelection(taxonomy);
    }

    private void unActivateLinks() {
      int widgetCount = taxonomyList.getWidgetCount();
      for(int i = 0; i < widgetCount; i++) {
        if(taxonomyList.getWidget(i) instanceof NavLink) {
          ((NavWidget) taxonomyList.getWidget(i)).setActive(false);
        }
      }
    }
  }
}
