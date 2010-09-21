/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.ValueTable;
import org.obiba.magma.views.View;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ViewDto;

public class ViewResource extends TableResource {
  //
  // Constructors
  //

  public ViewResource(View view) {
    super(view);
  }

  //
  // Methods
  //

  @GET
  public ViewDto getView(@Context final UriInfo uriInfo) {
    return ViewDtos.asDto((View) getValueTable());
  }

  @GET
  @Path("/from")
  public TableDto getFrom() {
    View view = (View) getValueTable();
    ValueTable fromTable = ((View) getValueTable()).getWrappedValueTable();
    UriBuilder tableLink = UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getTable");
    UriBuilder viewLink = UriBuilder.fromPath("/").path(DatasourceResource.class).path(DatasourceResource.class, "getView");

    return Dtos.asDto(fromTable, tableLink).setViewLink(viewLink.build(fromTable.getDatasource().getName(), view.getName()).toString()).build();
  }
}
