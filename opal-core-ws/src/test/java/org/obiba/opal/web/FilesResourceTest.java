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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import junit.framework.Assert;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
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

  @Before
  public void setUp() throws URISyntaxException, FileSystemException {
    opalRuntimeMock = createMock(OpalRuntime.class);

    String rootDir = getClass().getResource("/test-file-system").toURI().toString();
    fileSystem = new OpalFileSystemImpl(rootDir);
    filesResource = new FilesResource(opalRuntimeMock);

    fileItemMock = createMock(FileItem.class);
    fileObjectMock = createMock(FileObject.class);
  }

  @Test
  public void testGetFileSystem() throws FileSystemException {

    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    FilesResource filesResource = new FilesResource(opalRuntimeMock);
    FileDto rootFileDto = filesResource.getFileSystem();

    Assert.assertEquals("root", rootFileDto.getName());
    Assert.assertEquals(FileDto.FileType.FOLDER, rootFileDto.getType());
    Assert.assertEquals("/", rootFileDto.getPath());

  }

  @Test
  public void verifyThatAllFilesAndFoldersInDtoStructureExistInFileSystem() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    FilesResource filesResource = new FilesResource(opalRuntimeMock);
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
  public void testGetFoldersInFileSystem() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).atLeastOnce();

    replay(opalRuntimeMock);

    checkGetFolderResponse("/", new String[] { "folder1", "folder2", "folder3", "folder4", "folder5", "file2.txt" });
    checkGetFolderResponse("/folder1/folder11", new String[] { "folder111", "file111.txt" });
    checkGetFolderResponse("/folder1/folder11/folder111", new String[] { "file1111.txt", "file1112.txt" });
    checkGetFolderResponse("/folder2", new String[] { "file21.txt" });
    checkGetFolderResponse("/folder3", new String[] { "folder31" });
    checkGetFolderResponse("/folder4", new String[] { "folder41", "file41.txt", "file42.txt", "file43.txt" });
    checkGetFolderResponse("/folder4/folder41", new String[] {});
    checkGetFolderResponse("/folder5", new String[] { "file51.txt" });

    verify(opalRuntimeMock);

  }

  private void checkGetFolderResponse(String path, String[] expectedFolderContentArray) throws FileSystemException {

    Set<String> expectedFolderContent = new HashSet<String>(Arrays.asList(expectedFolderContentArray));
    Response response = filesResource.getFileSystemEntry(path);

    // Make sure response is OK
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    FileDto folder = (FileDto) response.getEntity();
    List<FileDto> folderContent = folder.getChildrenList();

    // Make sure folder content is as expected.
    for(FileDto oneFileOrFolder : folderContent) {
      Assert.assertTrue(expectedFolderContent.contains(oneFileOrFolder.getName()));
    }
  }

  @Test
  public void testGetFilesInFileSystem() throws IOException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).atLeastOnce();

    replay(opalRuntimeMock);

    checkGetFileResponse("/file2.txt", "testing file2.txt content");
    checkGetFileResponse("/folder1/folder11/folder111/file1112.txt", "testing file1112.txt content");
    checkGetFileResponse("/folder4/file41.txt", "testing file41.txt content");

    verify(opalRuntimeMock);

  }

  private void checkGetFileResponse(String path, String expectedFileContent) throws IOException {
    Response response = filesResource.getFileSystemEntry(path);

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

    Response response = filesResource.getFileSystemEntry("/folder1/folder2");
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);

  }

  @Test
  public void testUploadFileToFileSystem() throws FileUploadException, IOException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();
    expect(fileItemMock.getInputStream()).andReturn(getClass().getResourceAsStream("/files-to-upload/fileToUpload.txt")).once();

    FilesResource fileResource = new FilesResource(opalRuntimeMock) {
      @Override
      protected FileItem getUploadedFile(HttpServletRequest request, FileObject fileToWriteTo) throws FileUploadException {
        return fileItemMock;
      }
    };

    replay(opalRuntimeMock, fileItemMock);

    // Upload the file.
    Response response = fileResource.uploadFile("/folder1/folder11/folder111/uploadedFile.txt", null);

    // Verify that the service response is OK.
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    // Verify that the file was uploaded at the right path in the file system.
    Assert.assertTrue(fileSystem.getRoot().resolveFile("/folder1/folder11/folder111/uploadedFile.txt").exists());

    verify(opalRuntimeMock, fileItemMock);

  }

  @Test
  public void testUploadFileNoContentSubmitted() throws FileSystemException, FileUploadException {

    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    FilesResource fileResource = new FilesResource(opalRuntimeMock) {
      @Override
      protected FileItem getUploadedFile(HttpServletRequest request, FileObject fileToWriteTo) throws FileUploadException {
        return null;
      }
    };

    Response response = fileResource.uploadFile("/", null);
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);

  }

  @Test
  public void testUploadFileWhenFolderExistWithThatNameAtSpecifiedPath() throws FileSystemException, FileUploadException {

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

    Response response = fileResource.uploadFile("/", null);
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock, fileObjectMock);
  }
}
