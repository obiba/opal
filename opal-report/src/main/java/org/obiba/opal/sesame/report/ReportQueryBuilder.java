/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.report;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.elmo.sesame.SesameManager;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ReportQueryBuilder {

  private static final Logger log = LoggerFactory.getLogger(ReportQueryBuilder.class);

  /**
   * Holds the name of the variable binding within the query (ie: select ?var {...} : the value is "?var")
   */
  private String variableBindingName = "?var";

  private int nextBindingId = 0;

  private Set<CharSequence> joinPatterns = new LinkedHashSet<CharSequence>();

  private Set<CharSequence> leftJoinPatterns = new LinkedHashSet<CharSequence>();;

  private Set<CharSequence> filters = new LinkedHashSet<CharSequence>();;

  private Map<CharSequence, Value> bindings = new LinkedHashMap<CharSequence, Value>();;

  public TupleQuery build(SesameManager manager) throws MalformedQueryException, RepositoryException {

    StringBuilder queryString = new StringBuilder();
    queryString.append("SELECT ?sid ?occ ?var ?value {_:entity opal:identifier ?sid . _:entity rdf:type opal:Participant . _:ds opal:isForEntity _:entity . ?varData opal:withinDataset _:ds . ?varData rdf:type ?var ");

    for(CharSequence seq : joinPatterns) {
      queryString.append(" . ").append(seq);
    }

    for(CharSequence seq : leftJoinPatterns) {
      queryString.append(" . OPTIONAL {").append(seq).append("}");
    }

    for(CharSequence seq : filters) {
      queryString.append(" . FILTER (").append(seq).append(")");
    }

    queryString.append(" . OPTIONAL { {?varData opal:dataValue ?value} UNION {?varData opal:hasCategory ?c . ?c rdf:type [ opal:code ?value ]} UNION {?varData rdf:type [ opal:code ?value ]}} } ORDER BY ?sid ?occ");

    QueryUtil.prefixQuery(manager.getConnection(), queryString);

    log.debug("Resulting query={}", queryString.toString());
    TupleQuery tq = manager.getConnection().prepareTupleQuery(queryString.toString());
    for(Map.Entry<CharSequence, Value> e : bindings.entrySet()) {
      tq.setBinding(e.getKey().toString(), e.getValue());
      log.debug("{}={}", e.getKey(), e.getValue());
    }
    log.debug("Query Plan\n{}", tq.toString());
    return tq;
  }

  public String getVariableBindingName() {
    return variableBindingName;
  }

  public String nextBinding() {
    return "tmp" + this.nextBindingId++;
  }

  public ReportQueryBuilder withOccurrence() {
    join("?varData opal:withinOccurrence [ opal:ordinal ?occ ; opal:withinDataset _:ds ]");
    return this;
  }

  public ReportQueryBuilder joinVariablePredicateValue(String predicate, Value value) {
    if(predicate == null) {
      throw new IllegalArgumentException("predicate cannot be null");
    }
    String bindingVar = nextBinding();
    StringBuilder sb = new StringBuilder(getVariableBindingName()).append(" ").append(predicate).append(" ?").append(bindingVar);
    join(sb);
    withBinding(bindingVar, value);
    return this;
  }

  public ReportQueryBuilder joinVariableCriteria(String criteria) {
    if(criteria == null) {
      throw new IllegalArgumentException("criteria cannot be null");
    }
    StringBuilder sb = new StringBuilder(getVariableBindingName()).append(" ").append(criteria);
    join(sb);
    return this;
  }

  public ReportQueryBuilder filterVariablePredicateValue(String predicate, Value value) {
    if(predicate == null) {
      throw new IllegalArgumentException("predicate cannot be null");
    }
    // Create a temp variable for testing if it is bound
    String tempVar = nextBinding();
    // Create a variable for replacing with the predicate value
    String valueVar = nextBinding();

    // ?varData rdf:type ?tmpVar . ?tmpVar <predicate> ?valueVar
    StringBuilder sb = new StringBuilder("?varData rdf:type ?").append(tempVar).append(" . ?").append(tempVar).append(" ").append(predicate).append(" ?").append(valueVar);
    leftJoin(sb);
    withBinding(valueVar, value);
    // Add a filter that will remove "rows" where ?tmpVar is non-null
    withFilter("!bound(?" + tempVar + ")");
    return this;
  }

  public ReportQueryBuilder filterVariableCriteria(String criteria) {
    if(criteria == null) {
      throw new IllegalArgumentException("criteria cannot be null");
    }
    // Create a temp variable for testing if it is bound
    String tempVar = nextBinding();

    // ?varData rdf:type ?tmpVar . ?tmpVar <criteria>
    StringBuilder sb = new StringBuilder("?varData rdf:type ?").append(tempVar).append(" . ?").append(tempVar).append(" ").append(criteria);
    leftJoin(sb);

    // Add a filter that will remove "rows" where ?tmpVar is non-null
    withFilter("!bound(?" + tempVar + ")");
    return this;
  }

  public ReportQueryBuilder join(CharSequence join) {
    if(join == null) {
      throw new IllegalArgumentException("pattern cannot be null");
    }
    joinPatterns.add(join);
    return this;
  }

  public ReportQueryBuilder leftJoin(CharSequence join) {
    if(join == null) {
      throw new IllegalArgumentException("pattern cannot be null");
    }
    leftJoinPatterns.add(join);
    return this;
  }

  public ReportQueryBuilder withPrefix(String prefix, String ns) {
    return this;
  }

  public ReportQueryBuilder withFilter(CharSequence filter) {
    if(filter == null) {
      throw new IllegalArgumentException("filter cannot be null");
    }
    filters.add(filter);
    return this;
  }

  public ReportQueryBuilder withBinding(CharSequence binding, Value value) {
    if(binding == null) {
      throw new IllegalArgumentException("binding cannot be null");
    }
    if(value == null) {
      throw new IllegalArgumentException("binding value cannot be null");
    }
    bindings.put(binding, value);
    return this;
  }

}
