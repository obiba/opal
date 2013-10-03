package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface AddVocabularyModalUiHandlers extends UiHandlers, ModalUiHandlers {
  boolean addTaxonomyNewVocabulary(TaxonomyDto taxonomyDto);
}
