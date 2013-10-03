package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TaxonomiesUiHandlers extends UiHandlers {

  void onTaxonomySelection(TaxonomyDto taxonomyDto);

  void onVocabularySelection(TaxonomyDto taxonomyDto, TaxonomyDto.VocabularyDto vocabularyDto);

  void showAddTaxonomy();
}
