/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view;

import org.obiba.opal.web.model.client.opal.TermDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface VocabularyUiHandlers extends UiHandlers {

  void onSearchVariables(String term);

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
