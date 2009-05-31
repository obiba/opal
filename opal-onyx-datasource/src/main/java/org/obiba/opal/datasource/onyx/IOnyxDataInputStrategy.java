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

import java.io.InputStream;
import java.util.List;

/**
 * Interface for strategies used to acquire an Onyx data <code>InputStream</code>.
 */
public interface IOnyxDataInputStrategy {

  /**
   * Based on information provided by the <code>context</code> argument, prepares (i.e., initializes) the strategy.
   * 
   * This method must be called <i>prior</i> to calling any of the other methods.
   * 
   * @param context the strategy's context
   */
  public void prepare(OnyxDataInputContext context);

  /**
   * Returns a list of entries (by name) that may be retrieved with the <code>getEntry</code> method.
   * 
   * @return list of entries (by name)
   */
  public List<String> listEntries();

  /**
   * Returns an <code>InputStream</code> for reading the specified entry.
   * 
   * @param name entry name
   * @return <code>InputStream</code> for reading the entry
   */
  public InputStream getEntry(String name);

  /**
   * The strategy's clean up method.
   * 
   * This method must be called when the strategy has completed its work (i.e., after all required invocations of
   * <code>listEntries</code> and <code>getEntry</code>).
   * 
   * @param context the strategy's context
   */
  public void terminate(OnyxDataInputContext context);
}
