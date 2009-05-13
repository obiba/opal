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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <code>FileOnyxDataInputStrategy</code> is an IOnyxDataInputStrategy used to acquire a <code>FileInputStream</code>
 * for an Onyx data file (i.e., a zip file).
 */
public class FileOnyxDataInputStrategy implements IOnyxDataInputStrategy {
  //
  // Instance Variables
  //

  private InputStream fileInputStream;

  //
  // IOnyxDataInputStrategy Methods
  //

  public void prepare(OnyxDataInputContext context) {
    // Nothing to do.
  }

  /**
   * Returns an <code>InputStream</code> for the specified file.
   * 
   * @param entry name (a file path)
   * @return <code>InputStream</code> for the file
   * @throws <code>RuntimeException</code> on any I/O error
   */
  public InputStream getEntry(String name) {
    try {
      // Close the current stream, if there is one.
      if(fileInputStream != null) {
        fileInputStream.close();
      }

      // Create a FileInputStream for the specified file (use the name argument as the file path).
      fileInputStream = new FileInputStream(name);
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    }

    return fileInputStream;
  }

  public List<String> listEntries() {
    // Not used in this strategy. It should never be called. Just return null.
    return null;
  }

  /**
   * Closes the <code>InputStream</code> that was created by the <code>prepare</code> method.
   * 
   * @param the strategy's context
   * @throws <code>RuntimeException</code> on any I/O exception
   */
  public void terminate(OnyxDataInputContext context) {
    // Close the current stream, if there is one.
    if(fileInputStream != null) {
      try {
        fileInputStream.close();
      } catch(IOException ex) {
        throw new RuntimeException(ex);
      } finally {
        fileInputStream = null;
      }
    }
  }
}
