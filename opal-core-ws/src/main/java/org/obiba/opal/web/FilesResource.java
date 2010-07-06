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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
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
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/files")
public class FilesResource {

  private static final Logger log = LoggerFactory.getLogger(FilesResource.class);

  private static final int NOT_IMPLEMENTED = 501;

  private OpalRuntime opalRuntime;

  @Autowired
  public FilesResource(OpalRuntime opalRuntime) {
    super();
    this.opalRuntime = opalRuntime;
  }

  @GET
  @Path("/meta")
  public Response getFileSystemRootDetails() throws FileSystemException {
    return getFileDetails("/");
  }

  @GET
  @Path("/meta/{path:.*}")
  public Response getFileDetails(@PathParam("path") String path) throws FileSystemException {
    FileObject file = resolveFileInFileSystem(path);
    if(!file.exists()) {
      return getPathNotExistResponse(path);
    } else if(file.getType() == FileType.FILE) {
      return Response.status(NOT_IMPLEMENTED).entity("Details are only available for folder content.  The specified path points to a file : " + path).build();
    } else {
      return getFolderDetails(file);
    }
  }

  @GET
  @Path("/")
  public Response getFileSystemRoot() throws IOException {
    return getFile("/");
  }

  @GET
  @Path("/{path:.*}")
  @AuthenticatedByCookie
  public Response getFile(@PathParam("path") String path) throws IOException {
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

  private Response getFolder(FileObject folder) throws IOException {
    FileObject[] files = folder.getChildren();
    if(files.length == 0) {
      return Response.status(Status.NO_CONTENT).entity("Cannot download the following folder content, because the folder contains no file : " + folder.getName().getPath()).build();
    }

    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String folderName = folder.getName().getBaseName();
    File compressedFolder = new File(System.getProperty("java.io.tmpdir"), (folderName.equals("") ? "filesystem" : folderName) + "_" + dateTimeFormatter.format(System.currentTimeMillis()) + ".zip");
    compressedFolder.deleteOnExit();

    compressFolder(compressedFolder, folder);

    return Response.ok(compressedFolder, MediaType.APPLICATION_OCTET_STREAM_TYPE).header("Content-Disposition", "attachment; filename=" + compressedFolder.getName()).build();
  }

  private Response getFolderDetails(FileObject folder) throws FileSystemException {

    // Create a FileDto representing the folder identified by the path.
    Opal.FileDto.Builder fileBuilder = getBaseFolderBuilder(folder, false);

    // Add the parent folder as child of the current folder to allow going back to parent folder when navigating
    // the file system.
    FileObject parentFolder = folder.getParent();
    if(parentFolder != null) {
      Opal.FileDto.Builder parentFolderBuilder = getBaseFolderBuilder(parentFolder, true);
      fileBuilder.addChildren(parentFolderBuilder.build());
    }

    // Create FileDtos for each file & folder in the folder corresponding to the path.
    addChildren(fileBuilder, folder);

    return Response.ok(fileBuilder.build()).build();

  }

  private Opal.FileDto.Builder getBaseFolderBuilder(FileObject folder, boolean symbolicLink) throws FileSystemException {
    Opal.FileDto.Builder fileBuilder = Opal.FileDto.newBuilder();
    String folderName = folder.getName().getBaseName();
    fileBuilder.setName(folderName.equals("") ? "root" : folderName).setType(Opal.FileDto.FileType.FOLDER).setPath(folder.getName().getPath());
    fileBuilder.setSymbolicLink(symbolicLink);
    fileBuilder.setLastModifiedTime(folder.getContent().getLastModifiedTime());
    return fileBuilder;
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

  // TODO Refactoring required to merge uploadFile() with createFolder()
  // @PUT
  // The POST method is required here to be compatible with Html forms which do not support the PUT method.
  @POST
  @Path("/{path:.*}")
  @Consumes("multipart/form-data")
  @Produces("text/html")
  @AuthenticatedByCookie
  public Response uploadFile(@PathParam("path") String path, @Context UriInfo uriInfo, @Context HttpServletRequest request) throws FileSystemException, FileUploadException {

    FileObject fileToWriteTo = resolveFileInFileSystem(path);
    FileObject folderOfFileToWriteTo = fileToWriteTo.getParent();

    FileItem uploadedFile = getUploadedFile(request, fileToWriteTo);

    if(uploadedFile == null) {
      return Response.status(Status.BAD_REQUEST).entity("No file has been submitted. Please make sure that you are submitting a file with your resquest.").build();

      // A folder exist with that name at the specified path
    } else if(fileToWriteTo.exists() && fileToWriteTo.getType() == FileType.FOLDER) {
      return Response.status(Status.FORBIDDEN).entity("Could not upload the file, a folder exist with that name at the specified path: " + path).build();

      // The parent folder does not exist (the specification says that we should not create folders)
    } else if(folderOfFileToWriteTo != null && !folderOfFileToWriteTo.exists()) {
      return getPathNotExistResponse(path);
    }

    writeUploadedFileToFileSystem(uploadedFile, fileToWriteTo);

    log.info("The following file was uploaded to Opal file system : {}", path);

    return Response.created(uriInfo.getAbsolutePath()).entity(path + " created").build();

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

  @DELETE
  @Path("/{path:.*}")
  public Response deleteFile(@PathParam("path") String path) throws FileSystemException {
    FileObject file = resolveFileInFileSystem(path);

    // File or folder does not exist.
    if(!file.exists()) {
      return getPathNotExistResponse(path);
    }

    // The path refers to a folder that contains one or many files.
    if(file.getType() == FileType.FOLDER && file.getChildren().length > 0) {
      return Response.status(Status.FORBIDDEN).entity("This folder contains one or many file(s) and as a result cannot be deleted: " + path).build();
    }

    // Read-only file or folder.
    if(!file.isWriteable()) {
      return Response.status(Status.FORBIDDEN).entity("Could delete the following file or folder because it is read-only: " + path).build();
    }

    try {
      file.delete();
      return Response.ok("The following file or folder has been deleted : " + path).build();
    } catch(FileSystemException couldNotDeleteFile) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("There was an error while deleting the following file or folder: " + path).build();
    }
  }

  @PUT
  @Path("/{path:.*}")
  public Response createFolder(@PathParam("path") String path, @Context UriInfo uriInfo) throws FileSystemException {
    FileObject file = resolveFileInFileSystem(path);

    // Folder or file already exist at specified path.
    if(file.exists()) {
      return Response.status(Status.FORBIDDEN).entity("Cannot create the folder, a folder or a file exist with that name at the specified path: " + path).build();
    }

    // Parent folder is read-only.
    if(!file.getParent().isWriteable()) {
      return Response.status(Status.FORBIDDEN).entity("Could create the following folder because the parent folder is read-only: " + path).build();
    }

    try {
      file.createFolder();
      return Response.created(uriInfo.getAbsolutePath()).entity("Created the following folder: " + path).build();
    } catch(FileSystemException couldNotCreateTheFolder) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("There was an error while creating the following folder: " + path).build();
    }
  }

  private void compressFolder(File compressedFile, FileObject folder) throws IOException {
    ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(compressedFile));
    addFolder(folder, outputStream);
    outputStream.close();
  }

  private void addFolder(FileObject folder, ZipOutputStream outputStream) throws IOException {
    FileObject[] files = folder.getChildren();

    for(int i = 0; i < files.length; i++) {
      if(files[i].getType() == FileType.FOLDER) {
        addFolder(files[i], outputStream);
        continue;
      }

      outputStream.putNextEntry(new ZipEntry(files[i].getName().getPath().substring(1)));

      FileInputStream inputStream = new FileInputStream(opalRuntime.getFileSystem().getLocalFile(files[i]));
      StreamUtil.copy(inputStream, outputStream);

      outputStream.closeEntry();
      StreamUtil.silentSafeClose(inputStream);

    }
  }
}
