/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

  void onAddToCart();
}
