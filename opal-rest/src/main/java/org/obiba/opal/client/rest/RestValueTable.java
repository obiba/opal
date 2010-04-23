package org.obiba.opal.client.rest;

import java.util.Set;

import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
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
import org.obiba.magma.xstream.XStreamValueSet;
import org.restlet.data.Reference;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

class RestValueTable extends AbstractValueTable {

  private final Reference tableReference;

  public RestValueTable(RestDatasource datasource, String name) {
    super(datasource, name);
    tableReference = getDatasource().newReference().addSegment("table").addSegment(name);
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

    Iterable<Variable> variables = getDatasource().readResource(newReference().addSegment("variables"));
    for(final Variable v : variables) {
      addVariableValueSource(new VariableValueSource() {

        @Override
        public Variable getVariable() {
          return v;
        }

        @Override
        public Value getValue(ValueSet valueSet) {
          return ((LazyValueSet) valueSet).loadValueSet().getValue(v);
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

  private Reference newReference() {
    return new Reference(this.tableReference);
  }

  private class FederatedVariableEntityProvider implements VariableEntityProvider, Initialisable {

    Iterable<String> identifiers;

    @Override
    public String getEntityType() {
      // TODO: fetch this from the service
      return "Participant";
    }

    @Override
    public void initialise() {
      identifiers = getDatasource().readResource(newReference().addSegment("entities"));
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return ImmutableSet.copyOf(Iterables.transform(identifiers, new Function<String, VariableEntity>() {

        @Override
        public VariableEntity apply(String from) {
          return new VariableEntityBean(getEntityType(), from);
        }
      }));
    }

    @Override
    public boolean isForEntityType(String entityType) {
      return getEntityType().equals(entityType);
    }

  }

  private class LazyValueSet extends ValueSetBean {

    private XStreamValueSet set;

    private LazyValueSet(ValueTable table, VariableEntity entity) {
      super(table, entity);
    }

    synchronized XStreamValueSet loadValueSet() {
      if(set == null) {
        set = getDatasource().readResource(newReference().addSegment("valueSet").addSegment(getVariableEntity().getIdentifier()));
      }
      return set;
    }
  }
}
