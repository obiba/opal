/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.obiba.opal.search.service.QuerySettings;
import org.obiba.opal.search.service.SearchException;
import org.obiba.opal.search.service.SearchQueryExecutor;
import org.obiba.opal.search.service.support.ScoreDocSerializer;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class ContentQueryExecutor implements SearchQueryExecutor {

  private static final Logger log = LoggerFactory.getLogger(ContentQueryExecutor.class);

  private final Set<String> defaultFields;

  private final Directory directory;

  private final Analyzer analyzer;

  public ContentQueryExecutor(Directory directory, Set<String> defaultFields, Analyzer analyzer) {
    this.directory = directory;
    this.defaultFields = defaultFields;
    this.analyzer = analyzer;
  }

  @Override
  public Search.QueryResultDto execute(QuerySettings querySettings) throws SearchException {
    // Create an IndexReader to access the index
    try (IndexReader reader = DirectoryReader.open(directory);) {
      // Create an IndexSearcher to perform the search
      IndexSearcher searcher = new IndexSearcher(reader);

      // Case no tables
      if (!querySettings.hasFilterReferences()) {
        return Search.QueryResultDto.newBuilder().setTotalHits(0).build();
      }

      // Make query
      Query query = makeQuery(querySettings);
      String lastDoc = querySettings.getLastDoc();
      ScoreDoc lastScoreDoc = Strings.isNullOrEmpty(lastDoc) ? null : ScoreDocSerializer.deserialize(lastDoc);

      // Search for the top results
      TopDocs results = searcher.searchAfter(lastScoreDoc, query, querySettings.getSize());
      ScoreDoc[] hits = results.scoreDocs;

      // Build results
      Search.QueryResultDto.Builder builder = Search.QueryResultDto.newBuilder().setTotalHits((int) results.totalHits.value);
      if (hits.length > 0) builder.setLastDoc(ScoreDocSerializer.serialize(hits[hits.length - 1]));

      StoredFields storedFields = reader.storedFields();
      for (ScoreDoc hit : hits) {
        Document doc = storedFields.document(hit.doc);
        log.debug("Document hit: {}", doc);
        String identifier = doc.get("id");
        Search.ItemResultDto.Builder resHit = Search.ItemResultDto.newBuilder().setIdentifier(identifier);
        Search.ItemFieldsDto.Builder resFields = Search.ItemFieldsDto.newBuilder();
        for (String field : Sets.union(Sets.newHashSet(querySettings.getFields()), defaultFields)) {
          IndexableField idxField = doc.getField(field);
          if (idxField != null)
            resFields.addFields(Opal.EntryDto.newBuilder().setKey(field).setValue(idxField.stringValue()).build());
        }
        resHit.setExtension(Search.ItemFieldsDto.item, resFields.build());
        builder.addHits(resHit);
      }
      return builder.build();
    } catch (IOException e) {
      throw new SearchException("Tables index access failure", e);
    } catch (ParseException e) {
      if (log.isTraceEnabled())
        log.warn("Wrong search query syntax", e);
      else
        log.warn("Wrong search query syntax: {}", e.getMessage());
      return Search.QueryResultDto.newBuilder().setTotalHits(0).build();
    }
  }

  @Override
  public Search.QueryCountDto count(QuerySettings querySettings) throws SearchException {
    // Create an IndexReader to access the index
    try (IndexReader reader = DirectoryReader.open(directory);) {
      // Create an IndexSearcher to perform the search
      IndexSearcher searcher = new IndexSearcher(reader);

      // Case no tables
      if (!querySettings.hasFilterReferences()) {
        return Search.QueryCountDto.newBuilder().setTotalHits(0).build();
      }

      // Make query
      Query query = makeQuery(querySettings);
      Search.QueryCountDto.Builder builder = Search.QueryCountDto.newBuilder().setTotalHits(searcher.count(query));
      return builder.build();
    } catch (IOException e) {
      throw new SearchException("Tables index access failure", e);
    } catch (ParseException e) {
      if (log.isTraceEnabled())
        log.warn("Wrong search query syntax", e);
      else
        log.warn("Wrong search query syntax: {}", e.getMessage());
      return Search.QueryCountDto.newBuilder().setTotalHits(0).build();
    }
  }

  private Query makeQuery(QuerySettings querySettings) throws ParseException {
    // Build a QueryParser
    QueryParser parser = new QueryParser("content", analyzer);

    // Parse a query
    Query query = parser.parse(querySettings.getQuery());
    if (querySettings.hasFilterReferences()) {
      // at least one table-ref must match
      BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
      for (String tableRef : querySettings.getFilterReferences()) {
        queryBuilder.add(new TermQuery(new Term("table-ref", tableRef)), BooleanClause.Occur.SHOULD);
      }
      Query termsQuery = queryBuilder.build();

      queryBuilder = new BooleanQuery.Builder();
      queryBuilder.add(new BooleanClause(query, BooleanClause.Occur.MUST));
      queryBuilder.add(new BooleanClause(termsQuery, BooleanClause.Occur.MUST));
      query = queryBuilder.build();
    }
    return query;
  }
}
