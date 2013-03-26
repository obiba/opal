/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.es;

import java.util.Set;

import javax.annotation.Nonnull;

import org.elasticsearch.common.base.Preconditions;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.SummariesIndexManager;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.ValueTableSummariesIndex;
import org.obiba.opal.search.es.mapping.VariableSummariesMapping;
import org.obiba.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EsSummariesIndexManager extends EsIndexManager implements SummariesIndexManager {

//  private static final Logger log = LoggerFactory.getLogger(EsSummariesIndexManager.class);

  private final Set<EsValueTableSummariesIndex> indices = Sets.newHashSet();

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  public EsSummariesIndexManager(ElasticSearchProvider esProvider, ElasticSearchConfigurationService esConfig,
      IndexManagerConfigurationService indexConfig, Version version) {
    super(esProvider, esConfig, indexConfig, version);
  }

  @Nonnull
  @Override
  public ValueTableSummariesIndex getIndex(@Nonnull ValueTable vt) {
    Preconditions.checkNotNull(vt);

    for(EsValueTableSummariesIndex index : indices) {
      if(index.isForTable(vt)) return index;
    }
    EsValueTableSummariesIndex index = new EsValueTableSummariesIndex(vt);
    indices.add(index);
    return index;
  }

  @Override
  public boolean isIndexable(@Nonnull ValueTable valueTable) {
    return !getIndex(valueTable).isUpToDate();
  }

  @Nonnull
  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new Indexer(valueTable, (EsValueTableSummariesIndex) index);
  }

  @Nonnull
  @Override
  public String getName() {
    return esIndexName() + "-variable-summaries";
  }

  private class Indexer extends EsIndexer {

    private final EsValueTableSummariesIndex index;

    private Indexer(ValueTable table, EsValueTableSummariesIndex index) {
      super(table, index);
      this.index = index;
    }

    @Override
    protected void index() {

      XContentBuilder builder = new VariableSummariesMapping()
          .createMapping(runtimeVersion, index.getIndexName(), valueTable);

    }

  }

  private class EsValueTableSummariesIndex extends EsValueTableIndex implements ValueTableSummariesIndex {

    private EsValueTableSummariesIndex(@Nonnull ValueTable vt) {
      super(vt);
    }

    @Override
    public String getFieldName(String variable) {
      return getIndexName() + "-" + variable;
    }

  }
}
