package org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter;

import org.obiba.opal.web.model.client.opal.TermDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface VocabularyEditUiHandlers extends UiHandlers {

  void onTermSelection(TermDto termDto);

  void onAddChild(String text);

  void onAddSibling(String text);

  void onCancel();

  void onSave();

  void onReorderTerms(String termName, int pos, boolean insertAfter);
}
