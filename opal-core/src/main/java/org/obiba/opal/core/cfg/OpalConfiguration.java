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

import org.obiba.magma.support.MagmaEngineFactory;
import org.obiba.opal.core.unit.FunctionalUnit;

import com.google.common.collect.Sets;

public class OpalConfiguration {
  //
  // Instance Variables
  //

  private String fileSystemRoot;

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

  public String getFileSystemRoot() {
    return fileSystemRoot;
  }

  public MagmaEngineFactory getMagmaEngineFactory() {
    return magmaEngineFactory;
  }

  public void setMagmaEngineFactory(MagmaEngineFactory magmaEngineFactory) {
    this.magmaEngineFactory = magmaEngineFactory;
  }

  public FunctionalUnit getFunctionalUnit(String unitName) {
    for(FunctionalUnit unit : functionalUnits) {
      if(unit.getName().equals(unitName)) {
        return unit;
      }
    }
    return null;
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

  }

}