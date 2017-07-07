/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Strings;

public class QuerySearchJsonBuilder {

  private static final Collection<String> defaultQueryFields = new ArrayList<>();

  private final static int DEFAULT_FROM = 0;

  private final static int DEFAULT_SIZE = 10;

  private final static String DEFAULT_QUERY_OPERATOR = "AND";

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

  private String sortField;

  private String sortDir;

  private boolean withDefaultQueryFields = true;

  //
  // Public methods
  //

  public QuerySearchJsonBuilder() {
  }

  public QuerySearchJsonBuilder from(int value) {
    from = value;
    return this;
  }

  public QuerySearchJsonBuilder size(int value) {
    size = value;
    return this;
  }

  public QuerySearchJsonBuilder fields(@NotNull Collection<String> value) {
    fields = value;
    return this;
  }

  public QuerySearchJsonBuilder facets(@NotNull Collection<String> value) {
    facets = value;
    return this;
  }

  public QuerySearchJsonBuilder sortField(@NotNull String value) {
    sortField = value;
    return this;
  }

  public QuerySearchJsonBuilder sortDir(@NotNull String value) {
    sortDir = value.toLowerCase(); // elastic search accepts only lower case
    return this;
  }

  public QuerySearchJsonBuilder query(@NotNull String value) {
    if(Strings.isNullOrEmpty(value)) throw new IllegalArgumentException();
    query = value;
    return this;
  }

  public QuerySearchJsonBuilder childQuery(@NotNull String type, @NotNull String value) {
    return childQuery(new ChildQuery(type, value));
  }

  public QuerySearchJsonBuilder childQueries(List<ChildQuery> queries) {
    if (queries == null) return this;
    childQueries.addAll(queries);
    return this;
  }

  public QuerySearchJsonBuilder childQuery(ChildQuery child) {
    childQueries.add(child);
    return this;
  }

  public QuerySearchJsonBuilder noDefaultFields() {
    withDefaultQueryFields = false;
    return this;
  }

  public QuerySearchJsonBuilder filterReferences(@NotNull Collection<String> value) {
    filterReferences = value;
    return this;
  }

  public JSONObject build() throws JSONException {
    JSONObject jsonQuery = new JSONObject();
    if (childQueries.isEmpty())
      jsonQuery.put("query", buildQueryString(query, withDefaultQueryFields));
    else
      jsonQuery.put("query", buildHasChildQueries());
    jsonQuery.put("sort", buildSortJson());
    if(fields != null && !fields.isEmpty()) jsonQuery.put("_source", buildFields());
    if(filterReferences != null && !filterReferences.isEmpty()) jsonQuery.put("filter", buildFilter());
    jsonQuery.put("from", from);
    jsonQuery.put("size", size);
    if (hasFacets()) jsonQuery.put("facets", buildFacetsJson());

    return jsonQuery;
  }

  //
  // Private members
  //

  private JSONObject buildQueryString(String query, boolean defaultFields) throws JSONException {
    JSONObject json = new JSONObject();
    if(defaultFields && !hasFacets() && !"*".equals(query)) json.put("fields", new JSONArray(defaultQueryFields));
    json.put("query", query);
    json.put("default_operator", DEFAULT_QUERY_OPERATOR);
    return new JSONObject().put("query_string", json);
  }

  private JSONObject buildHasChildQueries() throws JSONException {
    JSONObject json = new JSONObject();
    for (ChildQuery child : childQueries) json.accumulate(childQueryOperator, buildHasChildQuery(child));
    return new JSONObject().put("bool", json);
  }

  private JSONObject buildHasChildQuery(ChildQuery child) throws JSONException {
    JSONObject json = new JSONObject();
    json.put("type", child.type);
    json.put("query", buildQueryString(child.query, false));
    return new JSONObject().put("has_child", json);
  }

  private JSONObject buildSortJson() throws JSONException {
    if(Strings.isNullOrEmpty(sortField)) {
      return new JSONObject();
    }

    return new JSONObject().put(sortField, new JSONObject().put("order", sortDir));
  }

  private JSONObject buildFilter() throws JSONException {
    return new JSONObject().put("terms", new JSONObject().put("reference", new JSONArray(filterReferences)));
  }


  private JSONObject buildFacetsJson() throws JSONException {
    JSONObject jsonFacets = new JSONObject();
    for (String facet : facets) {
      String field = facet;
      int size = 10;
      int idx = facet.lastIndexOf(":");
      if (idx>0) {
        field = facet.substring(0, idx);
        try {
          size = Integer.parseInt(facet.substring(idx+1));
        } catch (NumberFormatException e) {
          // ignore
        }
      }
      jsonFacets.accumulate(facet, new JSONObject().put("terms", new JSONObject().put("field", field).put("size", size)));
    }

    return jsonFacets;
  }

  private JSONObject buildFields() throws JSONException {
    return new JSONObject().put("includes", new JSONArray(fields));
  }

  private boolean hasFacets() {
    return facets != null && !facets.isEmpty();
  }

  public void childQueryOperator(String name) {
    if (Strings.isNullOrEmpty(name) || !"or".equals(name.toLowerCase()))
      childQueryOperator = "must";
    else
      childQueryOperator = "should";
  }

  public static class ChildQuery {
    private final String type;
    private final String query;

    public ChildQuery(String type, String query) {
      this.type = type;
      this.query = query;
    }
  }
}
