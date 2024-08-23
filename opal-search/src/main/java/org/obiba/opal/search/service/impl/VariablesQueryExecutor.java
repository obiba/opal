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

import com.google.common.collect.Sets;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.obiba.opal.search.service.QuerySettings;
import org.obiba.opal.search.service.SearchException;
import org.obiba.opal.search.service.SearchQueryExecutor;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class VariablesQueryExecutor implements SearchQueryExecutor {

  private static final Logger log = LoggerFactory.getLogger(VariablesQueryExecutor.class);

  private final Set<String> DEFAULT_FIELDS = Set.of("name", "project", "table", "valueType", "entityType");

  private final Directory directory;

  public VariablesQueryExecutor(Directory directory) {
    this.directory = directory;
  }

  @Override
  public Search.QueryResultDto execute(QuerySettings querySettings) throws SearchException {
    // Create an IndexReader to access the index
    try (IndexReader reader = DirectoryReader.open(directory);) {
      // Create an IndexSearcher to perform the search
      IndexSearcher searcher = new IndexSearcher(reader);

      // Build a QueryParser
      Analyzer analyzer = AnalyzerFactory.newVariablesAnalyzer();
      QueryParser parser = new QueryParser("name", analyzer);

      // Parse a query (search for books with "Lucene" in the title)
      Query query = parser.parse(querySettings.getQuery());

      // Search for the top results
      TopDocs results = searcher.search(query, querySettings.getSize());
      ScoreDoc[] hits = results.scoreDocs;

      // Build results
      Search.QueryResultDto.Builder builder = Search.QueryResultDto.newBuilder().setTotalHits(hits.length);
      StoredFields storedFields = reader. storedFields();
      for (ScoreDoc hit : hits) {
        Document doc = storedFields. document(hit. doc);
        log.debug("Document hit: {}", doc);
        String identifier = doc.get("fullName");
        Search.ItemResultDto.Builder resHit = Search.ItemResultDto.newBuilder().setIdentifier(identifier);
        Search.ItemFieldsDto.Builder resFields = Search.ItemFieldsDto.newBuilder();
        for (String field : Sets.union(Sets.newHashSet(querySettings.getFields()), DEFAULT_FIELDS)) {
          IndexableField idxField = doc.getField(field);
          if (idxField != null)
            resFields.addFields(Opal.EntryDto.newBuilder().setKey(field).setValue(idxField.stringValue()).build());
        }
        resHit.setExtension(Search.ItemFieldsDto.item, resFields.build());
        builder.addHits(resHit);
      }
      return builder.build();
    } catch (IOException e) {
      throw new SearchException("Variables index access failure", e);
    } catch (ParseException e) {
      if (log.isTraceEnabled())
        log.warn("Wrong search query syntax", e);
      else
        log.warn("Wrong search query syntax: {}", e.getMessage());
      return Search.QueryResultDto.newBuilder().setTotalHits(0).build();
    }
  }
}
