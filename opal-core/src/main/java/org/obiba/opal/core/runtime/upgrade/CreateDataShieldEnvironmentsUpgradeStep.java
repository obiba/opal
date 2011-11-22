/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
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

public class CreateDataShieldEnvironmentsUpgradeStep extends AbstractConfigurationUpgradeStep {

  @Override
  public String getDescription() {
    return "Create the AGGREGATE environment from existing aggregating methods.";
  }

  @Override
  protected void doWithConfig(Document opalConfig) {
    NodeList list = opalConfig.getElementsByTagName("aggregatingMethods");
    if(list.getLength() != 1) {
      // Nothing to do.
      return;
    }

    Node aggregatingMethods = list.item(0);
    Node datashieldConfig = aggregatingMethods.getParentNode();

    // Special case of empty list
    if(aggregatingMethods.hasChildNodes() == false) {
      // no need to create environment
      datashieldConfig.removeChild(aggregatingMethods);
      return;
    }

    Node environment = createEnvironmentNode(opalConfig, aggregatingMethods);
    datashieldConfig.replaceChild(environment, aggregatingMethods);
  }

  @Override
  public Version getAppliesTo() {
    return new Version(1, 7, 0);
  }

  private Node createEnvironmentNode(Document opalConfig, Node aggregatingMethods) {
    Node environments = opalConfig.createElement("environments");
    Node environment = opalConfig.createElement("org.obiba.opal.datashield.DataShieldEnvironment");
    environments.appendChild(environment);
    Node aggregate = opalConfig.createElement("environment");
    aggregate.setTextContent("AGGREGATE");
    environment.appendChild(aggregate);

    Node methods = opalConfig.createElement("methods");
    environment.appendChild(methods);

    while(aggregatingMethods.hasChildNodes()) {
      methods.appendChild(aggregatingMethods.getFirstChild());
    }
    return environments;
  }

}
