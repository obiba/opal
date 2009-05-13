/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.datasource.onyx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileOnyxDataInputStrategyTest {
  //
  // Constants
  //

  private static final String TEST_ARCHIVE = //
  "src" + File.separator + //
  "test" + File.separator + //
  "resources" + File.separator + //
  "OnyxDataInputStrategyTests" + File.separator + //
  "testArchive.zip";

  //
  // Instance Variables
  //

  private FileOnyxDataInputStrategy fileInputStrategy;

  private OnyxDataInputContext context;

  //
  // Fixture Methods (setUp, tearDown)
  //

  @Before
  public void setUp() {
    context = new OnyxDataInputContext();
    fileInputStrategy = new FileOnyxDataInputStrategy();
  }

  @After
  public void tearDown() {
    fileInputStrategy.terminate(context);
  }

  //
  // Test Methods
  //

  /**
   * The <code>listEntries</code> has no meaning for this strategy. It should simply return <code>null</code>.
   */
  @Test
  public void testListEntries() {
    fileInputStrategy.prepare(context);

    assertNull(fileInputStrategy.listEntries());
  }

  @Test
  public void testGetEntry() {
    fileInputStrategy.prepare(context);

    // Get an "entry" (i.e., a file InputStream).
    // Try this twice, to ensure that the method may be called multiple times
    // without error.
    InputStream entryStream = null;

    for(int i = 0; i < 2; i++) {
      try {
        entryStream = fileInputStrategy.getEntry(TEST_ARCHIVE);

        assertNotNull(entryStream);
      } catch(RuntimeException ex) {
        fail("Unexpected exception (" + ex.getMessage() + ")");
      }
    }
  }

  @Test
  public void testGetNonExistingEntry() {
    fileInputStrategy.prepare(context);

    try {
      fileInputStrategy.getEntry("bogus");

      fail("Expected RuntimeException");
    } catch(RuntimeException ex) {
      Throwable cause = ex.getCause();
      assertNotNull(cause);
      assertEquals("FileNotFoundException", cause.getClass().getSimpleName());
    }
  }
}
