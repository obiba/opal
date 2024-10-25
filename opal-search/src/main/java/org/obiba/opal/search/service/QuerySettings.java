/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Settings to be used to build a search query.
 */
public class QuerySettings {

  public static final Collection<String> defaultQueryFields = new ArrayList<>();

  private final static int DEFAULT_FROM = 0;

  private final static int DEFAULT_SIZE = 10;

  public final static String DEFAULT_QUERY_OPERATOR = "AND";

  static {
    defaultQueryFields.add("name.analyzed");
    defaultQueryFields.add("label*");
    defaultQueryFields.add("description*");
  }

  //
  // Private data members
  //

  private int from = DEFAULT_FROM;

  private int size = DEFAULT_SIZE;

  private Collection<String> fields;

  private Collection<String> facets;

  private Collection<String> filterReferences;

  private String query;

  private List<ChildQuery> childQueries = Lists.newArrayList();

  private String childQueryOperator = "must";

  private List<String> sortWithOrder = Lists.newArrayList();

  private boolean withDefaultQueryFields = true;

  private String lastDoc;

  //
  // Public methods
  //

  public QuerySettings() {
  }

  public static QuerySettings newSettings(String query) {
    QuerySettings settings = new QuerySettings();
    settings.query(query);
    return settings;
  }

  public int getFrom() {
    return from;
  }

  public QuerySettings from(int value) {
    from = value;
    return this;
  }

  public String getLastDoc() {
    return lastDoc;
  }

  public QuerySettings lastDoc(String value) {
    lastDoc = value;
    return this;
  }

  public int getSize() {
    return size;
  }

  public QuerySettings size(int value) {
    size = value;
    return this;
  }

  public boolean hasFields() {
    return fields != null && !fields.isEmpty();
  }

  public Collection<String> getFields() {
    return fields;
  }

  public QuerySettings fields(@NotNull Collection<String> value) {
    fields = value;
    return this;
  }

  public boolean hasFacets() {
    return facets != null && !facets.isEmpty();
  }

  public Collection<String> getFacets() {
    return facets;
  }

  public QuerySettings facets(@NotNull Collection<String> value) {
    facets = value;
    return this;
  }

  public boolean hasSort() {
    return !sortWithOrder.isEmpty();
  }

  public List<String> getSort() {
    return sortWithOrder;
  }

  public QuerySettings sortField(@NotNull String field, @NotNull String order) {
    sortWithOrder.add(field + ":" + order.toLowerCase());
    return this;
  }

  /**
   * A list of [field]:[order] strings (both tokens required).
   *
   * @param sortsWithOrder
   * @return
   */
  public QuerySettings sortWithOrder(List<String> sortsWithOrder) {
    if (sortsWithOrder != null) this.sortWithOrder.addAll(sortsWithOrder);
    return this;
  }

  public String getQuery() {
    return query;
  }

  public QuerySettings query(@NotNull String value) {
    if(Strings.isNullOrEmpty(value)) throw new IllegalArgumentException();
    query = value;
    return this;
  }

  public String getChildQueryOperator() {
    return childQueryOperator;
  }

  public void childQueryOperator(String name) {
    if (Strings.isNullOrEmpty(name) || !"or".equals(name.toLowerCase()))
      childQueryOperator = "must";
    else
      childQueryOperator = "should";
  }


  public boolean hasChildQueries() {
    return childQueries != null && !childQueries.isEmpty();
  }

  public List<ChildQuery> getChildQueries() {
    return childQueries;
  }

  public QuerySettings childQuery(@NotNull String type, @NotNull String value) {
    return childQuery(new ChildQuery(type, value));
  }

  public QuerySettings childQueries(List<ChildQuery> queries) {
    if (queries == null) return this;
    childQueries.addAll(queries);
    return this;
  }

  public QuerySettings childQuery(ChildQuery child) {
    childQueries.add(child);
    return this;
  }

  public boolean withDefaultFields() {
    return withDefaultQueryFields;
  }

  public QuerySettings noDefaultFields() {
    withDefaultQueryFields = false;
    return this;
  }

  public boolean hasFilterReferences() {
    return filterReferences != null && !filterReferences.isEmpty();
  }

  public Collection<String> getFilterReferences() {
    return filterReferences;
  }

  public QuerySettings filterReferences(@NotNull Collection<String> value) {
    filterReferences = value;
    return this;
  }

  public static class ChildQuery {
    private final String type;
    private final String query;

    public ChildQuery(String type, String query) {
      this.type = type;
      this.query = query;
    }

    public String getType() {
      return type;
    }

    public String getQuery() {
      return query;
    }
  }
}
