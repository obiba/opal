/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSetBatch;
import org.obiba.magma.VariableEntity;
import org.obiba.opal.spi.r.RServerResult;

import java.util.List;
import java.util.stream.Collectors;

class RValueSetBatch implements ValueSetBatch {

  private final TibbleTable table;

  private final List<VariableEntity> entities;

  private final RValueSetFetcher fetcher;

  public RValueSetBatch(TibbleTable table, List<VariableEntity> entities) {
    this.table = table;
    this.entities = entities;
    this.fetcher = new RValueSetFetcher(table);
  }

  @Override
  public List<ValueSet> getValueSets() {
    RServerResult rexp = fetcher.getResult(entities);
    return entities.stream().map(e -> {
      RValueSet vs = new RValueSet(table, e);
      vs.parseResult(rexp);
      return vs;
    }).collect(Collectors.toList());
  }
}
