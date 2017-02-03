/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.views.JavascriptClause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.*;

abstract class AbstractValueTableResource {

  private ValueTable valueTable;

  private Set<Locale> locales = new HashSet<>();

  ApplicationContext applicationContext;

  @Autowired
  void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void setLocales(Set<Locale> locales) {
    this.locales = locales;
  }

  public void setValueTable(ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  public ValueTable getValueTable() {
    return valueTable;
  }

  Datasource getDatasource() {
    return valueTable.getDatasource();
  }

  public Set<Locale> getLocales() {
    return Collections.unmodifiableSet(locales);
  }

  LocalesResource getLocalesResource() {
    LocalesResource resource = applicationContext.getBean(LocalesResource.class);
    resource.setLocales(locales);
    return resource;
  }

  Iterable<Variable> filterVariables(String script, Integer offset, @Nullable Integer limit) {
    List<Variable> filteredVariables;

    if(script != null) {
      JavascriptClause jsClause = new JavascriptClause(script);
      jsClause.initialise();

      filteredVariables = new ArrayList<>();
      for(Variable variable : getValueTable().getVariables()) {
        if(jsClause.select(variable)) {
          filteredVariables.add(variable);
        }
      }
    } else {
      filteredVariables = Lists.newArrayList(getValueTable().getVariables());
    }

    int fromIndex = offset < filteredVariables.size() ? offset : filteredVariables.size();
    int toIndex = limit != null && limit >= 0 //
        ? Math.min(fromIndex + limit, filteredVariables.size()) //
        : filteredVariables.size();

    orderVariables(filteredVariables);

    return filteredVariables.subList(fromIndex, toIndex);
  }

  protected List<Variable> orderVariables(List<Variable> variables) {
    Collections.sort(variables, (o1, o2) -> o1.getIndex() - o2.getIndex());
    return variables;
  }

  Iterable<VariableEntity> filterEntities(@Nullable Integer offset, @Nullable Integer limit) {
    Iterable<VariableEntity> entities;
    entities = Sets.newTreeSet(valueTable.getVariableEntities());
    // Apply offset then limit (in that order)
    if(offset != null) {
      entities = Iterables.skip(entities, offset);
    }
    if(limit != null && limit >= 0) {
      entities = Iterables.limit(entities, limit);
    }
    return entities;
  }

}
