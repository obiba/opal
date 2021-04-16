/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.support;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

import java.util.Collections;
import java.util.List;

public class DefaultPagingVectorSourceImpl implements PagingVectorSource {

  private final ValueTable vt;

  private final VariableValueSource vvs;

  public DefaultPagingVectorSourceImpl(ValueTable vt, VariableValueSource vvs) {
    this.vt = vt;
    this.vvs = vvs;
  }

  @Override
  public Iterable<Value> getValues(int offset, int limit) {
    if (!vvs.supportVectorSource()) {
      return Collections.emptyList();
    }
    List<VariableEntity> entities = vt.getVariableEntities();
    int end = Math.min(offset + limit, entities.size());
    List<VariableEntity> entitySubList = entities.subList(offset, end);
    return vvs.asVectorSource().getValues(entitySubList);
  }
}