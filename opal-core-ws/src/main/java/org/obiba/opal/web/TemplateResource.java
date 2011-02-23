/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.stereotype.Component;

@Component
@Path("/templates")
@NoAuthorization
public class TemplateResource {

  private MimetypesFileTypeMap mimeTypes;

  public TemplateResource() {
    super();
    this.mimeTypes = new MimetypesFileTypeMap();
  }

  @GET
  @Path("/{templateFilename:[\\w\\.]+}")
  @AuthenticatedByCookie
  public Response getTemplate(@PathParam("templateFilename") String templateFilename) throws IOException {
    URL templateURL = getClass().getResource("/META-INF/templates/" + templateFilename);
    if(templateURL != null) {
      String mimeType = mimeTypes.getContentType(new File(templateFilename));
      return Response.ok(templateURL.getContent(), MediaType.valueOf(mimeType)).header("Content-Disposition", getContentDispositionOfAttachment(templateFilename)).build();
    } else {
      return Response.status(Status.NOT_FOUND).entity("The template specified does not exist: " + templateFilename).build();
    }
  }

  private String getContentDispositionOfAttachment(String fileName) {
    return "attachment; filename=\"" + fileName + "\"";
  }
}
