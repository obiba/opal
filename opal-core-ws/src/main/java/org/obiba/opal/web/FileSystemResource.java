/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
@Path("/filesystem")
// TODO We should delete this once the new FileSelection dialog has been integrated in the DataImport UI.
public class FileSystemResource {

  private OpalFileSystemService opalFileSystemService;

  @Autowired
  public void setOpalFileSystemService(OpalFileSystemService opalFileSystemService) {
    this.opalFileSystemService = opalFileSystemService;
  }

  @GET
  public Opal.FileDto getFileSystem() throws FileSystemException {
    FileObject root = opalFileSystemService.getFileSystem().getRoot();

    // Create a root FileDto representing the root of the FileSystem.
    Opal.FileDto.Builder fileBuilder = Opal.FileDto.newBuilder();
    fileBuilder.setName("root").setType(Opal.FileDto.FileType.FOLDER).setPath("/");
    fileBuilder.setReadable(root.isReadable()).setWritable(root.isWriteable());

    // Create FileDtos for each file & folder in the FileSystem and add them to the root FileDto recursively.
    addFiles(fileBuilder, root);

    return fileBuilder.build();
  }

  private void addFiles(Opal.FileDto.Builder parentFolderBuilder, FileObject parentFolder) throws FileSystemException {
    Opal.FileDto.Builder fileBuilder;

    // Get the children for the current folder (list of files & folders).
    List<FileObject> children = Arrays.asList(parentFolder.getChildren());

    Collections.sort(children, Comparator.comparing(FileObject::getName));

    // Loop through all children.
    for(FileObject child : children) {

      // Build a FileDto representing the child.
      fileBuilder = Opal.FileDto.newBuilder();
      fileBuilder.setName(child.getName().getBaseName()).setPath(child.getName().getPath());
      fileBuilder.setType(child.getType() == FileType.FILE ? Opal.FileDto.FileType.FILE : Opal.FileDto.FileType.FOLDER);
      fileBuilder.setReadable(child.isReadable()).setWritable(child.isWriteable());

      // If the current child is a folder, add its children recursively.
      if(child.getType() == FileType.FOLDER && child.isReadable()) {
        addFiles(fileBuilder, child);
      }

      // Add the current child to the parent FileDto (folder).
      parentFolderBuilder.addChildren(fileBuilder.build());
    }
  }
}
