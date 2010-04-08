package org.obiba.magma.datasource.federation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.XStream;

public class FederatedDatasource extends AbstractDatasource {

  private final String serverUrl;

  private final XStream xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();

  public FederatedDatasource(String name, String datasourceUrl) {
    super(name, "federated");
    this.serverUrl = datasourceUrl;
  }

  @Override
  protected Set<String> getValueTableNames() {
    Iterable<String> s = readResource(new ClientResource(serverUrl + "/datasource/tables"));
    return ImmutableSet.copyOf(s);
  }

  @Override
  protected ValueTable initialiseValueTable(final String tableName) {
    return new FederatedValueTable(this, tableName);
  }

  @SuppressWarnings("unchecked")
  <T> T readResource(ClientResource resource) {
    InputStream is = null;
    try {
      is = resource.get().getStream();
      return (T) xstream.fromXML(is);
    } catch(ResourceException e) {
      throw new MagmaRuntimeException(e);
    } catch(IOException e) {
      throw new MagmaRuntimeException(e);
    } finally {
      try {
        if(is != null) is.close();
      } catch(IOException e) {
      }
    }
  }

  String getServerUrl() {
    return this.serverUrl;
  }
}
