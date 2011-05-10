/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.es;

import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchConfigurationService {

  private final OpalConfigurationService configService;

  @Autowired
  public ElasticSearchConfigurationService(OpalConfigurationService configService) {
    this.configService = configService;
  }

  public ElasticSearchConfiguration getConfig() {
    if(configService.getOpalConfiguration().hasExtension(ElasticSearchConfiguration.class)) {
      return configService.getOpalConfiguration().getExtension(ElasticSearchConfiguration.class);
    }
    return new ElasticSearchConfiguration();
  }
}
