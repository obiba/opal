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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.obiba.core.util.StreamUtil;
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
  public Response getFileSystemEntry(@PathParam("path") String path) throws FileSystemException {

    FileObject file = resolveFileInFileSystem(path);
    if(!file.exists()) {
      return getPathNotExistResponse(path);
    } else if(file.getType() == FileType.FILE) {
      return getFile(file);
    } else {
      return getFolder(file);
    }

  }

  protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  private Response getFile(FileObject file) {
    return Response.ok(opalRuntime.getFileSystem().getLocalFile(file), MediaType.APPLICATION_OCTET_STREAM_TYPE).build();
  }

  private Response getFolder(FileObject folder) throws FileSystemException {

    // Create a FileDto representing the folder identified by the path.
    Opal.FileDto.Builder fileBuilder = Opal.FileDto.newBuilder();
    fileBuilder.setName(folder.getName().getBaseName()).setType(Opal.FileDto.FileType.FOLDER).setPath(folder.getName().getPath());

    // Create FileDtos for each file & folder in the folder corresponding to the path.
    addChildren(fileBuilder, folder);

    return Response.ok(fileBuilder.build()).build();

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

  @PUT
  // The POST method is required here to be compatible with Html forms which do not support the PUT method.
  @POST
  @Path("/{path:.*}")
  @Consumes("multipart/form-data")
  public Response uploadFile(@PathParam("path") String path, @Context HttpServletRequest request) throws FileSystemException, FileUploadException {

    FileObject fileToWriteTo = resolveFileInFileSystem(path);
    FileObject folderOfFileToWriteTo = fileToWriteTo.getParent();

    FileItem uploadedFile = getUploadedFile(request, fileToWriteTo);

    if(uploadedFile == null) {
      return Response.status(Status.BAD_REQUEST).entity("No file has been submitted. Please make sure that you are submitting a file with your resquest.").build();

      // A folder exist with that name at the specified path
    } else if(fileToWriteTo.exists() && fileToWriteTo.getType() == FileType.FOLDER) {
      return Response.status(Status.BAD_REQUEST).entity("Could not upload the file, a folder exist with that name at the specified path: " + path).build();

      // The parent folder does not exist (the specification says that we should not create folders)
    } else if(folderOfFileToWriteTo != null && !folderOfFileToWriteTo.exists()) {
      return getPathNotExistResponse(path);
    }

    writeUploadedFileToFileSystem(uploadedFile, fileToWriteTo);

    log.info("The following file was uploaded to Opal file system : {}", path);

    return Response.ok().build();

  }

  private Response getPathNotExistResponse(String path) {
    return Response.status(Status.NOT_FOUND).entity("The path specified does not exist: " + path).build();
  }

  @SuppressWarnings("unchecked")
  protected FileItem getUploadedFile(HttpServletRequest request, FileObject fileToWriteTo) throws FileUploadException {
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    for(FileItem fileItem : (List<FileItem>) upload.parseRequest(request)) {
      if(!fileItem.isFormField() && fileItem.getFieldName().equals("fileToUpload")) {
        return fileItem;
      }
    }

    return null;
  }

  private void writeUploadedFileToFileSystem(FileItem uploadedFile, FileObject fileToWriteTo) {
    OutputStream localFileStream = null;
    InputStream uploadedFileStream = null;
    try {
      localFileStream = fileToWriteTo.getContent().getOutputStream();
      uploadedFileStream = uploadedFile.getInputStream();
      StreamUtil.copy(uploadedFileStream, localFileStream);
    } catch(IOException couldNotWriteUploadedFile) {
      throw new RuntimeException("Could not write uploaded file to Opal file system", couldNotWriteUploadedFile);
    } finally {
      StreamUtil.silentSafeClose(localFileStream);
      StreamUtil.silentSafeClose(uploadedFileStream);
    }

  }
}
