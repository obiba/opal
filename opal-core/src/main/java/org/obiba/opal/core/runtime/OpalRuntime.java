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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.fs.OpalFileSystem;

/**
 *
 */
public interface OpalRuntime {

  public OpalConfiguration getOpalConfiguration();

  public Set<Service> getServices();

  public OpalFileSystem getFileSystem();

  public Set<FunctionalUnit> getFunctionalUnits();

  public FunctionalUnit getFunctionalUnit(String unitName);

  public FileObject getUnitDirectory(String unitName) throws NoSuchFunctionalUnitException, FileSystemException;

  public ViewManager getViewManager();

  public void start();

  public void stop();

  public void writeOpalConfiguration();
}
