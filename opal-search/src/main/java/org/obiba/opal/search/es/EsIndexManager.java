/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.es;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.indices.IndexMissingException;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;
import org.obiba.magma.concurrent.ConcurrentValueTableReader.ConcurrentReaderCallback;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.LocaleType;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.runtime.security.BackgroundJobServiceAuthToken;
import org.obiba.opal.search.IndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EsIndexManager implements IndexManager {

  private static final Logger log = LoggerFactory.getLogger(EsIndexManager.class);

  private final ElasticSearchProvider esProvider;

  private final ThreadFactory threadFactory;

  private final Set<ValueTableIndex> indices = Sets.newHashSet();

  private final Indexer indexer = new Indexer();

  @Autowired
  public EsIndexManager(ElasticSearchProvider esProvider, ThreadFactory threadFactory) {
    this.esProvider = esProvider;
    this.threadFactory = threadFactory;

  }

  private Subject getSubject() {
    // Login as background job user
    try {
      PrincipalCollection principals = SecurityUtils.getSecurityManager().authenticate(new BackgroundJobServiceAuthToken()).getPrincipals();
      return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
    } catch(Exception e) {
      return null;
    }
  }

  // Every ten seconds
  @Scheduled(fixedDelay = 10 * 1000)
  public void synchronizeIndices() {
    getSubject().execute(new Runnable() {

      @Override
      public void run() {
        for(Datasource ds : MagmaEngine.get().getDatasources()) {
          for(ValueTable vt : ds.getValueTables()) {
            if(getIndex(vt).isUpToDate(vt) == false) {
              indexer.update(vt, getIndex(vt));
            }
          }
        }
      }
    });
  }

  private ValueTableIndex getIndex(ValueTable vt) {
    for(ValueTableIndex i : this.indices) {
      if(i.isForTable(vt)) return i;
    }
    ValueTableIndex i = new ValueTableIndex(vt);
    this.indices.add(i);
    return i;
  }

  private String indexName(ValueTable vt) {
    return tableReference(vt).toLowerCase().replaceAll(" ", "_");
  }

  private String tableReference(ValueTable vt) {
    return vt.getDatasource().getName() + "." + vt.getName();
  }

  private class Indexer {

    public void update(ValueTable vt, ValueTableIndex index) {
      log.info("Updating ValueTable index {}", index.valueTableReference);
      try {
        esProvider.getClient().admin().indices().prepareDeleteMapping("opal").setType(index.name).execute().actionGet();
      } catch(IndexMissingException e) {
        esProvider.getClient().admin().indices().prepareCreate("opal").execute().actionGet();
      }
      index(vt, index);
    }

    private String esTypeForValueType(ValueType type) {
      if(TextType.get() == type) {
        return "string";
      } else if(IntegerType.get() == type) {
        return "long";
      } else if(DecimalType.get() == type) {
        return "double";
      } else if(BooleanType.get() == type) {
        return "boolean";
      } else if(DateType.get() == type || DateTimeType.get() == type) {
        return "date";
      } else if(LocaleType.get() == type) {
        return "string";
      }
      throw new IllegalArgumentException("unknown type");
    }

    private XContentBuilder createMapping(String name, Iterable<Variable> variables) {
      try {
        XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject(name);
        mapping.startObject("_all").field("enabled", false).endObject().startObject("_parent").field("type", "Participant").endObject();
        mapping.startObject("properties");
        for(Variable variable : variables) {
          mapping.startObject(variable.getName()).field("type", esTypeForValueType(variable.getValueType())).field("store", "yes").endObject();
        }

        mapping.endObject().endObject().endObject();
        log.info("mapping: {}", mapping.string());
        return mapping;
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }

    private void index(final ValueTable valueTable, final ValueTableIndex index) {

      XContentBuilder b = createMapping(index.name, valueTable.getVariables());

      esProvider.getClient().admin().indices().preparePutMapping("opal").setSource(b).execute().actionGet();

      ConcurrentValueTableReader.Builder.newReader().withThreads(threadFactory).from(valueTable).to(new ConcurrentReaderCallback() {

        BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

        @Override
        public void onValues(VariableEntity entity, Variable[] variables, Value[] values) {
          bulkRequest.add(esProvider.getClient().prepareIndex("opal", valueTable.getEntityType(), entity.getIdentifier()).setSource("{\"identifier\":\"" + entity.getIdentifier() + "\"}"));
          try {
            XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
            for(int i = 0; i < variables.length; i++) {
              xcb.field(variables[i].getName(), values[i].getValue());
            }
            bulkRequest.add(esProvider.getClient().prepareIndex("opal", index.name, entity.getIdentifier()).setParent(entity.getIdentifier()).setSource(xcb.endObject()));
            if(bulkRequest.numberOfActions() >= 100) {
              BulkResponse bulkResponse = bulkRequest.execute().actionGet();
              if(bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item
                throw new RuntimeException(bulkResponse.buildFailureMessage());
              }
              bulkRequest = esProvider.getClient().prepareBulk();
            }
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void onComplete() {
          index.upToDate = true;
        }

        @Override
        public void onBegin() {
        }

      }).build().read();

    }
  }

  private class ValueTableIndex {

    private final String name;

    private final String valueTableReference;

    protected boolean upToDate;

    private ValueTableIndex(ValueTable vt) {
      this.name = indexName(vt);
      this.valueTableReference = tableReference(vt);
    }

    boolean isForTable(ValueTable valueTable) {
      return valueTableReference.equals(tableReference(valueTable));
    }

    boolean isUpToDate(Timestamped timestamped) {
      return upToDate;
    }

    @Override
    public boolean equals(Object obj) {
      return ((ValueTableIndex) obj).name.equals(this.name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }

  }
}
