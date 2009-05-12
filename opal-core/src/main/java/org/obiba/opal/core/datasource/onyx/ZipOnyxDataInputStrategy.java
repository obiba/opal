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

  private ZipInputStream zipInputStream;

  //
  // IChainingOnyxDataInputStrategy Methods
  //

  public void setDelegate(IOnyxDataInputStrategy delegate) {
    this.delegate = delegate;
  }

  /**
   * Calls the delegate's <code>getEntry<code> method to get an <code>InputStream</code> for the 
   * data source indicated in <code>context</code> (it must be zip file), then creates a <code>ZipInputStream</code> on
   * top of it.
   * 
   * The <code>ZipInputStream</code> created by this method is subsequently used by the <code>listEntries</code> metod
   * to list zip file entries by the <code>getEntry</code> method to look up a specific entry.
   */
  public void prepare(OnyxDataInputContext context) {
    // Prepare delegate.
    delegate.prepare(context);

    // Now get an InputStream from the delegate and create a ZipInputStream on top of it.
    zipInputStream = new ZipInputStream(delegate.getEntry(context.getSource()));
  }

  public List<String> listEntries() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Returns the <code>ZipInputStream</code> (created by the <code>prepare</code> method), positioned at the
   * specified entry.
   * 
   * @param name entry name
   * @return <code>InputStream</code> positioned the specified entry (<code>null</code> if the entry was not found)
   */
  public InputStream getEntry(String name) {
    boolean foundIt = false;

    // Find the requested entry and return the InputStream, positioned at that entry.
    while(true) {
      ZipEntry entry = null;

      try {
        entry = zipInputStream.getNextEntry();
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
    if(zipInputStream != null) {
      try {
        zipInputStream.close();
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      } finally {
        zipInputStream = null;
      }

      delegate.terminate(context);
    }
  }

}
