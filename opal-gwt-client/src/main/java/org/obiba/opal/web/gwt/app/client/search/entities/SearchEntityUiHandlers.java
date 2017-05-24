/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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

public interface SearchEntityUiHandlers extends UiHandlers {

  void onSearch(String entityType, String entityId);

  void onTableChange(String tableReference);

  void requestValueSequenceView(VariableDto variableDto);

  void requestBinaryValueView(VariableDto variable);

  void requestGeoValueView(VariableDto variable, ValueSetsDto.ValueDto value);

  void requestEntityView(VariableDto variable, ValueSetsDto.ValueDto value);

}
