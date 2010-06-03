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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.model.Opal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/files")
public class FilesResource {

  private static final Logger log = LoggerFactory.getLogger(FilesResource.class);

  private OpalRuntime opalRuntime;

  @Autowired
  public FilesResource(OpalRuntime opalRuntime) {
    super();
    this.opalRuntime = opalRuntime;
  }

  @GET
  public Opal.FileDto getFileSystem() throws FileSystemException {

    // Create a root FileDto representing the root of the FileSystem.
    Opal.FileDto.Builder fileBuilder = Opal.FileDto.newBuilder();
    fileBuilder.setName("root").setType(Opal.FileDto.FileType.FOLDER).setPath("/");

    // Create FileDtos for each file & folder in the FileSystem and add them to the root FileDto recursively.
    addFiles(fileBuilder, opalRuntime.getFileSystem().getRoot());

    return fileBuilder.build();
  }

  private void addFiles(Opal.FileDto.Builder parentFolderBuilder, FileObject parentFolder) throws FileSystemException {
    Opal.FileDto.Builder fileBuilder;

    // Get the children for the current folder (list of files & folders).
    FileObject[] children = parentFolder.getChildren();

    // Loop through all children.
    for(int i = 0; i < children.length; i++) {

      // Build a FileDto representing the child.
      fileBuilder = Opal.FileDto.newBuilder();
      fileBuilder.setName(children[i].getName().getBaseName()).setPath(children[i].getName().getPath());
      fileBuilder.setType(children[i].getType() == FileType.FILE ? Opal.FileDto.FileType.FILE : Opal.FileDto.FileType.FOLDER);

      // If the current child is a folder, add its children recursively.
      if(children[i].getType() == FileType.FOLDER) {
        addFiles(fileBuilder, children[i]);
      }

      // Add the current child to the parent FileDto (folder).
      parentFolderBuilder.addChildren(fileBuilder.build());
    }
  }

  @GET
  @Path("/{path:.*}")
  public Response getFolder(@PathParam("path") String path) throws FileSystemException {

    // If path does not refer to a folder.
    FileObject folder = opalRuntime.getFileSystem().getRoot().resolveFile(path);
    if(!folder.exists() || folder.getType() != FileType.FOLDER) {
      return Response.status(Status.NOT_FOUND).build();
    } else {

      // Create a FileDto representing the folder identified by the path.
      Opal.FileDto.Builder fileBuilder = Opal.FileDto.newBuilder();
      fileBuilder.setName(folder.getName().getBaseName()).setType(Opal.FileDto.FileType.FOLDER).setPath(folder.getName().getPath());

      // Create FileDtos for each file & folder in the folder corresponding to the path.
      addChildren(fileBuilder, opalRuntime.getFileSystem().getRoot().resolveFile(path));

      return Response.ok(fileBuilder.build()).build();
    }
  }

  private void addChildren(Opal.FileDto.Builder parentFolderBuilder, FileObject parentFolder) throws FileSystemException {
    Opal.FileDto.Builder fileBuilder;

    // Get the children for the current folder (list of files & folders).
    FileObject[] children = parentFolder.getChildren();

    // Loop through all children.
    for(int i = 0; i < children.length; i++) {

      // Build a FileDto representing the child.
      fileBuilder = Opal.FileDto.newBuilder();
      fileBuilder.setName(children[i].getName().getBaseName()).setPath(children[i].getName().getPath());
      fileBuilder.setType(children[i].getType() == FileType.FILE ? Opal.FileDto.FileType.FILE : Opal.FileDto.FileType.FOLDER);

      // Set size on files only, not folders.
      if(children[i].getType() == FileType.FILE) {
        fileBuilder.setSize(children[i].getContent().getSize());
      }

      fileBuilder.setLastModifiedTime(children[i].getContent().getLastModifiedTime());

      // Add the current child to the parent FileDto (folder).
      parentFolderBuilder.addChildren(fileBuilder.build());
    }
  }
}
