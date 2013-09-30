package org.obiba.opal.web.gwt.app.client.magma.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface VariableUiHandlers extends UiHandlers {

  void onNextVariable();

  void onPreviousVariable();

  void onEdit();

  void onHistory();

  void onEditScript();

  void onRemove();

  void onAddToView();

  void onCategorizeToAnother();

  void onCategorizeToThis();

  void onDeriveCustom();

  void onShowSummary();

  void onShowValues();

  void onEditCategories();
}
