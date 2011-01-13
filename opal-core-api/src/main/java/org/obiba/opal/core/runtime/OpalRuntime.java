/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime;

import java.util.Set;

import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.fs.OpalFileSystem;

/**
 *
 */
public interface OpalRuntime {

  public OpalConfiguration getOpalConfiguration();

  public Set<Service> getServices();

  public OpalFileSystem getFileSystem();

  public ViewManager getViewManager();

  public void start();

  public void stop();

  public void writeOpalConfiguration();
}
