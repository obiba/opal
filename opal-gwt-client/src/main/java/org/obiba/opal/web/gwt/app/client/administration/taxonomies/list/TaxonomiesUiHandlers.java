package org.obiba.opal.web.gwt.app.client.administration.taxonomies.list;

import org.obiba.opal.web.model.client.opal.TaxonomiesDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TaxonomiesUiHandlers extends UiHandlers {

  void onTaxonomySelection(TaxonomiesDto.TaxonomySummaryDto taxonomyDto);

  void onAddTaxonomy();

  void onImportDefaultTaxonomies();

}
