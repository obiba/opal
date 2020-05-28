/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.database.list.data;

import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.gwtplatform.mvp.client.UiHandlers;

public interface DataDatabasesUiHandlers extends UiHandlers {

  void createSql(boolean storageOnly);

  void createMongo(boolean storageOnly);

  void edit(DatabaseDto dto);

  void testConnection(DatabaseDto dto);

  void deleteDatabase(DatabaseDto dto);
}
