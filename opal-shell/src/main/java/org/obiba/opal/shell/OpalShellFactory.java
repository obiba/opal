/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Factory of {@code OpalShell} instances.
 */
public interface OpalShellFactory {

  /**
   * Construct a new {@code OpalShell} instance using the provided streams as input and output.
   *
   * @param registry
   * @param in
   * @param out
   * @param err
   * @return
   */
  public OpalShell newShell(CommandRegistry registry, InputStream in, OutputStream out, OutputStream err);

}
