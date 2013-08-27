package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.model.client.opal.TaxonomyDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.TermDto;
import org.obiba.opal.web.model.client.opal.TaxonomyDto.VocabularyDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface VocabularyUiHandlers extends UiHandlers {

  void showEditVocabulary();

  void showAddTerm(TaxonomyDto taxonomyDto, VocabularyDto vocabulary);

  void onTermSelection(TaxonomyDto taxonomyDto, VocabularyDto vocabulary, TermDto termDto);
}
