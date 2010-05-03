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

import java.util.Arrays;

import org.restlet.Application;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class TablesResource extends ServerResource {

  @Get("xml")
  public Iterable<String> getTables() {
    return Arrays.asList(Application.getCurrent().getContext().getParameters().getValuesArray("table"));
  }

}
