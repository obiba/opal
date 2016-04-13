package org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view;

import org.obiba.opal.web.model.client.opal.TermDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface VocabularyUiHandlers extends UiHandlers {

  void onDelete();

  void onPrevious();

  void onNext();

  void onEdit();

  void onTaxonomySelected();

  void onAddTerm();

  void onEditTerm(TermDto termDto);

  void onDeleteTerm(TermDto termDto);

  void onFilterUpdate(String filter);

  void onMoveUpTerm(TermDto termDto);

  void onMoveDownTerm(TermDto termDto);

  void onSortTerms(boolean isAscending);

  void onSaveChanges();

  void onResetChanges();
}
