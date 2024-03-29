/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.table;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.magma.TableDto;

public interface ViewModalUiHandlers extends ModalUiHandlers {

  void onSave(String name, List<TableDto> referencedTables, List<String> innerFrom);

  void onSave(String name, String from, String idColumn, String entityType, String profile, boolean allColumns);
}
