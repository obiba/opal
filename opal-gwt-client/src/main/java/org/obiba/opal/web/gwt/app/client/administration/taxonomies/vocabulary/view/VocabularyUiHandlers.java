package org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view;

import org.obiba.opal.web.model.client.opal.TermDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface VocabularyUiHandlers extends UiHandlers {

  void onDelete();

  void onEdit();

  void onTaxonomySelected();

  void onEditTerm(TermDto termDto);

  void onDeleteTerm(TermDto termDto);

  void onFilterUpdate(String filter);

}
