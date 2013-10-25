package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.model.client.opal.TermDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface VocabularyUiHandlers extends UiHandlers {

//  void showEditVocabulary();

//  void showAddTerm(TaxonomyDto taxonomyDto, VocabularyDto vocabulary);

  void onTermSelection(TermDto termDto);

  void onEditVocabulary();

//  void onMoveUp(TaxonomyDto taxonomy, VocabularyDto vocabulary, TermDto term);
//
//  void onMoveDown(TaxonomyDto taxonomy, VocabularyDto vocabulary, TermDto term);

//  void onEditVocabulary(TaxonomyDto taxonomyDto, VocabularyDto vocabulary);
}
