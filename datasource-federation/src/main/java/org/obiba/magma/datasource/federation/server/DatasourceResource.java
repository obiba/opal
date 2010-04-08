package org.obiba.magma.datasource.federation.server;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.federation.MagmaFederationExtension;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.xstream.MagmaXStreamExtension;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.thoughtworks.xstream.XStream;

@Path("/datasource")
public class DatasourceResource {

  private final XStream xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();

  @GET
  @Path("/tables")
  @Produces("text/xml")
  public String getValueTables() {
    List<ValueTable> tables = MagmaEngine.get().getExtension(MagmaFederationExtension.class).getFederatedTables();
    return xstream.toXML(ImmutableSet.copyOf(Iterables.transform(tables, new Function<ValueTable, String>() {
      @Override
      public String apply(ValueTable from) {
        return from.getName();
      }
    })));
  }

  @GET
  @Path("/table/{table}/variables")
  @Produces("text/xml")
  public String getVariables(@PathParam("table") String tableName) {
    ValueTable table = getTable(tableName);
    return xstream.toXML(table.getVariables());
  }

  @GET
  @Path("/table/{table}/entities")
  @Produces("text/xml")
  public String getVariableEntities(@PathParam("table") String tableName) {
    ValueTable table = getTable(tableName);
    return xstream.toXML(ImmutableSet.copyOf(Iterables.transform(table.getValueSets(), new Function<ValueSet, String>() {
      @Override
      public String apply(ValueSet from) {
        return from.getVariableEntity().getIdentifier();
      }
    })));
  }

  @GET
  @Path("/table/{table}/{variable}/{valueSet}")
  @Produces("text/xml")
  public String getValue(@PathParam("table") String tableName, @PathParam("variable") String variable, @PathParam("valueSet") String entityIdentifier) {
    ValueTable table = getTable(tableName);
    ValueSet valueSet = table.getValueSet(new VariableEntityBean(table.getEntityType(), entityIdentifier));
    return xstream.toXML(table.getVariableValueSource(variable).getValue(valueSet));
  }

  protected ValueTable getTable(final String name) {
    List<ValueTable> tables = MagmaEngine.get().getExtension(MagmaFederationExtension.class).getFederatedTables();
    return Iterables.find(tables, new Predicate<ValueTable>() {

      @Override
      public boolean apply(ValueTable input) {
        return input.getName().equals(name);
      }

    });
  }
}
