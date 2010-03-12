/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.cfg;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.vfs.FileObject;
import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.obiba.opal.fs.OpalFileSystem;

import com.google.common.collect.Sets;

public class OpalConfiguration {
  //
  // Instance Variables
  //

  private String fileSystemRoot;

  private OpalFileSystem opalFileSystem;

  private MagmaEngineFactory magmaEngineFactory;

  private Set<FunctionalUnit> functionalUnits;

  //
  // Constructors
  //

  public OpalConfiguration() {
    functionalUnits = Sets.newLinkedHashSet();
  }

  //
  // Methods
  //

  public void setFileSystemRoot(String fileSystemRoot) {
    this.fileSystemRoot = fileSystemRoot;
  }

  public OpalFileSystem getFileSystem() {
    if(opalFileSystem == null) {
      opalFileSystem = new OpalFileSystem(fileSystemRoot);
    }
    return opalFileSystem;
  }

  public MagmaEngineFactory getMagmaEngineFactory() {
    return magmaEngineFactory;
  }

  public void setMagmaEngineFactory(MagmaEngineFactory magmaEngineFactory) {
    this.magmaEngineFactory = magmaEngineFactory;
  }

  public Set<FunctionalUnit> getFunctionalUnits() {
    return Collections.unmodifiableSet(functionalUnits);
  }

  public void setFunctionalUnits(Set<FunctionalUnit> functionalUnits) {
    this.functionalUnits.clear();
    if(functionalUnits != null) {
      this.functionalUnits.addAll(functionalUnits);
    }
  }

  public void init() throws IOException {
    for(FunctionalUnit unit : functionalUnits) {
      FileObject unitsDir = getFileSystem().getRoot().resolveFile("units");
      if(!unitsDir.exists()) {
        unitsDir.createFolder();
      }

      FileObject unitDir = unitsDir.resolveFile(unit.getName());
      if(!unitDir.exists()) {
        unitDir.createFolder();
      }
    }
  }
}