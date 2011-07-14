/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

import org.obiba.magma.ValueTable;

/**
 * A table resource that supports DELETE (drop)
 */
public class DroppableTableResource extends TableResource {

  public DroppableTableResource(ValueTable valueTable, Set<Locale> locales) {
    super(valueTable, locales);
  }

  @DELETE
  public Response drop() {
    getDatasource().dropTable(this.getValueTable().getName());
    return Response.ok().build();
  }
}
