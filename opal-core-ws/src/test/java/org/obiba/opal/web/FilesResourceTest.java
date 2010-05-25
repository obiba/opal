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

import java.net.URISyntaxException;

import junit.framework.Assert;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.OpalFileSystemImpl;
import org.obiba.opal.web.model.Opal.FileDto;

public class FilesResourceTest {

  private OpalRuntime opalRuntimeMock;

  private OpalFileSystem fileSystem;

  @Before
  public void setUp() throws URISyntaxException, FileSystemException {
    opalRuntimeMock = createMock(OpalRuntime.class);

    String rootDir = getClass().getResource("/test-file-system").toURI().toString();
    fileSystem = new OpalFileSystemImpl(rootDir);
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
    Assert.assertEquals(19, childrenCounter);

    verify(opalRuntimeMock);
  }

  private int verifyThatChildrenExistInFileSystem(FileDto folder, int childrenCounter) throws FileSystemException {
    FileObject correspondingFileObj;
    for(FileDto child : folder.getChildrenList()) {
      childrenCounter++;
      correspondingFileObj = fileSystem.getRoot().resolveFile(child.getPath());
      System.out.println(correspondingFileObj.getName().getPath());
      Assert.assertTrue(correspondingFileObj.exists());
      if(child.getType() == FileDto.FileType.FOLDER) {
        childrenCounter = verifyThatChildrenExistInFileSystem(child, childrenCounter);
      }
    }
    return childrenCounter;
  }

}
