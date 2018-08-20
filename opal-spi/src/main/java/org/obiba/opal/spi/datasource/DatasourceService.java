/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.datasource;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.json.JSONObject;
import org.obiba.magma.DatasourceFactory;
import org.obiba.opal.spi.support.OpalFileSystemPathResolver;
import org.obiba.plugins.spi.ServicePlugin;

/**
 * {@link ServicePlugin} that acts as a {@link DatasourceFactory} factory.
 */
public interface DatasourceService extends ServicePlugin {

  String SERVICE_TYPE = "opal-datasource";

  /**
   * Get the {@link DatasourceUsage}s that are covered by this datasource factory.
   *
   * @return
   */
  @NotNull Collection<DatasourceUsage> getUsages();

  /**
   * Get the user parameters form specifications for a specific usage as a JSON object.
   *
   * @param usage
   * @return
   */
  JSONObject getJSONSchemaForm(@NotNull DatasourceUsage usage);

  /**
   * Create a new instance of datasource factory with specified usage and user parameters.
   *
   * @param usage
   * @param parameters
   * @return
   */
  DatasourceFactory createDatasourceFactory(@NotNull DatasourceUsage usage, @NotNull JSONObject parameters);

  /**
   * Sets an instance of an Opal file system path resolver.
   *
   * @param resolver
   */
  void setOpalFileSystemPathResolver(OpalFileSystemPathResolver resolver);

}
