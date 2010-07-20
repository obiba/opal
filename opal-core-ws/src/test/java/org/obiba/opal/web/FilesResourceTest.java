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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.OpalFileSystemImpl;
import org.obiba.opal.web.model.Opal.FileDto;

public class FilesResourceTest {

  private OpalRuntime opalRuntimeMock;

  private OpalFileSystem fileSystem;

  private FilesResource filesResource;

  private FileObject fileObjectMock;

  private FileItem fileItemMock;

  private UriInfo uriInfoMock;

  /**
   * Delete these files in tearDown().
   */
  private List<String> filesCreatedByTest = new ArrayList<String>();

  @Before
  public void setUp() throws URISyntaxException, FileSystemException {
    opalRuntimeMock = createMock(OpalRuntime.class);

    String rootDir = getClass().getResource("/test-file-system").toURI().toString();
    fileSystem = new OpalFileSystemImpl(rootDir);
    filesResource = new FilesResource(opalRuntimeMock);

    fileItemMock = createMock(FileItem.class);
    fileObjectMock = createMock(FileObject.class);

    uriInfoMock = createMock(UriInfo.class);
  }

  @After
  public void tearDown() throws FileSystemException {
    // Delete any files created by the test.
    for(String filePath : filesCreatedByTest) {
      FileObject file = fileSystem.getRoot().resolveFile(filePath);
      if(file.exists()) {
        file.delete();
      }
    }
  }

  @Test
  public void testGetFileSystem() throws FileSystemException {

    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    FileSystemResource filesResource = new FileSystemResource(opalRuntimeMock);
    FileDto rootFileDto = filesResource.getFileSystem();

    Assert.assertEquals("root", rootFileDto.getName());
    Assert.assertEquals(FileDto.FileType.FOLDER, rootFileDto.getType());
    Assert.assertEquals("/", rootFileDto.getPath());

  }

  @Test
  public void verifyThatAllFilesAndFoldersInDtoStructureExistInFileSystem() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    FileSystemResource filesResource = new FileSystemResource(opalRuntimeMock);
    FileDto rootFileDto = filesResource.getFileSystem();

    int childrenCounter = 0;
    childrenCounter = verifyThatChildrenExistInFileSystem(rootFileDto, childrenCounter);

    // File count in Dto structure should be the same as file count in file system.
    Assert.assertEquals(20, childrenCounter);

    verify(opalRuntimeMock);
  }

  private int verifyThatChildrenExistInFileSystem(FileDto folder, int childrenCounter) throws FileSystemException {
    FileObject correspondingFileObj;
    for(FileDto child : folder.getChildrenList()) {
      childrenCounter++;
      correspondingFileObj = fileSystem.getRoot().resolveFile(child.getPath());
      Assert.assertTrue(correspondingFileObj.exists());
      if(child.getType() == FileDto.FileType.FOLDER) {
        childrenCounter = verifyThatChildrenExistInFileSystem(child, childrenCounter);
      }
    }
    return childrenCounter;
  }

  @Test
  public void testGetFoldersDetailsInFileSystem() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).atLeastOnce();

    replay(opalRuntimeMock);

    checkGetFileDetailsResponse("/", new String[] { "folder1", "folder2", "folder3", "folder4", "folder5", "file2.txt", "folder11", "file11.txt", "file21.txt", "folder31", "folder41", "file41.txt", "file42.txt", "file43.txt", "file51.txt" });
    checkGetFileDetailsResponse("/folder1/folder11", new String[] { "folder111", "file111.txt", "file1111.txt", "file1112.txt" });
    checkGetFileDetailsResponse("/folder1/folder11/folder111", new String[] { "file1111.txt", "file1112.txt" });
    checkGetFileDetailsResponse("/folder2", new String[] { "file21.txt" });
    checkGetFileDetailsResponse("/folder3", new String[] { "folder31", "file311.txt" });
    checkGetFileDetailsResponse("/folder4", new String[] { "folder41", "file41.txt", "file42.txt", "file43.txt" });
    checkGetFileDetailsResponse("/folder5", new String[] { "file51.txt" });

    verify(opalRuntimeMock);

  }

  private void checkGetFileDetailsResponse(String path, String[] expectedFolderContentArray) throws FileSystemException {

    Set<String> expectedFolderContent = new HashSet<String>(Arrays.asList(expectedFolderContentArray));
    Response response = filesResource.getFileDetails(path);

    // Make sure response is OK
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    FileDto folder = (FileDto) response.getEntity();
    List<FileDto> folderContent = folder.getChildrenList();

    // Check folder content recursively two levels down.
    checkFolderContent(expectedFolderContent, folderContent, 2);

    Assert.assertTrue(expectedFolderContent.isEmpty());
  }

  private void checkFolderContent(Set<String> expectedFolderContent, List<FileDto> folderContent, int level) {
    // Make sure folder content is as expected.
    for(FileDto oneFileOrFolder : folderContent) {
      Assert.assertTrue(expectedFolderContent.contains(oneFileOrFolder.getName()));
      expectedFolderContent.remove(oneFileOrFolder.getName());
      if(level > 0 && oneFileOrFolder.getChildrenCount() > 0) {
        checkFolderContent(expectedFolderContent, oneFileOrFolder.getChildrenList(), level - 1);
      }

    }
  }

  @Test
  public void testGetFile_GetCompressedFolderFromFileSystem() throws IOException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).atLeastOnce();
    replay(opalRuntimeMock);

    checkCompressedFolder("/folder1", new String[] { "folder1", "folder1/folder11", "folder1/file11.txt", "folder1/folder11/folder111", "folder1/folder11/file111.txt", "folder1/folder11/folder111/file1111.txt", "folder1/folder11/folder111/file1112.txt" });
    checkCompressedFolder("/folder2", new String[] { "folder2", "folder2/file21.txt" });
    checkCompressedFolder("/folder3", new String[] { "folder3", "folder3/folder31", "folder3/folder31/file311.txt" });
    checkCompressedFolder("/folder4", new String[] { "folder4", "folder4/folder41", "folder4/file41.txt", "folder4/file42.txt", "folder4/file43.txt" });
    checkCompressedFolder("/folder5", new String[] { "folder5", "folder5/file51.txt" });
    checkCompressedFolder("/", new String[] { "/", "folder1", "folder1/folder11", "folder1/file11.txt", "folder1/folder11/folder111", "folder1/folder11/file111.txt", "folder1/folder11/folder111/file1111.txt", "folder1/folder11/folder111/file1112.txt", "folder2", "folder2/file21.txt", "folder3", "folder3/folder31", "folder3/folder31/file311.txt", "folder4", "folder4/folder41", "folder4/file41.txt", "folder4/file42.txt", "folder4/file43.txt", "folder5", "folder5/file51.txt", "file2.txt" });

    verify(opalRuntimeMock);

  }

  private void checkCompressedFolder(String folderPath, String[] expectedFolderContentArray) throws IOException {
    Response response = filesResource.getFile(folderPath);
    ZipFile zipfile = new ZipFile(((File) response.getEntity()).getPath());

    // Check that all folders and files exist in the compressed archive that represents the folder.
    ZipEntry zipEntry;
    for(int i = 0; i < expectedFolderContentArray.length; i++) {
      zipEntry = zipfile.getEntry(expectedFolderContentArray[i]);
      Assert.assertNotNull(zipEntry);
    }

    Enumeration<ZipEntry> zipEnum = (Enumeration<ZipEntry>) zipfile.entries();
    int count = 0;

    while(zipEnum.hasMoreElements()) {
      zipEnum.nextElement();
      count++;
    }

    // Make sure that they are no unexpected files in the compressed archive.
    Assert.assertEquals(expectedFolderContentArray.length, count);

    zipfile.close();
  }

  public void testGetFilesInFileSystem() throws IOException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).atLeastOnce();

    replay(opalRuntimeMock);

    checkGetFileDetailsResponse("/file2.txt", "testing file2.txt content");
    checkGetFileDetailsResponse("/folder1/folder11/folder111/file1112.txt", "testing file1112.txt content");
    checkGetFileDetailsResponse("/folder4/file41.txt", "testing file41.txt content");

    verify(opalRuntimeMock);

  }

  private void checkGetFileDetailsResponse(String path, String expectedFileContent) throws IOException {
    Response response = filesResource.getFile(path);

    // Make sure response is OK
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    File file = (File) response.getEntity();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String textFirstLine = reader.readLine();

    // Make sure that the downloaded file content is the one expected.
    Assert.assertEquals(expectedFileContent, textFirstLine);

  }

  @Test
  public void testGetPathThatDoesNotExist() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    Response response = filesResource.getFileDetails("/folder1/folder2");
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);

  }

  @Test
  public void testUploadFileToFileSystem() throws FileUploadException, IOException, URISyntaxException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();
    expect(fileItemMock.getInputStream()).andReturn(getClass().getResourceAsStream("/files-to-upload/fileToUpload.txt")).once();
    expect(uriInfoMock.getAbsolutePath()).andReturn(new URI("test")).once();

    FilesResource fileResource = new FilesResource(opalRuntimeMock) {
      @Override
      protected FileItem getUploadedFile(HttpServletRequest request, FileObject fileToWriteTo) throws FileUploadException {
        return fileItemMock;
      }
    };

    replay(opalRuntimeMock, fileItemMock, uriInfoMock);

    // Upload the file.
    String uploadedFilePath = "/folder1/folder11/folder111/uploadedFile.txt";
    Response response = fileResource.uploadFile(uploadedFilePath, uriInfoMock, null);
    filesCreatedByTest.add(uploadedFilePath);

    // Verify that the service response is OK.
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    // Verify that the file was uploaded at the right path in the file system.
    Assert.assertTrue(fileSystem.getRoot().resolveFile("/folder1/folder11/folder111/uploadedFile.txt").exists());

    verify(opalRuntimeMock, fileItemMock, uriInfoMock);

  }

  @Test
  public void testUploadFileNoContentSubmitted() throws FileSystemException, FileUploadException, URISyntaxException {

    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    FilesResource fileResource = new FilesResource(opalRuntimeMock) {
      @Override
      protected FileItem getUploadedFile(HttpServletRequest request, FileObject fileToWriteTo) throws FileUploadException {
        return null;
      }
    };

    Response response = fileResource.uploadFile("/", uriInfoMock, null);
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);

  }

  @Test
  public void testUploadFileWhenFolderExistWithThatNameAtSpecifiedPath() throws FileSystemException, FileUploadException, URISyntaxException {

    expect(fileObjectMock.getType()).andReturn(FileType.FOLDER).once();
    expect(fileObjectMock.exists()).andReturn(true).once();
    expect(fileObjectMock.getParent()).andReturn(fileObjectMock);

    replay(opalRuntimeMock, fileObjectMock);

    FilesResource fileResource = new FilesResource(opalRuntimeMock) {
      @Override
      protected FileItem getUploadedFile(HttpServletRequest request, FileObject fileToWriteTo) throws FileUploadException {
        return fileItemMock;
      }

      @Override
      protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
        return fileObjectMock;
      }
    };

    Response response = fileResource.uploadFile("/", uriInfoMock, null);
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock, fileObjectMock);
  }

  @Test
  public void testUploadFile_ReturnsNotFoundResponseWhenUploadDestinationDoesNotExist() throws FileSystemException, FileUploadException, URISyntaxException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();

    FileObject parentFolderMock = createMock(FileObject.class);
    expect(parentFolderMock.exists()).andReturn(false).atLeastOnce();

    expect(fileObjectMock.getParent()).andReturn(parentFolderMock).atLeastOnce();

    replay(opalRuntimeMock, fileObjectMock, parentFolderMock);

    FilesResource fileResource = new FilesResource(opalRuntimeMock) {
      @Override
      protected FileItem getUploadedFile(HttpServletRequest request, FileObject fileToWriteTo) throws FileUploadException {
        return fileItemMock;
      }

      @Override
      protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
        return fileObjectMock;
      }
    };

    Response response = fileResource.uploadFile("/invalid/path/fileToUpload.txt", uriInfoMock, null);
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock, fileObjectMock);
  }

  @Test
  public void testDeleteFile_FileDoesNotExist() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    Response response = filesResource.deleteFile("/folder1/folder2/filethatdoesnotexist.txt");
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);
  }

  @Test
  public void testDeleteFile_CannotDeleteFolderWithContent() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    Response response = filesResource.deleteFile("/folder1");
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("cannotDeleteNotEmptyFolder", response.getEntity());

    verify(opalRuntimeMock);

  }

  @Test
  public void testDeleteFile_CannotDeleteReadOnlyFile() throws FileSystemException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.isWriteable()).andReturn(false).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().deleteFile("path");
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("cannotDeleteReadOnlyFile", response.getEntity());

    verify(fileObjectMock);

  }

  @Test
  public void testDeleteFile_FileDeletedSuccessfully() throws FileSystemException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.delete()).andReturn(true).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().deleteFile("path");
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    verify(fileObjectMock);

  }

  @Test
  public void testDeleteFile_CouldNotDeleteFile() throws FileSystemException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.delete()).andThrow(new FileSystemException("test")).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().deleteFile("path");
    Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    Assert.assertEquals("couldNotDeleteFileError", response.getEntity());

    verify(fileObjectMock);
  }

  private FilesResource getFileResource() {
    FilesResource filesResource = new FilesResource(opalRuntimeMock) {
      @Override
      protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
        return fileObjectMock;
      }
    };
    return filesResource;
  }

  @Test
  public void testCreateFolder_CannotCreateFolderPathAlreadyExist() throws FileSystemException, URISyntaxException {
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().createFolder("path", uriInfoMock);
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("cannotCreateFolderPathAlreadyExist", response.getEntity());

    verify(fileObjectMock);
  }

  @Test
  public void testCreateFolder_CannotCreateFolderParentIsReadOnly() throws FileSystemException, URISyntaxException {
    expect(fileObjectMock.exists()).andReturn(false).atLeastOnce();
    FileObject parentFolderMock = createMock(FileObject.class);
    expect(fileObjectMock.getParent()).andReturn(parentFolderMock).atLeastOnce();
    expect(parentFolderMock.isWriteable()).andReturn(false).atLeastOnce();

    replay(fileObjectMock, parentFolderMock);

    Response response = getFileResource().createFolder("path", uriInfoMock);
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("cannotCreateFolderParentIsReadOnly", response.getEntity());

    verify(fileObjectMock, parentFolderMock);
  }

  @Test
  public void testCreateFolder_CannotCreateFolderUnexpectedError() throws FileSystemException, URISyntaxException {
    expect(fileObjectMock.exists()).andReturn(false).atLeastOnce();
    FileObject parentFolderMock = createMock(FileObject.class);
    expect(fileObjectMock.getParent()).andReturn(parentFolderMock).atLeastOnce();
    expect(parentFolderMock.isWriteable()).andReturn(true).atLeastOnce();
    fileObjectMock.createFolder();
    EasyMock.expectLastCall().andThrow(new FileSystemException("test")).atLeastOnce();

    replay(fileObjectMock, parentFolderMock);

    Response response = getFileResource().createFolder("path", uriInfoMock);
    Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

    verify(fileObjectMock, fileObjectMock);
  }

  @Test
  public void testCreateFolder_FolderCreatedSuccessfully() throws FileSystemException, URISyntaxException {
    expect(fileObjectMock.exists()).andReturn(false).atLeastOnce();
    FileObject parentFolderMock = createMock(FileObject.class);
    expect(fileObjectMock.getParent()).andReturn(parentFolderMock).atLeastOnce();
    expect(parentFolderMock.isWriteable()).andReturn(true).atLeastOnce();
    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getAbsolutePath()).andReturn(new URI("path"));
    fileObjectMock.createFolder();

    replay(fileObjectMock, uriInfoMock, parentFolderMock);

    Response response = getFileResource().createFolder("path", uriInfoMock);
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    verify(fileObjectMock, uriInfoMock, fileObjectMock);
  }
}
