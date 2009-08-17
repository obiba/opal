/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.report;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.sesame.support.SesameUtil;
import org.openrdf.elmo.ElmoManager;
import org.openrdf.elmo.sesame.SesameManagerFactory;
import org.openrdf.model.URI;

/**
 * 
 */
public class Report implements DataItemSet {

  private String name;

  private boolean withOccurrence;

  private List<IDataItemSelection> selections;
  
  private SesameManagerFactory sesameManagerFactory;
  
  public void setSesameManagerFactory(SesameManagerFactory sesameManagerFactory) {
    this.sesameManagerFactory = sesameManagerFactory;
  }

  public String getName() {
    return name;
  }

  public boolean hasOccurrence() {
    return withOccurrence;
  }

  public Set<DataItem> getDataItems() {
    ElmoManager manager = sesameManagerFactory.createElmoManager();
    Set<DataItem> items = new LinkedHashSet<DataItem>();
    try {
      for(IDataItemSelection selection : selections) {
        for(URI item : selection.getSelection()) {
          items.add(manager.designate(SesameUtil.toQName(item), DataItem.class));
        }
      }
    } finally {
//      manager.close();
    }
    return items;
  }

  public List<IDataItemSelection> getSelections() {
    return selections;
  }

}
