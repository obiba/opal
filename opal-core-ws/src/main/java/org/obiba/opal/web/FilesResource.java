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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.vfs2.*;
import org.apache.shiro.SecurityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.core.util.StreamUtil;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.security.OpalPermissions;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.security.AuthorizationInterceptor;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Path("/files")
public class FilesResource {

  private static final Logger log = LoggerFactory.getLogger(FilesResource.class);

  private OpalRuntime opalRuntime;

  private SubjectAclService subjectAclService;

  private final MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

  private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

  @Autowired
  public void setOpalRuntime(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  @Autowired
  public void setSubjectAclService(SubjectAclService subjectAclService) {
    this.subjectAclService = subjectAclService;
  }

  @GET
  @Path("/_meta")
  @NoAuthorization
  public Response getFileSystemRootDetails() throws IOException {
    return getFileDetails("/");
  }

  @GET
  @Path("/_meta/{path:.*}")
  @NoAuthorization
  public Response getFileDetails(@PathParam("path") String path) throws IOException {
    FileObject file = resolveFileInFileSystem(path);
    return file.exists()
        ? file.getType() == FileType.FILE ? getFileDetails(file) : getFolderDetails(file)
        : getPathNotExistResponse("/" + path);
  }

  @GET
  @Path("/")
  @AuthenticatedByCookie
  public Response getFileSystemRoot(@HeaderParam("X-File-Key") String password) throws IOException {
    return getFile("/", null, password);
  }


  @POST
  @Path("/{path:.*}")
  @AuthenticatedByCookie
  public Response getFileFromForm(@PathParam("path") String path, @QueryParam("file") List<String> children,
                                  @Nullable @FormParam("key") String fileKey) throws IOException {
    return getFileInternal(path, children, fileKey);
  }

  @GET
  @Path("/{path:.*}")
  @AuthenticatedByCookie
  public Response getFile(@PathParam("path") String path, @QueryParam("file") List<String> children, @HeaderParam("X-File-Key") String fileKey)
      throws IOException {
    return getFileInternal(path, children, fileKey);
  }

  private Response getFileInternal(String path, List<String> children, String fileKey) throws IOException {
    if (!Strings.isNullOrEmpty(fileKey) && fileKey.length() < 8) {
      return Response.status(Status.BAD_REQUEST).entity("The file key is too short (minimum 8 characters).").build();
    }
    FileObject file = resolveFileInFileSystem(path);
    return file.exists()
        ? file.getType() == FileType.FILE ? getFile(file, fileKey) : getFolder(file, children, fileKey)
        : getPathNotExistResponse(path);
  }

  /**
   * Copy or move a file to the current folder.
   *
   * @param destinationPath
   * @param action          'copy' (default) or 'move'
   * @param sourcesPath
   * @return
   * @throws IOException
   */
  @PUT
  @Path("/{path:.*}")
  @AuthenticatedByCookie
  public Response updateFile(@PathParam("path") String destinationPath,
                             @QueryParam("action") @DefaultValue("copy") String action, @QueryParam("file") List<String> sourcesPath)
      throws IOException {

    // destination check
    FileObject destinationFile = resolveFileInFileSystem(destinationPath);
    if (!destinationFile.isWriteable())
      return Response.status(Status.FORBIDDEN).entity("Destination file is not writable: " + destinationPath).build();

    // sources check
    if (sourcesPath == null || sourcesPath.isEmpty())
      return Response.status(Status.BAD_REQUEST).entity("Source file is missing").build();

    // filter actions: copy, move
    if ("move".equals(action.toLowerCase())) return moveTo(destinationFile, sourcesPath);
    if ("copy".equals(action.toLowerCase())) return copyFrom(destinationFile, sourcesPath);

    return Response.status(Status.BAD_REQUEST).entity("Unexpected file action: " + action).build();
  }

  private Response moveTo(FileObject destinationFile, List<String> sourcesPath) throws IOException {
    if (sourcesPath.size() > 1) {
      // several files or folders can only be moved into an existing folder
      return moveToFolder(destinationFile, sourcesPath);
    }

    // one file or folder can be renamed and/or moved
    if (destinationFile.exists() && FileType.FOLDER.equals(destinationFile.getType()))
      return moveToFolder(destinationFile, sourcesPath);

    return renameTo(destinationFile, sourcesPath.get(0));
  }

  /**
   * Move source files and folders into an existing folder.
   *
   * @param destinationFolder
   * @param sourcesPath
   * @return
   * @throws IOException
   */
  private Response moveToFolder(FileObject destinationFolder, Iterable<String> sourcesPath) throws IOException {
    // destination check
    String destinationPath = destinationFolder.getName().getPath();
    if (!destinationFolder.exists()) return getPathNotExistResponse(destinationPath);
    if (destinationFolder.getType() != FileType.FOLDER)
      return Response.status(Status.BAD_REQUEST).entity("Destination must be a folder: " + destinationPath).build();

    // sources check
    for (String sourcePath : sourcesPath) {
      Response check = checkSourceFile(resolveFileInFileSystem(sourcePath));
      if (check != null) return check;
    }

    // do action
    for (String sourcePath : sourcesPath) {
      if (!sourcePath.equals(destinationPath)) {
        FileObject sourceFile = resolveFileInFileSystem(sourcePath);
        FileObject destinationFile = resolveFileInFileSystem(destinationPath + "/" + sourceFile.getName().getBaseName());
        sourceFile.moveTo(destinationFile);
      }
    }

    return Response.ok().build();
  }

  /**
   * Rename a file of folder. Can be moved if the parent folder is different.
   *
   * @param destinationFile
   * @param sourcePath
   * @return
   * @throws IOException
   */
  private Response renameTo(FileObject destinationFile, String sourcePath) throws IOException {
    // check source
    FileObject sourceFile = resolveFileInFileSystem(sourcePath);
    Response check = checkSourceFile(sourceFile);
    if (check != null) return check;

    String destinationPath = destinationFile.getName().getPath();
    if (sourceFile.getType() == FileType.FOLDER && destinationFile.getType() == FileType.FILE)
      return Response.status(Status.BAD_REQUEST).entity("Cannot rename a folder into an existing file: " + destinationPath).build();
    if (!destinationFile.getParent().isWriteable()) {
      return Response.status(Status.FORBIDDEN).entity("Source file cannot be moved: " + sourcePath).build();
    }

    // cannot rename to itself
    if (!destinationPath.equals(sourcePath)) sourceFile.moveTo(destinationFile);

    return Response.ok().build();
  }

  /**
   * Check that source file is readable and writeable.
   *
   * @param sourceFile
   * @return
   * @throws IOException
   */
  private Response checkSourceFile(FileObject sourceFile) throws IOException {
    String sourcePath = sourceFile.getName().getPath();
    if (!sourceFile.exists()) getPathNotExistResponse(sourcePath);
    if (!sourceFile.isReadable()) {
      return Response.status(Status.FORBIDDEN).entity("Source file is not readable: " + sourcePath).build();
    }
    if (!sourceFile.isWriteable()) {
      return Response.status(Status.FORBIDDEN).entity("Source file cannot be moved: " + sourcePath).build();
    }
    return null;
  }

  private Response copyFrom(FileObject destinationFolder, Iterable<String> sourcesPath) throws IOException {
    // destination check
    String destinationPath = destinationFolder.getName().getPath();
    if (!destinationFolder.exists()) return getPathNotExistResponse(destinationPath);
    if (destinationFolder.getType() != FileType.FOLDER)
      return Response.status(Status.BAD_REQUEST).entity("Destination must be a folder: " + destinationPath).build();

    // sources check
    for (String sourcePath : sourcesPath) {
      FileObject sourceFile = resolveFileInFileSystem(sourcePath);
      if (!sourceFile.exists()) getPathNotExistResponse(sourcePath);
      if (!sourceFile.isReadable()) {
        return Response.status(Status.FORBIDDEN).entity("Source file is not readable: " + sourcePath).build();
      }
    }

    // do action
    for (String sourcePath : sourcesPath) {
      if (!sourcePath.equals(destinationPath)) {
        FileObject sourceFile = resolveFileInFileSystem(sourcePath);
        FileObject destinationFile = resolveFileInFileSystem(destinationPath + "/" + sourceFile.getName().getBaseName());
        FileSelector selector = sourceFile.getType() == FileType.FOLDER ? Selectors.SELECT_ALL : Selectors.SELECT_SELF;
        destinationFile.copyFrom(sourceFile, selector);
      }
    }

    return Response.ok().build();
  }

  @POST
  @Path("/")
  @Consumes("multipart/form-data")
  @Produces("text/html")
  @AuthenticatedByCookie
  public Response uploadFile(@Context UriInfo uriInfo, @Context HttpServletRequest request)
      throws IOException, FileUploadException {
    return uploadFile("/", uriInfo, request);
  }

  // The POST method is required here to be compatible with Html forms which do not support the PUT method.
  @POST
  @Path("/{path:.*}")
  @Consumes("multipart/form-data")
  @Produces("text/html")
  @AuthenticatedByCookie
  public Response uploadFile(@PathParam("path") String path, @Context UriInfo uriInfo,
                             @Context HttpServletRequest request) throws IOException, FileUploadException {

    String folderPath = getPathOfFileToWrite(path);
    FileObject folder = resolveFileInFileSystem(folderPath);

    if (folder == null || !folder.exists()) {
      return getPathNotExistResponse(path);
    }
    if (folder.getType() != FileType.FOLDER) {
      return Response.status(Status.FORBIDDEN).entity("Not a folder: " + path).build();
    }

    List<FileItem> uploadedFiles = getUploadedFiles(request);
    if (uploadedFiles.isEmpty()) {
      return Response.status(Status.BAD_REQUEST)
          .entity("No file has been submitted. Please make sure that you are submitting a file with your request.")
          .build();
    }

    return doUploadFiles(folderPath, folder, uploadedFiles, uriInfo);
  }

  private Response doUploadFiles(String folderPath, FileObject folder, List<FileItem> uploadedFiles, UriInfo uriInfo)
      throws FileSystemException {

    for (FileItem uploadedFile : uploadedFiles) {
      String fileName = uploadedFile.getName();
      // #3275 make sure file name is valid
      if (Strings.isNullOrEmpty(fileName) || fileName.contains("/"))
        return Response.status(Status.BAD_REQUEST).entity("Not a valid file name.").build();

      // #3275 make sure parent folder of the written file is the provided destination folder
      FileObject file = folder.resolveFile(fileName);
      if (!file.getParent().getURL().equals(folder.getURL()))
        return Response.status(Status.BAD_REQUEST).entity("Not a valid file name.").build();

      boolean overwrite = file.exists();
      writeUploadedFileToFileSystem(uploadedFile, file);

      log.info("The following file was uploaded to Opal file system : {}", file.getURL());

      if (!overwrite) {
        URI fileUri = uriInfo.getBaseUriBuilder().path(FilesResource.class).path(folderPath).path(fileName).build();
        OpalPermissions perms = new OpalPermissions(fileUri, AclAction.FILES_ALL);
        subjectAclService.addSubjectPermission(perms.getDomain(), perms.getNode(),
            SubjectAcl.SubjectType.USER.subjectFor(SecurityUtils.getSubject().getPrincipal().toString()),
            AclAction.FILES_ALL.name());
      }
    }

    return Response.ok().build();
  }

  @POST
  @Path("/")
  @Consumes("text/plain")
  public Response createFolder(String folderName, @Context UriInfo uriInfo) throws IOException {
    return createFolder("/", folderName, uriInfo);
  }

  @POST
  @Path("/{path:.*}")
  @Consumes("text/plain")
  public Response createFolder(@PathParam("path") String path, String folderName, @Context UriInfo uriInfo)
      throws IOException {
    if (folderName == null || folderName.trim().isEmpty()) return Response.status(Status.BAD_REQUEST).build();

    String folderPath = getPathOfFileToWrite(path);
    FileObject folder = resolveFileInFileSystem(folderPath);
    Response folderResponse = validateFolder(folder, path);
    if (folderResponse != null) return folderResponse;

    FileObject file = folder.resolveFile(folderName);
    Response fileResponse = validateFile(file);
    if (fileResponse != null) return fileResponse;

    try {
      file.createFolder();
      Opal.FileDto dto = getBaseFolderBuilder(file).build();
      URI folderUri = uriInfo.getBaseUriBuilder().path(FilesResource.class).path(folderPath).path(folderName).build();
      return Response.created(folderUri)//
          .header(AuthorizationInterceptor.ALT_PERMISSIONS, new OpalPermissions(folderUri, AclAction.FILES_ALL))//
          .entity(dto).build();
    } catch (FileSystemException couldNotCreateTheFolder) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("cannotCreateFolderUnexpectedError").build();
    }
  }

  @Nullable
  private Response validateFolder(FileObject folder, String path) throws IOException {
    if (folder == null || !folder.exists()) {
      return getPathNotExistResponse(path);
    }
    if (folder.getType() != FileType.FOLDER) {
      return Response.status(Status.FORBIDDEN).entity("Not a folder: " + path).build();
    }
    return null;
  }

  @Nullable
  private Response validateFile(FileObject file) throws FileSystemException {
    // Folder or file already exist at specified path.
    if (file.exists()) {
      return Response.status(Status.FORBIDDEN).entity("cannotCreateFolderPathAlreadyExist").build();
    }

    // Parent folder is read-only.
    if (!file.getParent().isWriteable()) {
      return Response.status(Status.FORBIDDEN).entity("cannotCreateFolderParentIsReadOnly").build();
    }

    return null;
  }

  @DELETE
  @Path("/{path:.*}")
  public Response deleteFile(@PathParam("path") String path) throws IOException {
    FileObject file = resolveFileInFileSystem(path);

    // File or folder does not exist.
    if (!file.exists()) {
      return getPathNotExistResponse(path);
    }

    // Read-only file or folder.
    if (!file.isWriteable()) {
      return Response.status(Status.FORBIDDEN).entity("cannotDeleteReadOnlyFile").build();
    }

    try {
      if (file.getType() == FileType.FOLDER) {
        deleteFolder(file);
      } else {
        file.delete();
      }
      subjectAclService.deleteNodePermissions("/files/" + path);
      return Response.ok("The following file or folder has been deleted : " + path).build();
    } catch (FileSystemException couldNotDeleteFile) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("couldNotDeleteFileError").build();
    }
  }

  //
  // charsets
  //

  @GET
  @Cache
  @Path("/charsets/available")
  @NoAuthorization
  public Response getAvailableCharsets() {
    SortedMap<String, Charset> charsets = Charset.availableCharsets();
    List<String> names = new ArrayList<>();
    for (Charset charSet : charsets.values()) {
      names.add(charSet.name());
      names.addAll(charSet.aliases());
    }
    return Response.ok(new JSONArray(names).toString()).build();
  }

  //
  // private methods
  //

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  FileObject resolveFileInFileSystem(File localFile) throws FileSystemException {
    FileObject root = opalRuntime.getFileSystem().getRoot();
    File localRoot = opalRuntime.getFileSystem().getLocalFile(root);
    String path = localFile.getAbsolutePath().replace(localRoot.getAbsolutePath(), "");
    return root.resolveFile(path);
  }

  private Response getFile(FileObject file, String key) throws IOException {
    final File localFile = opalRuntime.getFileSystem().getLocalFile(file);
    String fileName = Strings.isNullOrEmpty(key) ? localFile.getName() : localFile.getName() + ".zip";
    String mimeType = mimeTypes.getContentType(fileName);

    StreamingOutput stream = os -> {
      File output = localFile;
      // if file key is provided, file is encrypted in a zip
      if (!Strings.isNullOrEmpty(key)) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "opal-" + dateTimeFormatter.format(System.currentTimeMillis()));
        tmpDir.mkdirs();
        output = org.obiba.core.util.FileUtil.zip(localFile, new File(tmpDir, localFile.getName() + ".zip"), key);
      }
      Files.copy(output.toPath(), os);
      if (!Strings.isNullOrEmpty(key)) {
        output.getParentFile().delete();
      }
    };

    return Response.ok(stream, mimeType)
        .header("Content-Disposition", getContentDispositionOfAttachment(fileName)).build();
  }

  private Response getFolder(FileObject folder, Collection<String> children, String key) throws IOException {
    final File localFolder = opalRuntime.getFileSystem().getLocalFile(folder);
    final String fileName = localFolder.getName() + ".zip";
    String mimeType = mimeTypes.getContentType(fileName);

    StreamingOutput stream = os -> {
      File tmpDir = new File(System.getProperty("java.io.tmpdir"), "opal-" + dateTimeFormatter.format(System.currentTimeMillis()));
      tmpDir.mkdirs();
      File output = org.obiba.core.util.FileUtil.zip(localFolder, pathname -> {
        // check read access
        try {
          FileObject fileObject = resolveFileInFileSystem(pathname);
          if (!fileObject.isReadable()) return false;
        } catch (FileSystemException e) {
          return false;
        }
        // check first level filter
        if (children == null || children.isEmpty()) return true;
        if (pathname.getParentFile().equals(localFolder)) {
          return children.contains(pathname.getName());
        }
        // anything else is ok
        return true;
      }, new File(tmpDir, fileName), key);
      Files.copy(output.toPath(), os);
      tmpDir.delete();
    };

    return Response.ok(stream, mimeType)
        .header("Content-Disposition", getContentDispositionOfAttachment(fileName)).build();
  }

  private Response getFileDetails(FileObject file) throws FileSystemException {
    Opal.FileDto.Builder fileBuilder;

    fileBuilder = Opal.FileDto.newBuilder();
    fileBuilder.setName(file.getName().getBaseName()).setPath(file.getName().getPath());
    fileBuilder.setType(file.getType() == FileType.FILE ? Opal.FileDto.FileType.FILE : Opal.FileDto.FileType.FOLDER);
    fileBuilder.setReadable(file.isReadable()).setWritable(file.isWriteable());

    // Set size on files only, not folders.
    if (file.getType() == FileType.FILE) {
      fileBuilder.setSize(file.getContent().getSize());
    }

    fileBuilder.setLastModifiedTime(file.getContent().getLastModifiedTime());

    return Response.ok(fileBuilder.build()).build();
  }

  private Response getFolderDetails(FileObject folder) throws FileSystemException {
    // Create a FileDto representing the folder identified by the path.
    Opal.FileDto.Builder folderBuilder = getBaseFolderBuilder(folder);

    // Create FileDtos for each file & folder in the folder corresponding to the path.
    if (folder.isReadable()) {
      addChildren(folderBuilder, folder, 2);
    }

    return Response.ok(folderBuilder.build()).build();

  }

  private Opal.FileDto.Builder getBaseFolderBuilder(FileObject folder) throws FileSystemException {
    Opal.FileDto.Builder fileBuilder = Opal.FileDto.newBuilder();
    String folderName = folder.getName().getBaseName();
    fileBuilder.setName("".equals(folderName) ? "root" : folderName).setType(Opal.FileDto.FileType.FOLDER)
        .setPath(folder.getName().getPath());
    fileBuilder.setLastModifiedTime(folder.getContent().getLastModifiedTime());
    fileBuilder.setReadable(folder.isReadable()).setWritable(folder.isWriteable());
    return fileBuilder;
  }

  private void addChildren(Opal.FileDto.Builder folderBuilder, FileObject parentFolder, int level)
      throws FileSystemException {
    Opal.FileDto.Builder fileBuilder;

    // Get the children for the current folder (list of files & folders).
    List<FileObject> children = Arrays.asList(parentFolder.getChildren());

    Collections.sort(children, (arg0, arg1) -> arg0.getName().compareTo(arg1.getName()));

    // Loop through all children.
    for (FileObject child : children) {
      // Build a FileDto representing the child.
      fileBuilder = Opal.FileDto.newBuilder();
      fileBuilder.setName(child.getName().getBaseName()).setPath(child.getName().getPath());
      fileBuilder.setType(child.getType() == FileType.FILE ? Opal.FileDto.FileType.FILE : Opal.FileDto.FileType.FOLDER);
      fileBuilder.setReadable(child.isReadable()).setWritable(child.isWriteable());

      // Set size on files only, not folders.
      if (child.getType() == FileType.FILE) {
        fileBuilder.setSize(child.getContent().getSize());
      }

      fileBuilder.setLastModifiedTime(child.getContent().getLastModifiedTime());

      if (child.getType().hasChildren() && child.getChildren().length > 0 && level - 1 > 0 && child.isReadable()) {
        addChildren(fileBuilder, child, level - 1);
      }

      // Add the current child to the parent FileDto (folder).
      folderBuilder.addChildren(fileBuilder.build());
    }
  }

  private String getPathOfFileToWrite(String path) {
    return path.startsWith("/tmp/") ? "/tmp/" + UUID.randomUUID() + ".tmp" : path;
  }

  private Response getPathNotExistResponse(String path) throws NoSuchFileException {
    throw new NoSuchFileException(path);
  }

  /**
   * Returns the first {@code FileItem} that is represents a file upload field. If no such field exists, this method
   * returns null
   *
   * @param request
   * @return
   * @throws FileUploadException
   */
  List<FileItem> getUploadedFiles(HttpServletRequest request) throws FileUploadException {
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload(factory);
    List<FileItem> files = Lists.newArrayList();
    for (FileItem fileItem : upload.parseRequest(request)) {
      if (!fileItem.isFormField()) {
        files.add(fileItem);
      }
    }
    return files;
  }

  private void writeUploadedFileToFileSystem(FileItem uploadedFile, FileObject fileToWriteTo) {

    // OPAL-919: We need to wrap the OutputStream returned by commons-vfs into another OutputStream
    // to force a call to flush() on every call to write() in order to prevent the system from running out of memory
    // when copying large files.
    try (OutputStream localFileStream = new BufferedOutputStream(fileToWriteTo.getContent().getOutputStream()) {
      @Override
      public synchronized void write(byte[] b, int off, int len) throws IOException {
        flush();
        super.write(b, off, len);
      }

    };
         InputStream uploadedFileStream = uploadedFile.getInputStream()) {

      StreamUtil.copy(uploadedFileStream, localFileStream);
    } catch (IOException couldNotWriteUploadedFile) {
      throw new RuntimeException("Could not write uploaded file to Opal file system", couldNotWriteUploadedFile);
    }
  }

  /**
   * Delete writable folder and sub-folders.
   *
   * @param folder
   * @throws FileSystemException
   */
  private void deleteFolder(FileObject folder) throws FileSystemException {
    if (!folder.isWriteable()) return;

    FileObject[] files = folder.getChildren();
    for (FileObject file : files) {
      if (file.getType() == FileType.FOLDER) {
        deleteFolder(file);
      } else if (file.isWriteable()) {
        file.delete();
      }
    }
    if (folder.getChildren().length == 0) {
      folder.delete();
    }
  }

  private String getContentDispositionOfAttachment(String fileName) {
    return "attachment; filename=\"" + fileName + "\"";
  }

}
