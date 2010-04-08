package org.obiba.magma.datasource.federation;

import java.util.Set;

import org.obiba.magma.Initialisable;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.AbstractValueTable;
import org.obiba.magma.support.ValueSetBean;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableEntityProvider;
import org.restlet.resource.ClientResource;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

class FederatedValueTable extends AbstractValueTable {

  public FederatedValueTable(FederatedDatasource datasource, String name) {
    super(datasource, name);
  }

  public FederatedDatasource getDatasource() {
    return (FederatedDatasource) super.getDatasource();
  }

  @Override
  public void initialise() {
    super.initialise();
    FederatedVariableEntityProvider provider = new FederatedVariableEntityProvider();
    provider.initialise();
    super.setVariableEntityProvider(provider);

    ClientResource r = createResource("/datasource/table/" + getName() + "/variables");
    Iterable<Variable> variables = getDatasource().readResource(r);
    for(final Variable v : variables) {
      addVariableValueSource(new VariableValueSource() {

        @Override
        public Variable getVariable() {
          return v;
        }

        @Override
        public Value getValue(ValueSet valueSet) {
          ClientResource resource = createResource("/datasource/table/" + getName() + "/" + getVariable().getName() + "/" + valueSet.getVariableEntity().getIdentifier());
          return getDatasource().readResource(resource);
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
    return new ValueSetBean(this, entity);
  }

  private ClientResource createResource(String suffix) {
    return new ClientResource(getDatasource().getServerUrl() + suffix);
  }

  private class FederatedVariableEntityProvider implements VariableEntityProvider, Initialisable {

    Iterable<String> identifiers;

    @Override
    public String getEntityType() {
      return "";
    }

    @Override
    public void initialise() {
      ClientResource r = createResource("/datasource/table/" + getName() + "/entities");
      identifiers = getDatasource().readResource(r);
    }

    @Override
    public Set<VariableEntity> getVariableEntities() {
      return ImmutableSet.copyOf(Iterables.transform(identifiers, new Function<String, VariableEntity>() {

        @Override
        public VariableEntity apply(String from) {
          return new VariableEntityBean("", from);
        }
      }));
    }

    @Override
    public boolean isForEntityType(String entityType) {
      return true;
    }

  }
}
