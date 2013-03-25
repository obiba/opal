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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.common.base.Preconditions;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.Category;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.SummariesIndexManager;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.ValueTableSummariesIndex;
import org.obiba.opal.search.es.mapping.AttributeMapping;
import org.obiba.opal.search.es.mapping.ValueTableVariablesMapping;
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

      XContentBuilder b = new ValueTableVariablesMapping()
          .createMapping(runtimeVersion, index.getIndexName(), valueTable);
      esProvider.getClient().admin().indices().preparePutMapping(getName()).setType(index.getIndexName()).setSource(b)
          .execute().actionGet();

      BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

      String fullNamePrefix = valueTable.getDatasource().getName() + "." + valueTable.getName();
      for(Variable variable : valueTable.getVariables()) {
        bulkRequest = indexVariable(bulkRequest, fullNamePrefix, variable);
      }

      sendAndCheck(bulkRequest);
      index.updateTimestamps();
    }

    private BulkRequestBuilder indexVariable(BulkRequestBuilder bulkRequest, String fullNamePrefix, Variable variable) {
      String fullName = fullNamePrefix + ":" + variable.getName();
      try {
        XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
        xcb.field("datasource", valueTable.getDatasource().getName());
        xcb.field("table", valueTable.getName());
        xcb.field("fullName", fullName);
        indexVariableParameters(variable, xcb);

        if(variable.hasAttributes()) {
          indexVariableAttributes(variable, xcb);
        }

        if(variable.hasCategories()) {
          indexVariableCategories(variable, xcb);
        }

        bulkRequest.add(
            esProvider.getClient().prepareIndex(getName(), index.getIndexName(), fullName).setSource(xcb.endObject()));
        if(bulkRequest.numberOfActions() >= ES_BATCH_SIZE) {
          return sendAndCheck(bulkRequest);
        }
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
      return bulkRequest;
    }

    private void indexVariableParameters(Variable variable, XContentBuilder xcb) throws IOException {
      xcb.field("name", variable.getName());
      xcb.field("entityType", variable.getEntityType());
      xcb.field("valueType", variable.getValueType().getName());
      xcb.field("occurrenceGroup", variable.getOccurrenceGroup());
      xcb.field("repeatable", variable.isRepeatable());
      xcb.field("mimeType", variable.getMimeType());
      xcb.field("unit", variable.getUnit());
      xcb.field("referencedEntityType", variable.getReferencedEntityType());
    }

    private void indexVariableAttributes(AttributeAware variable, XContentBuilder xcb) throws IOException {
      for(Attribute attribute : variable.getAttributes()) {
        if(!attribute.getValue().isNull()) {
          xcb.field(AttributeMapping.getFieldName(attribute), attribute.getValue());
        }
      }
    }

    private void indexVariableCategories(Variable variable, XContentBuilder xcb) throws IOException {
      List<String> names = Lists.newArrayList();
      Map<String, List<Object>> attributeFields = Maps.newHashMap();
      for(Category category : variable.getCategories()) {
        names.add(category.getName());
        if(category.hasAttributes()) {
          for(Attribute attribute : category.getAttributes()) {
            String field = "category-" + AttributeMapping.getFieldName(attribute);
            if(!attributeFields.containsKey(field)) {
              attributeFields.put(field, new ArrayList<Object>());
            }
            attributeFields.get(field).add(attribute.getValue().getValue());
          }
        }
      }
      xcb.field("category", names);
      for(String field : attributeFields.keySet()) {
        xcb.field(field, attributeFields.get(field));
      }
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
