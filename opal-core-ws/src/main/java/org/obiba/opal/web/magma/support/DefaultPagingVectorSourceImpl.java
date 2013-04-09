/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;

public class DefaultPagingVectorSourceImpl implements PagingVectorSource {

  private final ValueTable vt;

  private final VariableValueSource vvs;

  public DefaultPagingVectorSourceImpl(ValueTable vt, VariableValueSource vvs) {
    this.vt = vt;
    this.vvs = vvs;
  }

  @Override
  public Iterable<Value> getValues(int offset, int limit) {
    VectorSource vectorSource = vvs.asVectorSource();
    if(vectorSource == null) {
      return Collections.emptyList();
    }

    // TODO: Refactor this code. We are creating a TreeSet (to sort the entities), then converting to a List
    // (to extract the desired sublist), then converting it back to a TreeSet (because VectorSource.getValues
    // expects a SortedSet of entities).
    TreeSet<VariableEntity> sortedEntities = new TreeSet<VariableEntity>(vt.getVariableEntities());
    int end = Math.min(offset + limit, sortedEntities.size());
    List<VariableEntity> entitySubList = new ArrayList<VariableEntity>(sortedEntities).subList(offset, end);

    return vectorSource.getValues(new TreeSet<VariableEntity>(entitySubList));
  }
}