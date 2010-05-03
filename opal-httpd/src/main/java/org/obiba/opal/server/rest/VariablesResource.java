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
import org.obiba.magma.Variable;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.thoughtworks.xstream.XStream;

public class VariablesResource extends AbstractTableResource {

  private final XStream xstream = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();

  @Override
  protected Representation get() throws ResourceException {
    XstreamRepresentation<Iterable<Variable>> representation = new XstreamRepresentation<Iterable<Variable>>(getValueTable().getVariables());
    representation.setXstream(xstream);
    return representation;
  }

}
