/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.es;

import org.elasticsearch.client.Client;
import org.elasticsearch.rest.RestController;

public interface ElasticSearchProvider {

  boolean isEnabled();

  Client getClient();

  RestController getRest();

}
