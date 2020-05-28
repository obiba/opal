/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.analysis.support;

import org.obiba.opal.web.gwt.app.client.support.PluginsResource;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;

public class AnalysisPluginsResource extends PluginsResource {

  @Override
  public String getResource() {
    return UriBuilders.PLUGINS_ANALYSIS.create().build();
  }

  public static AnalysisPluginsResource getInstance() {
    return new AnalysisPluginsResource();
  }

}
