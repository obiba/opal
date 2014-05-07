/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter;

import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface IdentifiersTableUiHandlers extends UiHandlers {

  void onIdentifiersRequest(TableDto identifiersTable, String select, int offset, int limit);

  void onDeleteIdentifiersTable();

  void onImportSystemIdentifiers();

  void onCopySystemIdentifiers();

  void onImportIdentifiersMapping();

  void onAddIdentifiersMapping();

  void onEditIdentifiersMapping(VariableDto variable);

  void onDeleteIdentifiersMapping(VariableDto variable);

  void onGenerateIdentifiersMapping(VariableDto variable);

  void onDownloadIdentifiers(VariableDto variable);

  void onDownloadIdentifiers();
}
