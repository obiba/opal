/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.resource;

import org.obiba.plugins.spi.ServicePlugin;

import java.util.List;

public interface ResourceFactoryService extends ServicePlugin {

  /**
   * Get the list of {@link ResourceFactory} taht are provided by this plugin.
   *
   * @return
   */
  List<ResourceFactory> getResourceFactories();

}
