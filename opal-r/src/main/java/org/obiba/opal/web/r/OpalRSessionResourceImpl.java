/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import com.google.common.base.Strings;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.spi.r.FileReadROperation;
import org.obiba.opal.spi.r.FileWriteROperation;
import org.obiba.opal.spi.r.RScriptROperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Handles web services on a particular R session of the invoking Opal user.
 */
@Component("opalRSessionResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class OpalRSessionResourceImpl extends AbstractRSessionResource implements OpalRSessionResource {

  @Autowired
  private OpalRuntime opalRuntime;

  @Override
  public Response execute(String script, boolean async, String body) {
    String rScript = script;
    if(Strings.isNullOrEmpty(rScript)) {
      rScript = body;
    }
    return RSessionResourceHelper.executeScript(getOpalRSession(), rScript, async);
  }

  @Override
  public Response pushFile(String source, String destination) throws FileSystemException {
    if (Strings.isNullOrEmpty(source)) return Response.status(Response.Status.BAD_REQUEST) //
      .entity("Source file is missing.").build();
    // source file must exists and be accessible
    String sourcePath = source;
    if (source.startsWith("~")) sourcePath = source.replaceFirst("~", "/home/" + getOpalRSession().getUser());
    FileObject file = resolveFileInFileSystem(sourcePath);
    if (!file.exists() || !file.isReadable()) return Response.status(Response.Status.NOT_FOUND) //
        .entity("The file does not exist or is not readable: " + sourcePath).build();
    if (file.getType() != FileType.FILE) return Response.status(Response.Status.BAD_REQUEST) //
        .entity("The file must not be a folder: " + sourcePath).build();
    // destination must be relative
    if (!Strings.isNullOrEmpty(destination) &&
        (destination.startsWith("~") || destination.startsWith("/") || destination.startsWith("$")))
      return Response.status(Response.Status.BAD_REQUEST) //
        .entity("Destination file must be relative to R workspace.").build();
    String dest = prepareDestinationInR(destination, file.getName().getBaseName());
    try {
      FileWriteROperation rop = new FileWriteROperation(dest, opalRuntime.getFileSystem().getLocalFile(file));
      getOpalRSession().execute(rop);
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Cannot write file to R workspace: " + source).build();
    }
    return Response.ok().build();
  }

  @Override
  public Response pullFile(String source, String destination) throws FileSystemException {
    if (Strings.isNullOrEmpty(source)) return Response.status(Response.Status.BAD_REQUEST) //
        .entity("Source file is missing.").build();
    if (Strings.isNullOrEmpty(destination)) return Response.status(Response.Status.BAD_REQUEST) //
        .entity("Destination file or folder is missing.").build();
    if (source.startsWith("~") || source.startsWith("/") || source.startsWith("$")) return Response.status(Response.Status.BAD_REQUEST) //
        .entity("Source file must be relative to R workspace.").build();
    String sourceName = source;
    if (source.contains("/")) sourceName = source.substring(source.lastIndexOf("/") + 1);
    String destinationPath = destination;
    if (destination.startsWith("~")) destinationPath = destination.replaceFirst("~", "/home/" + getOpalRSession().getUser());
    FileObject dest = resolveFileInFileSystem(destinationPath);
    if ((dest.exists() && !dest.isWriteable()) || (!dest.exists() && !dest.getParent().isWriteable())) return Response.status(Response.Status.BAD_REQUEST) //
        .entity("Destination file is not accessible for writing.").build();
    File file = prepareDestinationInOpal(dest, sourceName);
    try {
      FileReadROperation rop = new FileReadROperation(source, file);
      getOpalRSession().execute(rop);
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Cannot read file from R workspace: " + source).build();
    }
    return Response.ok().build();
  }

  //
  // private methods
  //

  private String prepareDestinationInR(String path, String defaultName) {
    String destination = path;
    if (Strings.isNullOrEmpty(destination)) destination = defaultName;
    if (destination.startsWith("/")) destination = destination.substring(1);
    if (destination.contains("/")) {
      // make sure destination directory exists
      String rscript = String.format("base::dir.create('%s', showWarnings=FALSE, recursive=TRUE)", destination.substring(0, destination.lastIndexOf("/")));
      RScriptROperation rop = new RScriptROperation(rscript, false);
      getOpalRSession().execute(rop);
    }
    return destination;
  }

  private File prepareDestinationInOpal(FileObject file, String defaultName) throws FileSystemException {
    File destination = opalRuntime.getFileSystem().getLocalFile(file);
    if (file.exists() && file.getType() == FileType.FOLDER) destination = new File(destination, defaultName);
    return destination;
  }

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

}
