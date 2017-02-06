/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Writes a tibble in a R session.
 */
public class RValueTableWriter implements ValueTableWriter {

  private final RValueTable valueTable;

  private List<Variable> variables = Collections.synchronizedList(Lists.newArrayList());

  private Map<String, Map<String, Value>> variableEntityValues = Maps.newConcurrentMap();

  public RValueTableWriter(RValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public VariableWriter writeVariables() {
    return new RVariableWriter();
  }

  @Override
  public ValueSetWriter writeValueSet(@NotNull VariableEntity entity) {
    return new RValueSetWriter(entity);
  }

  @Override
  public void close() {

  }

  private class RVariableWriter implements VariableWriter {

    @Override
    public void writeVariable(@NotNull Variable variable) {
      variables.add(variable);
    }

    @Override
    public void removeVariable(@NotNull Variable variable) {
      throw new UnsupportedOperationException("Cannot remove a R variable (column in a tibble)");
    }

    @Override
    public void close() {

    }
  }

  private class RValueSetWriter implements ValueSetWriter {

    private final VariableEntity entity;

    private RValueSetWriter(VariableEntity entity) {
      this.entity = entity;
    }

    @Override
    public void writeValue(@NotNull Variable variable, Value value) {
      if (!variableEntityValues.containsKey(entity.getIdentifier()))
        variableEntityValues.put(entity.getIdentifier(), Maps.newHashMap());
      variableEntityValues.get(entity.getIdentifier()).put(variable.getName(), value);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Cannot remove a R value set (row in a tibble)");
    }

    @Override
    public void close() {

    }
  }
}
