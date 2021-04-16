/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.search.entities;

import com.gwtplatform.mvp.client.UiHandlers;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.List;

public interface SearchEntitiesUiHandlers extends UiHandlers {

  void onSearch(String entityType, String idQuery, List<String> query, int offset, int limit);

  void onVariableCriterion(String datasource, String table, String variable);

  void onClear();

  void onEntityType(String selection);

  void onAddToView(List<String> variableFullNames, List<String> magmaJsStatements);
}
