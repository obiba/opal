package org.obiba.magma.datasource.federation;

import java.util.List;

import org.obiba.magma.Disposable;
import org.obiba.magma.MagmaEngineExtension;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.datasource.federation.server.FederationApp;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class MagmaFederationExtension implements MagmaEngineExtension, Disposable {

  private final Component comp = new Component();

  private List<ValueTable> federatedTables = Lists.newArrayList();

  public void addFederatedTable(ValueTable table) {
    this.federatedTables.add(table);
  }

  public List<ValueTable> getFederatedTables() {
    return ImmutableList.copyOf(federatedTables);
  }

  @Override
  public String getName() {
    return "federation";
  }

  @Override
  public void initialise() {

    Server server = comp.getServers().add(Protocol.HTTP, 8182);

    // create JAX-RS runtime environment
    JaxRsApplication application = new JaxRsApplication(comp.getContext());

    // attach ApplicationConfig
    application.add(new FederationApp());

    // Attach the application to the component and start it
    comp.getDefaultHost().attach(application);
    try {
      System.out.println("Starting server");
      comp.start();
      System.out.println("Server started on port " + server.getPort());
    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }
  }

  @Override
  public void dispose() {
    try {
      System.out.println("Stopping server");
      comp.stop();
      System.out.println("Server stopped");
    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }
  }
}
