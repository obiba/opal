/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service.impl;

import org.obiba.magma.Timestamps;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.support.Timestampeds;
import org.obiba.opal.search.service.ValueTableIndex;

import java.util.Calendar;
import java.util.Date;

public abstract class AbstractValueTableIndex implements ValueTableIndex {

  protected final String name;

  protected final ValueTable table;

  protected AbstractValueTableIndex(String name, ValueTable table) {
    this.name = name;
    this.table = table;
  }

  @Override
  public String getIndexName() {
    return name;
  }

  @Override
  public String getValueTableReference() {
    return table.getTableReference();
  }

  @Override
  public boolean isUpToDate() {
    return Timestampeds.lastUpdateComparator.compare(this, table) >= 0;
  }

  @Override
  public Calendar now() {
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    return c;
  }

  @Override
  public int hashCode() {
    return getIndexType().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && (obj == this ||
        obj instanceof AbstractValueTableIndex && ((ValueTableIndex) obj).getIndexType().equals(getIndexType()));
  }

  @Override
  public Timestamps getTimestamps() {
    return null;
  }

  public Iterable<Variable> getVariables() {
    return table.getVariables();
  }
}
