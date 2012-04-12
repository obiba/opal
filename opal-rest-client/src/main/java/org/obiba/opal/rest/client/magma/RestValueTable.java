package org.obiba.opal.rest.client.magma;

import java.net.URI;
import java.util.Set;

import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.NoSuchVariableException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.NullTimestamps;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.obiba.opal.web.model.Magma.ValueSetsDto.ValueSetDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

class RestValueTable extends AbstractValueTable {

  private final TableDto tableDto;

  private final URI tableReference;

  public RestValueTable(RestDatasource datasource, TableDto dto) {
    super(datasource, dto.getName());
    this.tableDto = dto;
    tableReference = getDatasource().newReference("table", dto.getName());
  }

  public RestDatasource getDatasource() {
    return (RestDatasource) super.getDatasource();
  }

  @Override
  public void initialise() {
    super.initialise();
    FederatedVariableEntityProvider provider = new FederatedVariableEntityProvider();
    provider.initialise();
    super.setVariableEntityProvider(provider);

    Iterable<VariableDto> variables = getOpalClient().getResources(VariableDto.class, newReference("variables"), VariableDto.newBuilder());
    for(final VariableDto dto : variables) {
      addVariableValueSource(new VariableValueSource() {

        private final Variable v = Dtos.fromDto(dto);

        @Override
        public Variable getVariable() {
          return v;
        }

        @Override
        public Value getValue(ValueSet valueSet) {
          return ((LazyValueSet) valueSet).get(v);
        }

        @Override
        public ValueType getValueType() {
          return v.getValueType();
        }

        @Override
        public VectorSource asVectorSource() {
          return null;
        }

      });
    }
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new LazyValueSet(this, entity);
  }

  void refresh() {
    getSources().clear();
    initialise();
  }

  OpalJavaClient getOpalClient() {
    return getDatasource().getOpalClient();
  }

  URI newReference(String... segments) {
    return getDatasource().buildURI(this.tableReference, segments);
  }

  UriBuilder newUri(String... segments) {
    return getDatasource().uriBuilder(this.tableReference).segment(segments);
  }

  private class FederatedVariableEntityProvider implements VariableEntityProvider, Initialisable {

    Iterable<VariableEntityDto> entities;

    @Override
    public String getEntityType() {
      return tableDto.getEntityType();
    }

    @Override
    public void initialise() {
      entities = getOpalClient().getResources(VariableEntityDto.class, newReference("entities"), VariableEntityDto.newBuilder());
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return ImmutableSet.copyOf(Iterables.transform(entities, new Function<VariableEntityDto, VariableEntity>() {

        @Override
        public VariableEntity apply(VariableEntityDto from) {
          return new VariableEntityBean(getEntityType(), from.getIdentifier());
        }
      }));
    }

    @Override
    public boolean isForEntityType(String entityType) {
      return getEntityType().equals(entityType);
    }

  }

  private class LazyValueSet extends ValueSetBean {

    private ValueSetsDto valueSet;

    private LazyValueSet(ValueTable table, VariableEntity entity) {
      super(table, entity);
    }

    public Value get(Variable variable) {
      ValueSetsDto valueSet = loadValueSet();
      ValueSetsDto.ValueSetDto values = valueSet.getValueSets(0);

      for(int i = 0; i < valueSet.getVariablesCount(); i++) {
        if(variable.getName().equals(valueSet.getVariables(i))) {
          return Dtos.fromDto(values.getValues(i), variable.getValueType(), variable.isRepeatable());
        }
      }
      throw new NoSuchVariableException(variable.getName());
    }

    synchronized ValueSetsDto loadValueSet() {
      if(valueSet == null) {
        valueSet = getOpalClient().getResource(ValueSetsDto.class, newUri("valueSet", getVariableEntity().getIdentifier()).query("filterBinary", "false").build(), ValueSetsDto.newBuilder());
      }
      return valueSet;
    }
  }

  @Override
  public Timestamps getTimestamps() {
    return NullTimestamps.get();
  }
}
