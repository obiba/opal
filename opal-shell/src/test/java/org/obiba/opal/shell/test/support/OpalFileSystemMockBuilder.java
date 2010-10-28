/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.test.support;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.easymock.IExpectationSetters;
import org.obiba.opal.fs.OpalFileSystem;

/**
 * A builder for creating {@link OpalFileSystem} mocks.
 */
public class OpalFileSystemMockBuilder {
  //
  // Instance Variables
  //

  private OpalFileSystem opalFileSystemMock;

  private FileObject root;

  private Map<String, FileObject> fileMap = new HashMap<String, FileObject>();

  private IExpectationSetters<?> expectationSetters;

  //
  // Constructors
  //

  public OpalFileSystemMockBuilder() {
    opalFileSystemMock = createMock(OpalFileSystem.class);

    root = createMock(FileObject.class);
    expect(opalFileSystemMock.getRoot()).andReturn(root).anyTimes();

    fileMap.put("/", root);
  }

  //
  // Methods
  //

  public static OpalFileSystemMockBuilder newBuilder() {
    return new OpalFileSystemMockBuilder();
  }

  public OpalFileSystemMockBuilder resolveFile(String absolutePath) throws FileSystemException {
    FileObject file = createMock(FileObject.class);
    expect(root.resolveFile(absolutePath)).andReturn(file).anyTimes();

    fileMap.put(absolutePath, file);

    return this;
  }

  public OpalFileSystemMockBuilder resolveFile(String parentPath, String relativePath) throws FileSystemException {
    FileObject file = createMock(FileObject.class);
    FileObject parent = fileMap.get(parentPath);
    expect(parent.resolveFile(relativePath)).andReturn(file).anyTimes();

    fileMap.put(parentPath + "/" + relativePath, file);

    return this;
  }

  public OpalFileSystemMockBuilder getLocalFile(String absolutePath, String localPath) throws FileSystemException {
    FileObject file = fileMap.get(absolutePath);
    expect(opalFileSystemMock.getLocalFile(file)).andReturn(new File(localPath)).anyTimes();

    return this;
  }

  public OpalFileSystemMockBuilder createFolder(String folderPath) throws FileSystemException {
    FileObject folder = fileMap.get(folderPath);
    folder.createFolder();

    expectationSetters = expectLastCall();

    return this;
  }

  public OpalFileSystemMockBuilder getObfuscatedPath(String absolutePath) {
    FileObject file = fileMap.get(absolutePath);
    expect(opalFileSystemMock.getObfuscatedPath(file)).andReturn("OBFUSCATED[" + absolutePath + "]").anyTimes();

    return this;
  }

  public OpalFileSystemMockBuilder once() {
    if(expectationSetters != null) {
      expectationSetters.once();
      expectationSetters = null;
    }

    return this;
  }

  public OpalFileSystemMockBuilder atLeastOnce() {
    if(expectationSetters != null) {
      expectationSetters.atLeastOnce();
      expectationSetters = null;
    }

    return this;
  }

  public OpalFileSystemMockBuilder anyTimes() {
    if(expectationSetters != null) {
      expectationSetters.anyTimes();
      expectationSetters = null;
    }

    return this;
  }

  public OpalFileSystem build() {
    for(FileObject fileMock : fileMap.values()) {
      replay(fileMock);
    }

    return opalFileSystemMock;
  }

  //
  // Inner Classes
  //

  static class FileObjectMatcher implements IArgumentMatcher {

    private FileObject expected;

    public FileObjectMatcher(FileObject expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      if(actual instanceof FileObject) {
        return ((FileObject) actual).getName().getPath().equals(expected.getName().getPath());
      } else {
        return false;
      }
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqFileObject(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with path \"");
      buffer.append(expected.getName().getPath());
      buffer.append("\")");
    }

  }

  static FileObject eqFileObject(FileObject in) {
    EasyMock.reportMatcher(new FileObjectMatcher(in));
    return null;
  }
}
