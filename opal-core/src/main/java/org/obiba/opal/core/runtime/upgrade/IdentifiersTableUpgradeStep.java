/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.upgrade;

import org.obiba.runtime.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IdentifiersTableUpgradeStep extends AbstractConfigurationUpgradeStep {

  @Override
  public String getDescription() {
    return "Remove the identifiers table from the Magma engine.";
  }

  @Override
  protected void doWithConfig(Document opalConfig) {
    NodeList list = opalConfig.getElementsByTagName("beanName");
    for(int i = 0; i < list.getLength(); i++) {
      Node node = list.item(i);
      System.out.println("node " + i);
      if(node.getTextContent().equals("keySessionFactory")) {
        System.out.println(" remove node from parent");
        Node toRemove = node.getParentNode().getParentNode();
        toRemove.getParentNode().removeChild(toRemove);
      }
    }
  }

  @Override
  public Version getAppliesTo() {
    return new Version(1, 5, 0);
  }

}
