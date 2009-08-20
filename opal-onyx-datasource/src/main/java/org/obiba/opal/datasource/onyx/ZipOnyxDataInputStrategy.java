/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * <code>IOnyxDataInputStrategy</code> used to acquire an <code>InputStream</code> for entries in an Onyx data file
 * (i.e., a zip file).
 */
public class ZipOnyxDataInputStrategy implements IOnyxDataInputStrategy {
  //
  // Instance Variables
  //

  private String filename;

  private ZipFile zipFile;

  /**
   * Calls the delegate's <code>getEntry<code> method to get an <code>InputStream</code> for the data source indicated
   * in <code>context</code> (it must be a zip file), then creates a <code>ZipInputStream</code> on top of it.
   * 
   * The <code>ZipInputStream</code> created by this method is subsequently used by the <code>listEntries</code> method
   * to list zip file entries and by the <code>getEntry</code> method to return an <code>InputStream</code> for a
   * specific entry.
   */
  public void prepare(OnyxDataInputContext context) {
    // Initialize the data source.
    filename = context.getSource();

    try {
      zipFile = new ZipFile(filename);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a list of the zip file's entries.
   * 
   * @return list of zip file entries (i.e., their names)
   */
  public List<String> listEntries() {
    if(filename == null) {
      throw new IllegalStateException("Null source (prepare method must be called prior to listEntries method");
    }

    // Read all the zip entries and add their names to a list.
    List<String> entrieNames = new ArrayList<String>();

    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while(entries.hasMoreElements()) {
      ZipEntry zipEntry = (ZipEntry) entries.nextElement();
      entrieNames.add(zipEntry.getName());
    }
    return entrieNames;
  }

  /**
   * Returns the <code>ZipInputStream</code> (created by the <code>prepare</code> method), positioned at the specified
   * entry.
   * 
   * @param name entry name
   * @return <code>InputStream</code> positioned the specified entry (<code>null</code> if the entry was not found)
   * @throws IllegalStateException if <code>prepare</code> method was not called
   */
  public InputStream getEntry(String name) {
    if(filename == null) {
      throw new IllegalStateException("Null source (prepare method must be called prior to getEntry method");
    }

    ZipEntry entry = zipFile.getEntry(name);
    if(entry == null) {
      return null;
    }

    try {
      return zipFile.getInputStream(entry);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Closes the <code>ZipInputStream</code> created by the <code>prepare</code> method, then calls the delegate's
   * <code>terminate</code> method.
   * 
   * @param the strategy's context
   */
  public void terminate(OnyxDataInputContext context) {
    // Set the data source to null.
    filename = null;
    try {
      zipFile.close();
    } catch(Exception e) {

    }
    zipFile = null;
  }

  //
  // Methods
  //

  public String getSource() {
    return filename;
  }
}
