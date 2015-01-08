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

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;
import org.obiba.magma.concurrent.ConcurrentValueTableReader.ConcurrentReaderCallback;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.DateType;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.core.service.NotificationService;
import org.obiba.opal.core.service.ValidationService;
import org.obiba.opal.core.service.VariableSummaryService;
import org.obiba.opal.core.support.MessageLogger;
import org.obiba.opal.core.support.Slf4jMessageAdapter;
import org.obiba.opal.search.*;
import org.obiba.opal.search.es.mapping.ValueTableMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

@Component
@Transactional(readOnly = true)
public class EsValuesIndexManager extends EsIndexManager implements ValuesIndexManager {

  private static final Logger log = LoggerFactory.getLogger(EsValuesIndexManager.class);

  @Autowired
  private ThreadFactory threadFactory;

  @Autowired
  private VariableSummaryService variableSummaryService;

  @Autowired
  private ValidationService validationService;

  @Autowired
  private NotificationService notificationService;

  private MessageCollector messageCollector = new MessageCollector();

  @NotNull
  @Override
  public EsValueTableValuesIndex getIndex(@NotNull ValueTable vt) {
    return (EsValueTableValuesIndex) super.getIndex(vt);
  }

  @Override
  protected ValueTableIndex createIndex(@NotNull ValueTable vt) {
    return new EsValueTableValuesIndex(vt);
  }

  @NotNull
  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new Indexer(valueTable, (EsValueTableValuesIndex) index);
  }

  @Override
  public List<Message> getMessages(@NotNull ValueTable valueTable) {
    return messageCollector.getMessages(valueTable);
  }

  private MessageCollector.Task createCollectorTask(ValueTable valueTable) {
      return messageCollector.createTask(valueTable);
  }

  @NotNull
  @Override
  public String getName() {
    return esIndexName() + "-values";
  }

    /**
     * Checks if a notification is due for the indexing failure, and if so sends it.
     * @param valueTable
     * @param collectorTask
     */
  private void checkIndexFailureNotification(ValueTable valueTable, MessageCollector.Task collectorTask) {

    if (notificationService.isNotificationEnabled()) {
        String subject = String.format("Indexing failed for table %s", valueTable.getTableReference());
        String text = getNotificationEmailText(collectorTask);

        //@todo make from address configurable
        notificationService.sendNotification(subject, text);
    }
  }

  static String getNotificationEmailText(MessageCollector.Task collectorTask) {
    StringBuilder sb = new StringBuilder();
    for (Message msg: collectorTask.getErrorMessages()) {
        sb.append(msg.toString());
        sb.append("/n");
    }
    return sb.toString();
  }

    private class Indexer extends EsIndexer {

    private final EsValueTableValuesIndex index;

    private Indexer(ValueTable table, EsValueTableValuesIndex index) {
      super(table, index);
      this.index = index;
    }

    @Override
    protected void index() {
      MessageCollector.Task collectorTask = createCollectorTask(valueTable);
      ConcurrentReaderCallback callback = new ValuesReaderCallback();

      MessageLogger logger = new Slf4jMessageAdapter(log);
      ConcurrentReaderCallback validatingCallback = validationService.createValidatingCallback(valueTable, callback, logger);
      if (validatingCallback != null) {
        //if validation is due, use the validating callback instead
        callback = validatingCallback;
      }
      boolean success = false;

      try {
          ConcurrentValueTableReader.Builder builder =
                ConcurrentValueTableReader.Builder.newReader() //
                  .withThreads(threadFactory) //
                  .from(valueTable) //
                  .variablesFilter(index.getVariables()) //
                  .to(callback); //

          if (validatingCallback == null) {
              //this was on the original code, but its not desirable for validation
              builder.ignoreReadErrors();
          }

          collectorTask.info("Indexing table %s", valueTable.getTableReference());
          builder.build().read();
          collectorTask.info("Table successfully indexed");
          success = true;

      } catch (RuntimeException ex) {
          collectorTask.error("Failure indexing table: %s", ex.getMessage());
          throw ex;
      } finally {
          //making sure messages are kept
          collectorTask.close();
          if (!success) {
              //checking if a failure notification is due
              checkIndexFailureNotification(valueTable, collectorTask);
          }
      }
    }

    private class ValuesReaderCallback implements ConcurrentReaderCallback {

      private BulkRequestBuilder bulkRequest = opalSearchService.getClient().prepareBulk();

      private final Map<Variable, VariableNature> natures = new HashMap<>();

      private final Stopwatch stopwatch = Stopwatch.createUnstarted();

      @Override
      public void onBegin(List<VariableEntity> entitiesToCopy, Variable... variables) {
        stopwatch.start();
        for(Variable variable : variables) {
          natures.put(variable, VariableNature.getNature(variable));
        }
      }

      @Override
      public void onValues(VariableEntity entity, Variable[] variables, Value... values) {
        if(stop) {
          return;
        }

        String identifier = entity.getIdentifier();
        bulkRequest.add(opalSearchService.getClient() //
            .prepareIndex(getName(), valueTable.getEntityType(), identifier) //
            .setSource("{\"identifier\":\"" + identifier + "\"}"));
        try {
          XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
          for(int i = 0; i < variables.length; i++) {
            indexValue(builder, variables[i], values[i]);
          }
          builder.endObject();

          IndexRequestBuilder requestBuilder = opalSearchService.getClient()
              .prepareIndex(getName(), index.getIndexName(), identifier).setParent(identifier).setSource(builder);
          bulkRequest.add(requestBuilder);
          done++;
          if(bulkRequest.numberOfActions() >= ES_BATCH_SIZE) {
            bulkRequest = sendAndCheck(bulkRequest);
          }
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }

      private void indexValue(XContentBuilder xcb, Variable variable, Value value) throws IOException {
        String fieldName = index.getFieldName(variable.getName());
        if(value.isSequence() && !value.isNull()) {
          List<Object> values = Lists.newArrayList();
          for(Value v : value.asSequence().getValue()) {
            values.add(esValue(variable, v));
          }
          xcb.field(fieldName, values);
        } else {
          xcb.field(fieldName, esValue(variable, value));
        }
        variableSummaryService.stackVariable(getValueTable(), variable, value);
      }

      @Override
      public void onComplete() {
        stopwatch.stop();
        if(stop) {
          index.delete();
          variableSummaryService.clearComputingSummaries(getValueTable());
        } else {
          sendAndCheck(bulkRequest);
          index.updateTimestamps();
          log.info("Indexed table {} in {}", getValueTable().getTableReference(), stopwatch);

          // compute summaries in a new thread
          new Thread(new Runnable() {
            @Override
            public void run() {
              variableSummaryService.computeSummaries(getValueTable());
            }
          }).start();
        }
      }

      @Override
      public boolean isCancelled() {
        return stop;
      }

      /**
       * OPAL-1158: missing values are indexed as null for continuous variables
       *
       * @param variable the variable
       * @param value the value
       * @return an object
       */
      @Nullable
      private Object esValue(Variable variable, Value value) {
        switch(natures.get(variable)) {
          case CONTINUOUS:
            if(variable.isMissingValue(value)) {
              return null;
            }
        }
        if(value.isNull()) return null;
        Object obj = value.getValue();
        if(value.getValueType() == DateType.get()) {
          return obj.toString(); // ie MagmaDate.toString()
        }
        return obj;
      }
    }

  }

  private class EsValueTableValuesIndex extends EsValueTableIndex implements ValueTableValuesIndex {

    private EsValueTableValuesIndex(ValueTable vt) {
      super(vt);
    }

    @Override
    public String getFieldName(String variable) {
      return getIndexName() + "-" + variable;
    }

    @Override
    protected XContentBuilder getMapping() {
      return new ValueTableMapping().createMapping(runtimeVersionProvider.getVersion(), getIndexName(), resolveTable());
    }

    @Override
    public Iterable<Variable> getVariables() {
      // Do not index binary values, do not even extract the binary values
      // TODO Could be configurable at table level?
      return Iterables.filter(resolveTable().getVariables(), new Predicate<Variable>() {

        @Override
        public boolean apply(Variable input) {
          return !input.getValueType().isGeo() && !input.getValueType().equals(BinaryType.get());
        }

      });
    }

  }
}
