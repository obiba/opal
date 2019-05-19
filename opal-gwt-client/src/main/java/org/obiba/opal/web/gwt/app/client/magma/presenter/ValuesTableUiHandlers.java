/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.presenter;

import com.gwtplatform.mvp.client.UiHandlers;
import org.obiba.opal.web.model.client.magma.ValueSetsDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.List;

public interface ValuesTableUiHandlers extends UiHandlers {

  void onVariableFilter(String variableName);

  void onSearchValueSets(List<VariableDto> variables, List<String> query, int offset, int limit);

  void onSearchEntities(String idQuery, List<String> queries);

  void onRequestValueSets(List<VariableDto> variables, int offset, int limit);

  void onRequestValueSets(String filter, int offset, int limit, boolean exactMatch);

  void requestBinaryValue(VariableDto variable, String entityIdentifier);

  void requestGeoValue(VariableDto variable, String entityIdentifier, ValueSetsDto.ValueDto value);

  void requestValueSequence(VariableDto variable, String entityIdentifier);

  void requestEntitySearch(String entityType, String entityId);

  void updateVariables(String select);
}
