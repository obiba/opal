/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.domain.metadata;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DataItemSet {
  //
  // Instance Variables
  //

  private String name;

  private Set<DataItem> dataItems;

  //
  // Constructors
  //

  public DataItemSet(String name) {
    this.name = name;

    dataItems = new LinkedHashSet<DataItem>();
  }

  //
  // Methods
  //

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<DataItem> getDataItems() {
    return Collections.unmodifiableSet(dataItems);
  }

  public void setDataItems(Set<DataItem> dataItems) {
    this.dataItems.clear();

    if(dataItems != null) {
      this.dataItems.addAll(dataItems);
    }
  }
}
