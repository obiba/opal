/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import org.obiba.opal.search.es.ElasticSearchProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Creates @{SearchQueryExecutor} based on Elastic Search.
 */
@Component
public class EsSearchQueryExecutorFactory implements SearchQueryExecutorFactory {

  @Value("${org.obiba.opal.web.search.termsFacetSizeLimit}")
  private int termsFacetSizeLimit;

  @Autowired
  private ElasticSearchProvider esProvider;

  @Override
  public SearchQueryExecutor create() {
    return new EsSearchQueryExecutor(esProvider, termsFacetSizeLimit);
  }
}
