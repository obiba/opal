/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.event;

public class ValueTableAddedEvent extends ValueTableEvent {

  private String datasourceName;

  private String tableName;

  public ValueTableAddedEvent(String datasourceName, String tableName) {
    super(null);
    this.datasourceName = datasourceName;
    this.tableName = tableName;
  }

  public String getDatasourceName() {
    if (hasValueTable()) return getValueTable().getDatasource().getName();
    return datasourceName;
  }

  public String getTableName() {
    if (hasValueTable()) return getValueTable().getName();
    return tableName;
  }
}