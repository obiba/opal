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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * <code>IOnyxDataInputStrategy</code> used to acquire an <code>InputStream</code> for entries in an Onyx data file
 * (i.e., a zip file).
 */
public class ZipOnyxDataInputStrategy implements IChainingOnyxDataInputStrategy {
  //
  // Instance Variables
  //

  private IOnyxDataInputStrategy delegate;

  private String source;

  private ZipInputStream zipInputStream;

  //
  // IChainingOnyxDataInputStrategy Methods
  //

  public void setDelegate(IOnyxDataInputStrategy delegate) {
    this.delegate = delegate;
  }

  /**
   * Calls the delegate's <code>getEntry<code> method to get an <code>InputStream</code> for the 
   * data source indicated in <code>context</code> (it must be a zip file), then creates a <code>ZipInputStream</code> on
   * top of it.
   * 
   * The <code>ZipInputStream</code> created by this method is subsequently used by the <code>listEntries</code> method
   * to list zip file entries and by the <code>getEntry</code> method to return an <code>InputStream</code> for a specific entry.
   */
  public void prepare(OnyxDataInputContext context) {
    // Prepare delegate.
    delegate.prepare(context);

    // Initialize the data source.
    source = context.getSource();
  }

  /**
   * Returns a list of the zip file's entries.
   * 
   * @return list of zip file entries (i.e., their names)
   */
  public List<String> listEntries() {
    if(source == null) {
      throw new IllegalStateException("Null source (prepare method must be called prior to listEntries method");
    }

    // Get an InputStream from the delegate and create a ZipInputStream on top of it.
    zipInputStream = new ZipInputStream(delegate.getEntry(source));

    // Read all the zip entries and add their names to a list.
    List<String> entries = new ArrayList<String>();

    while(true) {
      ZipEntry entry = null;

      try {
        entry = zipInputStream.getNextEntry();

        if(entry == null) {
          break;
        }
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      }

      entries.add(entry.getName());
    }

    return entries;
  }

  /**
   * Returns the <code>ZipInputStream</code> (created by the <code>prepare</code> method), positioned at the
   * specified entry.
   * 
   * @param name entry name
   * @return <code>InputStream</code> positioned the specified entry (<code>null</code> if the entry was not found)
   * @throws IllegalStateException if <code>prepare</code> method was not called
   */
  public InputStream getEntry(String name) {
    if(source == null) {
      throw new IllegalStateException("Null source (prepare method must be called prior to getEntry method");
    }

    // Get an InputStream from the delegate and create a ZipInputStream on top of it.
    zipInputStream = new ZipInputStream(delegate.getEntry(source));

    // Find the requested entry and return the InputStream, positioned at that entry.
    boolean foundIt = false;

    while(true) {
      ZipEntry entry = null;

      try {
        entry = zipInputStream.getNextEntry();

        if(entry == null) {
          break;
        }
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      }

      if(entry.getName().equals(name)) {
        foundIt = true;
        break;
      }
    }

    // Only return the InputStream if the entry was found; otherwise return null.
    return foundIt ? zipInputStream : null;
  }

  /**
   * Closes the <code>ZipInputStream</code> created by the <code>prepare</code> method, then calls the delegate's
   * <code>terminate</code> method.
   * 
   * @param the strategy's context
   */
  public void terminate(OnyxDataInputContext context) {
    // Set the data source to null.
    source = null;

    // Close the current stream, if there is one.
    if(zipInputStream != null) {
      try {
        zipInputStream.close();
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      } finally {
        // Set the stream to null.
        zipInputStream = null;
      }
    }

    // Terminate the delegate.
    delegate.terminate(context);
  }

}
