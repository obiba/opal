/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.resource;

import org.obiba.opal.core.service.ResourceReferenceService;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.spi.resource.TabularResourceConnector;
import org.obiba.opal.spi.resource.TabularResourceConnectorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RTabularResourceConnectorFactory implements TabularResourceConnectorFactory {

  private final ResourceReferenceService resourceReferenceService;

  private final OpalRSessionManager rSessionManager;

  @Autowired
  public RTabularResourceConnectorFactory(ResourceReferenceService resourceReferenceService, OpalRSessionManager rSessionManager) {
    this.resourceReferenceService = resourceReferenceService;
    this.rSessionManager = rSessionManager;
  }

  @Override
  public TabularResourceConnector newConnector(String project, String resource) {
    return new RTabularResourceConnector(resourceReferenceService, rSessionManager, project, resource, null);
  }

}
