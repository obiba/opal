package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.NamespacedAttributesTableUiHandlers;

public interface VariableUiHandlers extends NamespacedAttributesTableUiHandlers {

  void onNextVariable();

  void onPreviousVariable();

  void onHistory();

  void onEditScript();

  void onSaveScript();

  void onRemove();

  void onAddToView();

  void onCategorizeToAnother();

  void onCategorizeToThis();

  void onDeriveCustom();

  void onShowSummary();

  void onShowValues();

  void onEditCategories();

  void onEditProperties();

  void onAddAttribute();

  void onAddTaxonomy();
}
