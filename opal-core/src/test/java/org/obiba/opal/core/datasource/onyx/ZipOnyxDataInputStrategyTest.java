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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZipOnyxDataInputStrategyTest {
  //
  // Constants
  //

  private static final String TEST_ARCHIVE = //
  "src" + File.separator + //
  "test" + File.separator + //
  "resources" + File.separator + //
  "ZipOnyxDataInputStrategyTest" + File.separator + //
  "testArchive.zip";

  //
  // Instance Variables
  //

  private ZipOnyxDataInputStrategy zipStrategy;

  private OnyxDataInputContext context;

  //
  // Fixture Methods (setUp, tearDown)
  //

  @Before
  public void setUp() {
    context = new OnyxDataInputContext();
    context.setSource(TEST_ARCHIVE);

    zipStrategy = new ZipOnyxDataInputStrategy();
    zipStrategy.setDelegate(new FileOnyxDataInputStrategy());
  }

  @After
  public void tearDown() {
    zipStrategy.terminate(context);
  }

  //
  // Test Methods
  //

  @Test
  public void testPrepare() {
    zipStrategy.prepare(context);

    assertEquals(context.getSource(), zipStrategy.getSource());
  }

  @Test
  public void testListEntries() {
    zipStrategy.prepare(context);
    List<String> entries = zipStrategy.listEntries();

    assertNotNull(entries);
    assertEquals(3, entries.size());
    assertTrue(entries.contains("entry1.txt"));
    assertTrue(entries.contains("entry2.txt"));
    assertTrue(entries.contains("entry3.txt"));
  }

  @Test
  public void testListEntriesBeforePrepare() {
    try {
      zipStrategy.listEntries();
      fail("Expected IllegalStateException");
    } catch(IllegalStateException ex) {
      String exMsg = ex.getMessage();
      assertNotNull(exMsg);
      assertEquals("Null source (prepare method must be called prior to listEntries method", exMsg);
    } catch(Exception ex) {
      fail("Unexpected exception type (" + ex.getClass().getSimpleName() + ")");
    }
  }

  @Test
  public void testGetEntry() {
    zipStrategy.prepare(context);

    // Get an entry and verify its contents.
    InputStream entryOneStream = zipStrategy.getEntry("entry1.txt");
    assertNotNull(entryOneStream);
    verifyEntryContents(entryOneStream, "entry1 contents");

    // Get another entry and verify its contents.
    InputStream entryTwoStream = zipStrategy.getEntry("entry2.txt");
    assertNotNull(entryTwoStream);
    verifyEntryContents(entryTwoStream, "entry2 contents");
  }

  @Test
  public void testGetEntryBeforePrepare() {
    try {
      zipStrategy.getEntry("entry1.txt");
      fail("Expected IllegalStateException");
    } catch(IllegalStateException ex) {
      String exMsg = ex.getMessage();
      assertNotNull(exMsg);
      assertEquals("Null source (prepare method must be called prior to getEntry method", exMsg);
    } catch(Exception ex) {
      fail("Unexpected exception type (" + ex.getClass().getSimpleName() + ")");
    }
  }

  @Test
  public void testGetNonExistingEntry() {
    zipStrategy.prepare(context);
    InputStream entryStream = zipStrategy.getEntry("bogus");

    assertNull(entryStream);
  }

  @Test
  public void testTerminate() {
    zipStrategy.prepare(context);
    zipStrategy.terminate(context);

    assertNull(zipStrategy.getSource());
  }

  //
  // Methods
  //

  /**
   * Verifies the contents of an entry.
   * 
   * @param entryStream <code>InputStream</code> for the entry
   * @param entryContents expected entry contents
   */
  private void verifyEntryContents(InputStream entryStream, String entryContents) {
    BufferedReader streamReader = null;

    try {
      streamReader = new BufferedReader(new InputStreamReader(entryStream));
      String line = streamReader.readLine();

      assertEquals(entryContents, line);
    } catch(IOException ex) {
      fail(ex.getMessage());
    } finally {
      try {
        streamReader.close();
      } catch(IOException ex) {
        fail(ex.getMessage());
      }
    }
  }
}
