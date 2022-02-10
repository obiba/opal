/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.ViewAwareDatasource;
import org.obiba.opal.spi.r.RServerResult;
import org.obiba.opal.spi.r.resource.IRTabularResourceConnector;
import org.obiba.opal.spi.resource.TabularResourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A view that accesses a resource in its tabular form.
 */
public class ResourceView implements ValueView, TibbleTable, Initialisable, Disposable {

  private static final Logger log = LoggerFactory.getLogger(ResourceView.class);

  private static final String ID_COLUMN_DEFAULT = "id";

  // name of the view
  private String name;

  // project in which the resource is defined
  private String project;

  // resource name
  private String resource;

  // the entity type of the view
  private String entityType;

  // the column that identifies the entities
  private String idColumn = ID_COLUMN_DEFAULT;

  // created date
  private Value created = DateTimeType.get().now();

  // updated date
  private Value updated = DateTimeType.get().now();

  private Set<Variable> variables = new LinkedHashSet<>();

  // the datasource to which this view is attached to
  private transient ViewAwareDatasource viewAwareDatasource;

  private transient Set<VariableValueSource> variableValueSources;

  private transient boolean initialised = false;

  private transient IRTabularResourceConnector connector;

  private transient List<VariableEntity> entities;

  private transient int idPosition = 0;

  private transient Map<String, Integer> columnPositions = Maps.newHashMap();

  public ResourceView() {
  }

  public void setConnector(TabularResourceConnector connector) {
    this.connector = (IRTabularResourceConnector) connector;
  }

  public TabularResourceConnector getConnector() {
    return connector;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public String getProject() {
    return project;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getResource() {
    return resource;
  }

  public void setIdColumn(String idColumn) {
    this.idColumn = idColumn;
  }

  public String getIdColumn() {
    return idColumn;
  }

  public String getResourceFullName() {
    return String.format("%s.%s", project, resource);
  }

  public void setVariables(Collection<Variable> variables) {
    this.variables.clear();
    this.variables.addAll(variables);
  }

  @Override
  public synchronized void initialise() {
    try {
      Initialisables.initialise(connector);
      initialiseIdColumn();
      initialiseVariables();
      initialised = true;
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.error("Resource view '{}' init failed", name, e);
      else
        log.warn("Resource view '{}' init failed: {}", name, e.getMessage());
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Datasource getDatasource() {
    return viewAwareDatasource;
  }

  @Override
  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  @Override
  public boolean isForEntityType(String entityType) {
    return Objects.equals(getEntityType(), entityType);
  }

  //
  // Table based on a tibble
  //

  @Override
  public String getSymbol() {
    return connector.getSymbol();
  }

  @Override
  public RServerResult execute(String script) {
    return connector.execute(script);
  }

  @Override
  public int getIdPosition() {
    return idPosition;
  }

  @Override
  public Map<String, Integer> getColumnPositions() {
    return connector.getColumns().stream()
        .collect(Collectors.toMap(TabularResourceConnector.Column::getName, TabularResourceConnector.Column::getPosition));
  }

  @Override
  public boolean isMultilines() {
    // TODO check whether it is multi line (compare id column length vs distinct length)
    return false;
  }

  @Override
  public String getDefaultLocale() {
    // TODO ???
    return "en";
  }

  @Override
  public RVariableEntity getRVariableEntity(VariableEntity entity) {
    if (entity instanceof RVariableEntity) return (RVariableEntity) entity;
    return new RVariableEntity(entity.getType(), entity.getIdentifier());
  }

  private ValueSetBatch getValueSetsBatch(List<VariableEntity> entities) {
    return new RValueSetBatch(this, entities);
  }

  //
  // Entities
  //

  @Override
  public List<VariableEntity> getVariableEntities() {
    if (entities == null) {
      if (connector.hasColumn(idColumn)) {
        entities = connector.getColumn(idColumn).asVector(TextType.get()).stream()
            .filter(val -> val != null && !val.isNull())
            .map(Value::toString)
            .distinct()
            .map(id -> new RVariableEntity(getEntityType(), id))
            .collect(Collectors.toList());
      } else {
        entities = Lists.newArrayList();
      }
    }
    return entities;
  }

  @Override
  public int getVariableEntityCount() {
    return getVariableEntities().size();
  }

  //
  // ValueSet
  //

  @Override
  public boolean hasValueSet(VariableEntity entity) {
    return getVariableEntities().stream()
        .anyMatch(e -> entity.getIdentifier().equals(e.getIdentifier()));
  }

  @Override
  public Iterable<ValueSet> getValueSets() {
    return getValueSets(entities);
  }

  @Override
  public Iterable<ValueSet> getValueSets(Iterable<VariableEntity> entities) {
    return () -> new ValueSetIterator(entities);
    /*return StreamSupport.stream(entities.spliterator(), false)
        .map(this::getValueSet)
        .collect(Collectors.toList());*/
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new RValueSet(this, entity);
  }

  @Override
  public boolean canDropValueSets() {
    return false;
  }

  @Override
  public void dropValueSets() {

  }

  @Override
  public Timestamps getValueSetTimestamps(VariableEntity entity) throws NoSuchValueSetException {
    return getValueSet(entity).getTimestamps();
  }

  @Override
  public Iterable<Timestamps> getValueSetTimestamps(List<VariableEntity> entities) {
    return entities.stream().map(this::getValueSetTimestamps).collect(Collectors.toList());
  }

  //
  // Variables
  //

  @Override
  public boolean hasVariable(String name) {
    return false;
  }

  @Override
  public Iterable<Variable> getVariables() {
    ensureInitialised();
    return variables;
  }

  @Override
  public int getVariableCount() {
    return variables.size();
  }

  @Override
  public Variable getVariable(String name) throws NoSuchVariableException {
    return variables.stream().filter(var -> var.getName().equals(name)).findFirst().get();
  }

  @Override
  public Value getValue(Variable variable, ValueSet valueSet) {
    return getVariableValueSource(variable.getName()).getValue(valueSet);
  }

  @Override
  public VariableValueSource getVariableValueSource(String variableName) throws NoSuchVariableException {
    ensureInitialised();
    if (!initialised) {
      throw new IllegalStateException("The initialise() method must be called before getVariableValueSource().");
    }
    for (VariableValueSource variableValueSource : variableValueSources) {
      if (variableValueSource.getVariable().getName().equals(variableName)) {
        return variableValueSource;
      }
    }
    throw new NoSuchVariableException(variableName);
  }

  @Override
  public void setDatasource(ViewAwareDatasource datasource) {
    this.viewAwareDatasource = datasource;
  }

  @Override
  public ValueTableWriter.VariableWriter createVariableWriter() {
    return new ResourceVariableWriter();
  }

  @Override
  public void setCreated(@Nullable Value created) {
    this.created = created;
  }

  @Override
  public void setUpdated(@Nullable Value updated) {
    this.updated = updated;
  }

  @Override
  public String getTableReference() {
    return ValueTable.Reference.getReference(getDatasource() == null ? "null" : getDatasource().getName(), getName());
  }

  @Override
  public Timestamps getTimestamps() {
    return new Timestamps() {
      @Override
      public Value getLastUpdate() {
        return updated;
      }

      @Override
      public Value getCreated() {
        return created;
      }
    };
  }

  //
  // Private methods
  //

  private synchronized void ensureInitialised() {
    if (!initialised) {
      initialise();
    }
  }

  private void initialiseIdColumn() {
    connector.getColumns().stream()
        .filter(col -> col.getName().equals(idColumn))
        .findFirst().ifPresent(column -> idPosition = column.getPosition());
  }

  private void initialiseVariables() {
    if (variables.isEmpty()) {
      connector.getColumns().stream()
          .filter(column -> !idColumn.equals(column.getName()))
          .forEach(column -> {
            variables.add(column.asVariable(getEntityType()));
          });
    }

    Map<String, TabularResourceConnector.Column> columnByName = connector.getColumns().stream()
        .collect(Collectors.toMap(TabularResourceConnector.Column::getName, Function.identity()));

    variableValueSources = variables.stream()
        .map(var -> new ResourceVariableValueSource(var, columnByName.get(var.getName()), this))
        .collect(Collectors.toSet());
    for (VariableValueSource vvs : variableValueSources) {
      try {
        Initialisables.initialise(vvs);
      } catch (MagmaRuntimeException ignored) {
        log.debug("Error while initialising the Resource view variable", ignored);
      }
    }
  }

  @Override
  public void dispose() {
    Disposables.silentlyDispose(connector);
  }

  //
  // Inner classes
  //

  private class ResourceVariableWriter implements ValueTableWriter.VariableWriter {
    @Override
    public void writeVariable(Variable variable) {
      // update or add variable
      Set<Variable> variableSet = new LinkedHashSet<>();
      boolean updated = false;
      for (Variable var : variables) {
        if (var.getName().equals(variable.getName())) {
          variableSet.add(variable);
          updated = true;
        } else {
          variableSet.add(var);
        }
      }

      if (!updated) {
        variableSet.add(variable);
      }

      variables = variableSet;
    }

    @Override
    public void removeVariable(Variable variable) {
      // update or remove variable
      Set<Variable> variableSet = new LinkedHashSet<>();
      variableSet.addAll(variables.stream()
          .filter(var -> !var.getName().equals(variable.getName()))
          .collect(Collectors.toSet()));
      variables = variableSet;
    }

    @Override
    public void close() {
      // TODO better init
      initialiseVariables();
    }
  }

  /**
   * Lazy iterator of value sets: will make batch queries for extracting value sets.
   */
  private class ValueSetIterator implements Iterator<ValueSet> {

    private final Iterator<List<VariableEntity>> partitions;

    private Iterator<ValueSet> currentBatch;

    public ValueSetIterator(Iterable<VariableEntity> entities) {
      this.partitions = Iterables.partition(entities, getVariableEntityBatchSize()).iterator();
    }

    @Override
    public boolean hasNext() {
      synchronized (partitions) {
        return partitions.hasNext() || (currentBatch != null && currentBatch.hasNext());
      }
    }

    @Override
    public ValueSet next() {
      synchronized (partitions) {
        if (currentBatch == null || !currentBatch.hasNext()) {
          currentBatch = getValueSetsBatch(partitions.next()).getValueSets().iterator();
        }
        return currentBatch.next();
      }
    }
  }
}
