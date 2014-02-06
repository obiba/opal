package org.obiba.opal.web.gwt.app.client.administration.taxonomies.list;

import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TaxonomiesUiHandlers extends UiHandlers {

  void onTaxonomySelection(TaxonomyDto taxonomyDto);

  void onAddTaxonomy();

  void onTaxonomyEdit(TaxonomyDto taxonomyDto);

  void onVocabularySelection(String name, String vocabulary);
}
