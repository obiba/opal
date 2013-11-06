package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TaxonomyEditModalUiHandlers extends UiHandlers, ModalUiHandlers {
  void onSaveTaxonomy();

  void onDeleteTaxonomy();

  void onAddVocabulary();

  void onDeleteVocabulary(String vocabularyName);
}
