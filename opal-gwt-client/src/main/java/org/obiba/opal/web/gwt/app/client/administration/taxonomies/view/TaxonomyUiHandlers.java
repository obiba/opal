/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import org.obiba.opal.web.model.client.opal.VocabularyDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TaxonomyUiHandlers extends UiHandlers {

  void onDownload();

  void onEdit();

  void onDelete();

  void onVocabularySelection(String vocabularyName);

  void onAddVocabulary();

  void onSaveChanges();

  void onResetChanges();

  void onFilterUpdate(String filter);

  void onMoveUpVocabulary(VocabularyDto vocabularyDto);

  void onMoveDownVocabulary(VocabularyDto vocabularyDto);

  void onSortVocabularies(boolean isAscending);
}
