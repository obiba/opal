/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.fs;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 *
 */
public class DecoratedFileObjectTest {

  private static final Logger log = LoggerFactory.getLogger(OpalFileSystemTest.class);

  private FileObject root;

  private static class DFO extends DecoratedFileObject {

    private DFO(FileObject decoratedFileObject) {
      super(decoratedFileObject);
    }
  }

  @Before
  public void setUp() throws IOException {

    java.io.File tempDir = Files.createTempDir();
    tempDir.deleteOnExit();
    root = VFS.getManager().resolveFile(tempDir.getAbsolutePath());
  }

  @Test
  public void testMoveTo() throws FileSystemException {

    FileObject file_1 = root.resolveFile("file_1");
    file_1.createFile();
    FileObject file_2 = root.resolveFile("file_2");
    FileObject decorate_1 = new DFO(file_1);
    FileObject decorate_2 = new DFO(file_2);
    System.out.println(decorate_1);
    System.out.println(decorate_2);
    decorate_1.moveTo(decorate_2);

//    System.out.println(file_1);
//    log.debug("file_1: {}", file_1);
  }

}
