/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.reporting;

import java.io.File;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/report")
public class ReportResource {

  private static final Logger log = LoggerFactory.getLogger(ReportResource.class);

  private final OpalRuntime opalRuntime;

  private MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

  @Autowired
  public ReportResource(OpalRuntime opalRuntime) {
    super();
    this.opalRuntime = opalRuntime;
  }

  @GET
  @Path("/public/{obfuscated-file:.*}")
  @NotAuthenticated
  public Response getReport(@PathParam("obfuscated-file") String obfuscatedFile) throws FileSystemException {
    // TODO: go through reports directory content, hash the report paths "<report template name>/<report file name>" and
    // compare to 'obfuscated-file'
    String file = obfuscatedFile;
    File report = opalRuntime.getFileSystem().getLocalFile(resolveFileInFileSystem("/reports/" + file));
    if(!report.exists()) {
      return Response.status(Status.NOT_FOUND).build();
    }
    String mimeType = mimeTypes.getContentType(report);
    return Response.ok(report, MediaType.valueOf(mimeType)).header("Content-Disposition", getContentDispositionOfAttachment(report.getName())).build();
  }

  protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  private String getContentDispositionOfAttachment(String fileName) {
    return "attachment; filename=\"" + fileName + "\"";
  }
}
