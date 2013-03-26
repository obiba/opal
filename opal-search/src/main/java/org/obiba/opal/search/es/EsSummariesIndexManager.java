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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.common.base.Preconditions;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.SummariesIndexManager;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.ValueTableSummariesIndex;
import org.obiba.opal.search.es.mapping.VariableSummariesMapping;
import org.obiba.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
      // do nothing as we calculate summaries on values indexation (EsValuesIndexManager)
    }

    @Override
    protected XContentBuilder getMapping() {
      return new VariableSummariesMapping().createMapping(runtimeVersion, index.getIndexName(), valueTable);
    }

    @Override
    protected BulkRequestBuilder sendAndCheck(BulkRequestBuilder bulkRequest) {
      compute(bulkRequest);
      return super.sendAndCheck(bulkRequest);
    }

    // TODO do we need to do this async? Will it be long?
    private void compute(BulkRequestBuilder bulkRequest) {
      String variablePath = valueTable.getDatasource().getName() + "." + valueTable.getName();
      index.compute(variablePath, bulkRequest);
    }

  }

  private class EsValueTableSummariesIndex extends EsValueTableIndex implements ValueTableSummariesIndex {

    private final Map<String, CategoricalVariableSummary.Builder> categoricalSummaryBuilders = Maps.newHashMap();

    private EsValueTableSummariesIndex(@Nonnull ValueTable vt) {
      super(vt);
    }

    @Override
    public String getFieldName(String variable) {
      return getIndexName() + "-" + variable;
    }

    @Override
    public void indexVariable(@Nonnull Variable variable, @Nonnull Value value) {

      // skip binary variable
      if(variable.getValueType().equals(BinaryType.get())) {
        throw new RuntimeException("Cannot compute summary for binary variable " + variable.getName());
      }

      CategoricalVariableSummary.Builder builder = categoricalSummaryBuilders.get(variable.getName());
      if(builder == null) {
        // TODO distinct ?
        boolean distinct = TextType.get().equals(variable.getValueType()) && variable.areAllCategoriesMissing();
        builder = new CategoricalVariableSummary.Builder(variable).distinct(distinct);
      }
      builder.addValue(value);
    }

    private void compute(String variablePath, BulkRequestBuilder bulkRequest) {
      try {
        CategoricalVariableSummary summary = null;
        List<String> values = Lists.newArrayList();
        List<Long> freqs = Lists.newArrayList();
        List<Double> pcts = Lists.newArrayList();

        for(CategoricalVariableSummary.Builder builder : categoricalSummaryBuilders.values()) {
          summary = builder.build();
          values.clear();
          freqs.clear();
          pcts.clear();
          for(CategoricalVariableSummary.Frequency frequency : summary.getFrequencies()) {
            values.add(frequency.getValue());
            freqs.add(frequency.getFreq());
            pcts.add(frequency.getPct());
          }

          XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject() //
              .field("mode", summary.getMode()) //
              .field("n", summary.getN()) //
              .field("frequency-value", values) //
              .field("frequency-freq", freqs) //
              .field("frequency-pct", pcts) //
              .endObject();

          String variableFullName = variablePath + ":" + summary.getVariable().getName();
          bulkRequest.add(esProvider.getClient().prepareIndex(getName(), getIndexName(), variableFullName)
              .setSource(contentBuilder));
        }
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

  }
}
