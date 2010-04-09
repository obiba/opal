package org.obiba.opal.client.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;

import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.XStream;

public class RestDatasource extends AbstractDatasource {

  private final XStream xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();

  private final Reference datasourceReference;

  private final Client client;

  public RestDatasource(String name, Reference datasourceReference, Client client) {
    super(name, "rest");
    this.client = client;
    this.datasourceReference = datasourceReference;
  }

  @Override
  protected void onInitialise() {
    try {
      client.start();
    } catch(Exception e) {
      throw new MagmaRuntimeException(e);
    }
    super.onInitialise();
  }

  @Override
  protected void onDispose() {
    super.onDispose();
    try {
      client.stop();
    } catch(Exception ignore) {
      // ignore
    }
  }

  @Override
  protected Set<String> getValueTableNames() {
    Iterable<String> s = readResource(newReference().addSegment("tables"));
    return ImmutableSet.copyOf(s);
  }

  @Override
  protected ValueTable initialiseValueTable(final String tableName) {
    return new RestValueTable(this, tableName);
  }

  Reference newReference() {
    return new Reference(datasourceReference);
  }

  @SuppressWarnings("unchecked")
  <T> T readResource(Reference reference) {
    InputStream is = null;
    try {
      Response response = client.handle(new Request(Method.GET, reference));
      is = response.getEntity().getStream();
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

}
