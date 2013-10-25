package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TaxonomyUiHandlers extends UiHandlers {

  void showAddVocabulary(TaxonomyDto taxonomyDto);

  void showEditTaxonomy();

  void onVocabularySelection(TaxonomyDto taxonomyDto, VocabularyDto vocabulary);
}
