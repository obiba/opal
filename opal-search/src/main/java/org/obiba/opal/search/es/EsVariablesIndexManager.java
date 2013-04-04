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

import javax.annotation.Nonnull;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.Category;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.ValueTableVariablesIndex;
import org.obiba.opal.search.VariablesIndexManager;
import org.obiba.opal.search.es.mapping.AttributeMapping;
import org.obiba.opal.search.es.mapping.ValueTableVariablesMapping;
import org.obiba.runtime.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Component
public class EsVariablesIndexManager extends EsIndexManager implements VariablesIndexManager {

//  private static final Logger log = LoggerFactory.getLogger(EsVariablesIndexManager.class);

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  public EsVariablesIndexManager(ElasticSearchProvider esProvider, ElasticSearchConfigurationService esConfig,
      IndexManagerConfigurationService indexConfig, Version version) {
    super(esProvider, esConfig, indexConfig, version);
  }

  @Nonnull
  @Override
  public EsValueTableVariablesIndex getIndex(@Nonnull ValueTable vt) {
    return (EsValueTableVariablesIndex) super.getIndex(vt);
  }

  @Override
  protected ValueTableIndex createIndex(@Nonnull ValueTable vt) {
    return new EsValueTableVariablesIndex(vt);
  }

  @Override
  public boolean isIndexable(@Nonnull ValueTable valueTable) {
    return !getIndex(valueTable).isUpToDate();
  }

  @Nonnull
  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new Indexer(valueTable, (EsValueTableVariablesIndex) index);
  }

  @Nonnull
  @Override
  public String getName() {
    return esIndexName() + "-variables";
  }

  private class Indexer extends EsIndexer {

    private final EsValueTableVariablesIndex index;

    private Indexer(ValueTable table, EsValueTableVariablesIndex index) {
      super(table, index);
      this.index = index;
    }

    @Override
    protected void index() {
      BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

      int tableIndex = 0;
      for(Variable variable : valueTable.getVariables()) {
        bulkRequest = indexVariable(variable, bulkRequest, tableIndex++);
      }

      sendAndCheck(bulkRequest);
      index.updateTimestamps();
    }

    @Override
    protected XContentBuilder getMapping() {
      return new ValueTableVariablesMapping().createMapping(runtimeVersion, index.getIndexName(), valueTable);
    }

    private BulkRequestBuilder indexVariable(Variable variable, BulkRequestBuilder bulkRequest, int tableIndex) {
      String fullName = valueTable.getDatasource().getName() + "." + valueTable.getName() + ":" + variable.getName();
      try {
        XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
        xcb.field("datasource", valueTable.getDatasource().getName());
        xcb.field("table", valueTable.getName());
        xcb.field("fullName", fullName);
        xcb.field("index", tableIndex);
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

  private class EsValueTableVariablesIndex extends EsValueTableIndex implements ValueTableVariablesIndex {

    private EsValueTableVariablesIndex(ValueTable vt) {
      super(vt, "variables");
    }

    @Nonnull
    @Override
    public String getFieldName(@Nonnull Attribute attribute) {
      return AttributeMapping.getFieldName(attribute);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public String getFieldSortName(@Nonnull String field) {
      EsMapping.Properties properties = readMapping().properties();
      Map<String, Object> result = properties.getProperty(field);

      if(result == null || !"multi_field".equals(result.get("type")) || !result.containsKey("fields")) {
        return field;
      }

      Map<String, Object> fields = (Map<String, Object>) result.get("fields");
      for(String fieldKey : fields.keySet()) {
        if(!field.equals(fieldKey)) {
          // the field name get post-fixed by the un-analyzed mapping name (usually 'untouched')
          return field + "." + fieldKey;
        }
      }

      return field;
    }

  }
}
