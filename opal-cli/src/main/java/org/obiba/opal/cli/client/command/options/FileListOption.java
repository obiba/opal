/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command.options;

import java.io.File;
import java.util.List;

import uk.co.flamingpenguin.jewel.cli.Unparsed;

/**
 * This interface declares unparsed file arguments (i.e., at the end of a command line).
 * 
 * @author cag-dspathis
 * 
 */
public interface FileListOption {

  @Unparsed(name = "FILE")
  public List<File> getFiles();

  public boolean isFiles();
}
