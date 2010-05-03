/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.magma.xstream.XStreamValueSet;
import org.restlet.data.Status;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.thoughtworks.xstream.XStream;

public class ValueSetResource extends AbstractTableResource {

  private final XStream xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();

  private ValueSet valueSet;

  @Override
  protected void doInit() throws ResourceException {
    super.doInit();

    if(getStatus().isClientError() == false) {
      String identifier = (String) getRequestAttributes().get("identifier");
      try {
        valueSet = getValueTable().getValueSet(new VariableEntityBean(getValueTable().getEntityType(), identifier));
      } catch(NoSuchValueSetException e) {
        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
      }
    }
  }

  @Override
  protected Representation get() throws ResourceException {
    XStreamValueSet vs = new XStreamValueSet(getValueTable().getName(), valueSet.getVariableEntity());
    for(Variable v : getValueTable().getVariables()) {
      vs.setValue(v, getValueTable().getValue(v, valueSet));
    }

    XstreamRepresentation<?> x = new XstreamRepresentation<Object>(vs);
    x.setXstream(xstream);
    return x;
  }
}
