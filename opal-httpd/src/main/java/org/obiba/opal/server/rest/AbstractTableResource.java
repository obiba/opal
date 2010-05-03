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

import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.restlet.Application;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 *
 */
public class AbstractTableResource extends ServerResource {

  private ValueTable valueTable;

  @Override
  protected void doInit() throws ResourceException {
    super.doInit();
    String table = (String) getRequestAttributes().get("table");
    String datasource = Application.getCurrent().getContext().getParameters().getFirstValue(table);
    try {
      valueTable = MagmaEngineTableResolver.valueOf(datasource + "." + table).resolveTable();
    } catch(NoSuchValueTableException e) {
      setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    } catch(IllegalArgumentException e) {
      setStatus(Status.CLIENT_ERROR_NOT_FOUND);
    }
  }

  protected ValueTable getValueTable() {
    return valueTable;
  }
}
