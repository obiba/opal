package org.obiba.opal.rest.client.magma;

import java.util.Set;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.obiba.magma.Initialisable;
import org.obiba.magma.MagmaRuntimeException;
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
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.ValueSetDto;
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

    Iterable<VariableDto> variables = getDatasource().readResources(newReference("variables"), VariableDto.newBuilder());
    for(final VariableDto dto : variables) {
      addVariableValueSource(new VariableValueSource() {

        private final Variable v;
        
        {
          v = Variable.Builder.newVariable(dto.getName(), ValueType.Factory.forName(dto.getValueType()), dto.getEntityType()).build();
        }

        @Override
        public Variable getVariable() {
          return v;
        }

        @Override
        public Value getValue(ValueSet valueSet) {
          return ((LazyValueSet) valueSet).get(v.getName());
        }

        @Override
        public ValueType getValueType() {
          return v.getValueType();
        }

      });
    }
  }

  @Override
  public ValueSet getValueSet(VariableEntity entity) throws NoSuchValueSetException {
    return new LazyValueSet(this, entity);
  }

  URI newReference(String... segments) {
    try {
      URI uri = this.tableReference;
      for(String segment : segments) {
        segment = segment.endsWith("/") ? segment : segment + "/";
        uri = new URI(uri, segment, false);
      }
      return uri;
    } catch(URIException e) {
      throw new MagmaRuntimeException(e);
    }
  }

  private class FederatedVariableEntityProvider implements VariableEntityProvider, Initialisable {

    Iterable<VariableEntityDto> entities;

    @Override
    public String getEntityType() {
      return tableDto.getEntityType();
    }

    @Override
    public void initialise() {
      entities = getDatasource().readResources(newReference("entities"), VariableEntityDto.newBuilder());
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
    
    private ValueSetDto valueSet;

    private LazyValueSet(ValueTable table, VariableEntity entity) {
      super(table, entity);
    }
    
    public Value get(String name) {
      ValueSetDto valueSet = loadValueSet();
      for(int i = 0; i < valueSet.getVariablesCount(); i++) {
        if(name.equals(valueSet.getVariables(i))) {
          ValueDto value = valueSet.getValues(i);
          ValueType type = ValueType.Factory.forName(value.getValueType());
          String stringValue = value.hasValue() ? value.getValue() : null;
          return value.getIsSequence() ? type.sequenceOf(stringValue) : type.valueOf(stringValue);
        }
      }
      throw new NoSuchVariableException(name);
    }

    synchronized ValueSetDto loadValueSet() {
      if(valueSet == null) {
        valueSet = getDatasource().readResource(newReference("valueSet", getVariableEntity().getIdentifier()), ValueSetDto.newBuilder());
      }
      return valueSet;
    }
  }

  @Override
  public Timestamps getTimestamps(ValueSet valueSet) {
    return new Timestamps() {
      
      @Override
      public Value getLastUpdate() {
        return DateTimeType.get().nullValue();
      }
      
      @Override
      public Value getCreated() {
        return DateTimeType.get().nullValue();
      }
    };
  }
}
