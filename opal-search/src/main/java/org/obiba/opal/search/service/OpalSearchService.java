/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.service;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.node.internal.InternalNode;
import org.elasticsearch.rest.RestController;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.springframework.stereotype.Component;

@Component
public class OpalSearchService implements Service, ElasticSearchProvider {

  private Node esNode;

  private Client client;

  @Override
  public boolean isRunning() {
    return esNode != null;
  }

  @Override
  public void start() {
    esNode = NodeBuilder.nodeBuilder().client(true).settings(ImmutableSettings.settingsBuilder().put("http.enabled", false)).node();
    client = esNode.client();
  }

  public Client getClient() {
    return client;
  }

  public RestController getRest() {
    return ((InternalNode) esNode).injector().getInstance(RestController.class);
  }

  @Override
  public void stop() {
    esNode.close();
  }

}
