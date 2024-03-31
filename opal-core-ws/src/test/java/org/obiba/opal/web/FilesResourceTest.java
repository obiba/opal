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

import com.google.common.collect.Lists;
import org.apache.commons.vfs2.*;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.web.model.Opal.FileDto;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings({"OverlyLongMethod", "PMD.NcssMethodCount"})
public class FilesResourceTest {

  private OpalFileSystemService opalFileSystemServiceMock;

  private SubjectAclService subjectAclServiceMock;

  private OpalFileSystem fileSystem;

  private FilesResource filesResource;

  private FileObject fileObjectMock;

  private InputPart fileItemMock;

  private UriInfo uriInfoMock;

  /**
   * Delete these files in tearDown().
   */
  private final Collection<String> filesCreatedByTest = new ArrayList<>();

  @Before
  public void setUp() throws URISyntaxException {
    opalFileSystemServiceMock = createMock(OpalFileSystemService.class);
    subjectAclServiceMock = createMock(SubjectAclService.class);

    String rootDir = getClass().getResource("/test-file-system").toURI().toString();
    File emptyDir = new File(rootDir.replace("file:", ""), "folder4/folder41");
    if (!emptyDir.exists()) {
      assertThat(emptyDir.mkdirs()).isTrue();
    }
    fileSystem = new DefaultOpalFileSystem(rootDir);
    filesResource = new FilesResource();
    filesResource.setOpalFileSystemService(opalFileSystemServiceMock);
    filesResource.setSubjectAclService(subjectAclServiceMock);

    fileItemMock = createMock(InputPart.class);
    fileObjectMock = createMock(FileObject.class);

    uriInfoMock = createMock(UriInfo.class);
  }

  @After
  public void tearDown() throws IOException {
    // Delete any files created by the test.
    for (String filePath : filesCreatedByTest) {
      FileObject file = fileSystem.getRoot().resolveFile(filePath);
      if (file.exists()) {
        file.delete();
      }
    }
  }

  @Test
  public void testGetFileSystem() throws IOException {

    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalFileSystemServiceMock);

    FileSystemResource fsResource = new FileSystemResource();
    fsResource.setOpalFileSystemService(opalFileSystemServiceMock);
    FileDto rootFileDto = fsResource.getFileSystem();

    assertThat(rootFileDto.getName()).isEqualTo("root");
    assertThat(rootFileDto.getType()).isEqualTo(FileDto.FileType.FOLDER);
    assertThat(rootFileDto.getPath()).isEqualTo("/");

  }

  @Test
  @Ignore
  public void verifyThatAllFilesAndFoldersInDtoStructureExistInFileSystem() throws IOException {
    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalFileSystemServiceMock);

    FileSystemResource fsResource = new FileSystemResource();
    fsResource.setOpalFileSystemService(opalFileSystemServiceMock);

    FileDto rootFileDto = fsResource.getFileSystem();

    int childrenCounter = 0;
    childrenCounter = verifyThatChildrenExistInFileSystem(rootFileDto, childrenCounter);

    // File count in Dto structure should be the same as file count in file system.
    assertThat(childrenCounter).isEqualTo(20);

    verify(opalFileSystemServiceMock);
  }

  private int verifyThatChildrenExistInFileSystem(FileDto folder, int childrenCounter) throws IOException {
    FileObject correspondingFileObj;
    int counter = childrenCounter;
    for (FileDto child : folder.getChildrenList()) {
      counter++;
      correspondingFileObj = fileSystem.getRoot().resolveFile(child.getPath());
      assertThat(correspondingFileObj.exists()).isTrue();
      if (child.getType() == FileDto.FileType.FOLDER) {
        counter = verifyThatChildrenExistInFileSystem(child, childrenCounter);
      }
    }
    return counter;
  }

  @Ignore("SecurityManager dependency not satisfied")
  @Test
  public void testGetFoldersDetailsInFileSystem() throws IOException {
    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).atLeastOnce();

    replay(opalFileSystemServiceMock);

    checkGetFileDetailsResponse("/", "folder1", "folder2", "folder3", "folder4", "folder5", "file2.txt", "folder11",
        "file11.txt", "file21.txt", "folder31", "folder41", "file41.txt", "file42.txt", "file43.txt", "file51.txt");
    checkGetFileDetailsResponse("/folder1/folder11", "folder111", "file111.txt", "file1111.txt", "file1112.txt");
    checkGetFileDetailsResponse("/folder1/folder11/folder111", "file1111.txt", "file1112.txt");
    checkGetFileDetailsResponse("/folder2", "file21.txt");
    checkGetFileDetailsResponse("/folder3", "folder31", "file311.txt");
    checkGetFileDetailsResponse("/folder4", "folder41", "file41.txt", "file42.txt", "file43.txt");
    checkGetFileDetailsResponse("/folder5", "file51.txt");

    verify(opalFileSystemServiceMock);

  }

  private void checkGetFileDetailsResponse(String path, String... expectedFolderContentArray)
      throws IOException {

    Set<String> expectedFolderContent = new HashSet<>(Arrays.asList(expectedFolderContentArray));
    Response response = filesResource.getFileDetails(path);

    // Make sure response is OK
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

    FileDto folder = (FileDto) response.getEntity();
    List<FileDto> folderContent = folder.getChildrenList();

    // Check folder content recursively two levels down.
    checkFolderContent(expectedFolderContent, folderContent, 2);

    assertThat(expectedFolderContent).isEmpty();
  }

  private void checkFolderContent(Set<String> expectedFolderContent, Iterable<FileDto> folderContent, int level) {
    // Make sure folder content is as expected.
    for (FileDto oneFileOrFolder : folderContent) {
      assertThat(expectedFolderContent).contains(oneFileOrFolder.getName());
      expectedFolderContent.remove(oneFileOrFolder.getName());
      if (level > 0 && oneFileOrFolder.getChildrenCount() > 0) {
        checkFolderContent(expectedFolderContent, oneFileOrFolder.getChildrenList(), level - 1);
      }
    }
  }

  @Ignore("SecurityManager dependency not satisfied")
  @Test
  public void testGetFile_GetCompressedFolderFromFileSystem() throws IOException {
    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).atLeastOnce();
    replay(opalFileSystemServiceMock);

    checkCompressedFolder("/folder1", "folder1", "folder1/folder11", "folder1/file11.txt", "folder1/folder11/folder111",
        "folder1/folder11/file111.txt", "folder1/folder11/folder111/file1111.txt",
        "folder1/folder11/folder111/file1112.txt");
    checkCompressedFolder("/folder2", "folder2", "folder2/file21.txt");
    checkCompressedFolder("/folder3", "folder3", "folder3/folder31", "folder3/folder31/file311.txt");
    checkCompressedFolder("/folder4", "folder4", "folder4/folder41", "folder4/file41.txt", "folder4/file42.txt",
        "folder4/file43.txt");
    checkCompressedFolder("/folder5", "folder5", "folder5/file51.txt");
    checkCompressedFolder("/", "/", "folder1", "folder1/folder11", "folder1/file11.txt", "folder1/folder11/folder111",
        "folder1/folder11/file111.txt", "folder1/folder11/folder111/file1111.txt",
        "folder1/folder11/folder111/file1112.txt", "folder2", "folder2/file21.txt", "folder3", "folder3/folder31",
        "folder3/folder31/file311.txt", "folder4", "folder4/folder41", "folder4/file41.txt", "folder4/file42.txt",
        "folder4/file43.txt", "folder5", "folder5/file51.txt", "file2.txt");

    verify(opalFileSystemServiceMock);

  }

  @SuppressWarnings("unchecked")
  private void checkCompressedFolder(String folderPath, String... expectedFolderContentArray) throws IOException {
    Response response = filesResource.getFile(folderPath, null, null);
    ZipFile zipfile = new ZipFile(((File) response.getEntity()).getPath());

    // Check that all folders and files exist in the compressed archive that represents the folder.
    for (String anExpectedFolderContentArray : expectedFolderContentArray) {
      assertThat(zipfile.getEntry(anExpectedFolderContentArray)).isNotNull();
    }

    Enumeration<ZipEntry> zipEnum = (Enumeration<ZipEntry>) zipfile.entries();
    int count = 0;

    while (zipEnum.hasMoreElements()) {
      zipEnum.nextElement();
      count++;
    }

    // Make sure that they are no unexpected files in the compressed archive.
    assertThat(expectedFolderContentArray.length).isEqualTo(count);

    zipfile.close();
  }

  @Test
  public void testGetPathThatDoesNotExist() throws IOException {
    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalFileSystemServiceMock);

    try {
      filesResource.getFileDetails("/folder1/folder2");
      assertThat(false).isTrue();
    } catch (NoSuchFileException e) {
    }

    verify(opalFileSystemServiceMock);

  }

  @Test
  @Ignore
  public void testUploadFileToFileSystem() throws IOException {
    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).once();
    expect(fileItemMock.getFileName()).andReturn("fileToUpload.txt").atLeastOnce();
    expect(fileItemMock.getBody(InputStream.class, null)).andReturn(getClass().getResourceAsStream("/files-to-upload/fileToUpload.txt"))
        .once();
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilder.fromPath("/"));

    FilesResource fileResource = new FilesResource() {
      @Override
      protected List<InputPart> getUploadedFiles(MultipartFormDataInput input) {
        return Lists.newArrayList(fileItemMock);
      }
    };
    fileResource.setOpalFileSystemService(opalFileSystemServiceMock);
    fileResource.setSubjectAclService(subjectAclServiceMock);

    replay(opalFileSystemServiceMock, fileItemMock, uriInfoMock);

    // Upload the file.
    String destinationPath = "/folder1/folder11/folder111";
    Response response = fileResource.uploadFile(destinationPath, uriInfoMock, null);
    filesCreatedByTest.add(destinationPath);

    // Verify that the service response is CREATED.
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

    verify(opalFileSystemServiceMock, fileItemMock, uriInfoMock);

    // Verify that the file was uploaded at the right path in the file system.
    assertThat(fileSystem.getRoot().resolveFile("/folder1/folder11/folder111/fileToUpload.txt").exists()).isTrue();
    // clean up
    fileSystem.getRoot().resolveFile("/folder1/folder11/folder111/fileToUpload.txt").delete();
  }

  @Test
  public void testUploadFileNoContentSubmitted() throws IOException {

    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalFileSystemServiceMock);

    FilesResource fileResource = new FilesResource() {
      @Override
      protected List<InputPart> getUploadedFiles(MultipartFormDataInput input) {
        return Lists.newArrayList();
      }
    };
    fileResource.setOpalFileSystemService(opalFileSystemServiceMock);
    fileResource.setSubjectAclService(subjectAclServiceMock);

    Response response = fileResource.uploadFile("/", uriInfoMock, null);
    assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

    verify(opalFileSystemServiceMock);

  }

  @Test
  public void testUploadFile_ReturnsNotFoundResponseWhenUploadDestinationDoesNotExist()
      throws IOException {
    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).once();

    FilesResource fileResource = new FilesResource() {
      @Override
      protected List<InputPart> getUploadedFiles(MultipartFormDataInput input) {
        return Lists.newArrayList(fileItemMock);
      }
    };
    fileResource.setOpalFileSystemService(opalFileSystemServiceMock);
    filesResource.setSubjectAclService(subjectAclServiceMock);

    replay(opalFileSystemServiceMock, fileItemMock, uriInfoMock);

    // Upload the file.
    String destinationPath = "/folder1/folder11/folder111/patate";
    try {
      fileResource.uploadFile(destinationPath, uriInfoMock, null);
      assertThat(false).isTrue();
    } catch (NoSuchFileException e) {

    }

    verify(opalFileSystemServiceMock, fileItemMock, uriInfoMock);
  }

  @Test
  public void testDeleteFile_FileDoesNotExist() throws IOException {
    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalFileSystemServiceMock);

    try {
      filesResource.deleteFile("/folder1/folder2/filethatdoesnotexist.txt");
      assertThat(false).isTrue();
    } catch (NoSuchFileException e) {
    }

    verify(opalFileSystemServiceMock);
  }

  @Test
  @Ignore
  public void testDeleteFile_CannotDeleteFolderWithContent() throws IOException {
    expect(opalFileSystemServiceMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalFileSystemServiceMock);

    Response response = filesResource.deleteFile("/folder1");
    assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    assertThat(response.getEntity()).isEqualTo("cannotDeleteNotEmptyFolder");
    verify(opalFileSystemServiceMock);
  }

  @Test
  @Ignore
  public void testDeleteFile_CannotDeleteReadOnlyFile() throws IOException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.isWriteable()).andReturn(false).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().deleteFile("path");
    assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    assertThat(response.getEntity()).isEqualTo("cannotDeleteReadOnlyFile");
    verify(fileObjectMock);
  }

  @Test
  public void testDeleteFile_FileDeletedSuccessfully() throws IOException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.delete()).andReturn(true).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().deleteFile("path");
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

    verify(fileObjectMock);

  }

  @Test
  public void testDeleteFile_CouldNotDeleteFile() throws IOException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.delete()).andThrow(new FileSystemException("test")).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().deleteFile("path");
    assertThat(response.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    assertThat(response.getEntity()).isEqualTo("couldNotDeleteFileError");
    verify(fileObjectMock);
  }

  private FilesResource getFileResource() {
    FilesResource resource = new FilesResource() {
      @Override
      protected FileObject resolveFileInFileSystem(String path) {
        return fileObjectMock;
      }
    };
    resource.setOpalFileSystemService(opalFileSystemServiceMock);
    resource.setSubjectAclService(subjectAclServiceMock);
    return resource;
  }

  @Test
  public void testCreateFolder_CannotCreateFolderPathAlreadyExist() throws IOException, URISyntaxException {
    expect(fileObjectMock.getType()).andReturn(FileType.FOLDER).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.resolveFile("folder11")).andReturn(fileSystem.getRoot().resolveFile("/folder1/folder11"))
        .atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().createFolder("folder1", "folder11", uriInfoMock);
    assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    assertThat(response.getEntity()).isEqualTo("cannotCreateFolderPathAlreadyExist");
    verify(fileObjectMock);
  }

  @Test
  public void testCreateFolder_CannotCreateFolderParentIsReadOnly() throws IOException, URISyntaxException {
    expect(fileObjectMock.getType()).andReturn(FileType.FOLDER).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    FileObject childFolderMock = createMock(FileObject.class);
    expect(childFolderMock.exists()).andReturn(false).atLeastOnce();
    FileObject parentFolderMock = createMock(FileObject.class);
    expect(childFolderMock.getParent()).andReturn(parentFolderMock).atLeastOnce();
    expect(parentFolderMock.isWriteable()).andReturn(false).atLeastOnce();
    expect(fileObjectMock.resolveFile("folder")).andReturn(childFolderMock).atLeastOnce();

    replay(fileObjectMock, parentFolderMock, childFolderMock);

    Response response = getFileResource().createFolder("folder1", "folder", uriInfoMock);
    assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
    assertThat(response.getEntity()).isEqualTo("cannotCreateFolderParentIsReadOnly");
    verify(fileObjectMock, parentFolderMock, childFolderMock);
  }

  @Test
  public void testCreateFolder_FolderCreatedSuccessfully() throws IOException, URISyntaxException {
    expect(fileObjectMock.getType()).andReturn(FileType.FOLDER).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();

    FileObject childFolderMock = createMock(FileObject.class);

    FileName fileNameMock = createMock(FileName.class);
    expect(fileNameMock.getBaseName()).andReturn("folder").atLeastOnce();
    expect(fileNameMock.getPath()).andReturn("folder1/folder").atLeastOnce();

    expect(childFolderMock.getName()).andReturn(fileNameMock).atLeastOnce();
    expect(childFolderMock.exists()).andReturn(false).atLeastOnce();

    FileContent mockContent = createMock(FileContent.class);
    expect(childFolderMock.getContent()).andReturn(mockContent).atLeastOnce();
    expect(mockContent.getLastModifiedTime()).andReturn((long) 1).atLeastOnce();

    childFolderMock.createFolder();
    FileObject parentFolderMock = createMock(FileObject.class);
    expect(childFolderMock.getParent()).andReturn(parentFolderMock).atLeastOnce();
    expect(childFolderMock.isReadable()).andReturn(true).atLeastOnce();
    expect(childFolderMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(parentFolderMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.resolveFile("folder")).andReturn(childFolderMock).atLeastOnce();
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilder.fromPath("/"));

    replay(fileObjectMock, uriInfoMock, parentFolderMock, childFolderMock, fileNameMock, mockContent);

    Response response = getFileResource().createFolder("folder1", "folder", uriInfoMock);
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    verify(fileObjectMock, uriInfoMock, parentFolderMock, childFolderMock, fileNameMock, mockContent);
  }

  @Test
  public void testCharsetsAvailable() throws Exception {
    Response charSets = filesResource.getAvailableCharsets();
    assertThat(charSets.getEntity().toString()).contains("UTF-8");
  }
}
