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

import javax.validation.constraints.NotNull;

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
    defaultQueryFields.add("maelstrom*");
  }

  //
  // Private data members
  //

  private int from = DEFAULT_FROM;

  private int size = DEFAULT_SIZE;

  private Collection<String> fields;

  private Collection<String> facets;

  private Collection<String> filterTypes;

  private String query;

  private String sortField;

  private String sortDir;

  //
  // Public methods
  //

  public QuerySearchJsonBuilder() {
  }

  public QuerySearchJsonBuilder setFrom(int value) {
    from = value;
    return this;
  }

  public QuerySearchJsonBuilder setSize(int value) {
    size = value;
    return this;
  }

  public QuerySearchJsonBuilder setFields(@NotNull Collection<String> value) {
    fields = value;
    return this;
  }

  public QuerySearchJsonBuilder setFacets(@NotNull Collection<String> value) {
    facets = value;
    return this;
  }

  public QuerySearchJsonBuilder setSortField(@NotNull String value) {
    sortField = value;
    return this;
  }

  public QuerySearchJsonBuilder setSortDir(@NotNull String value) {
    sortDir = value.toLowerCase(); // elastic search accepts only lower case
    return this;
  }

  public QuerySearchJsonBuilder setQuery(@NotNull String value) {
    if(Strings.isNullOrEmpty(value)) {
      throw new IllegalArgumentException();
    }

    query = value;
    return this;
  }

  public QuerySearchJsonBuilder setFilterTypes(@NotNull Collection<String> value) {
    filterTypes = value;
    return this;
  }

  public JSONObject build() throws JSONException {
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.accumulate("query", new JSONObject().put("query_string", buildQueryStringJson()));
    jsonQuery.put("sort", buildSortJson());
    if(fields != null && !fields.isEmpty()) jsonQuery.put("partial_fields", buildFields());
    if(filterTypes != null && !filterTypes.isEmpty()) jsonQuery.put("filter", buildFilter());
    jsonQuery.put("from", from);
    jsonQuery.put("size", size);
    if (hasFacets()) jsonQuery.put("facets", buildFacetsJson());

    return jsonQuery;
  }

  //
  // Private members
  //

  private JSONObject buildQueryStringJson() throws JSONException {
    JSONObject json = new JSONObject();
    if(!hasFacets() && !"*".equals(query)) json.put("fields", new JSONArray(defaultQueryFields));
    json.put("query", query);
    json.put("default_operator", DEFAULT_QUERY_OPERATOR);

    return json;
  }

  private JSONObject buildSortJson() throws JSONException {
    if(Strings.isNullOrEmpty(sortField)) {
      return new JSONObject();
    }

    return new JSONObject().put(sortField, new JSONObject().put("order", sortDir));
  }

  private JSONObject buildFilter() throws JSONException {
    return new JSONObject().put("bool", new JSONObject()
        .put("must", new JSONObject().put("terms", new JSONObject().put("_type", new JSONArray(filterTypes)))));
  }


  private JSONObject buildFacetsJson() throws JSONException {
    JSONObject jsonFacets = new JSONObject();
    for (String facet : facets) {
      jsonFacets.accumulate(facet, new JSONObject().put("terms", new JSONObject().put("field", facet)));
    }

    return jsonFacets;
  }

  private JSONObject buildFields() throws JSONException {
    return new JSONObject().put("partial", new JSONObject().put("include", new JSONArray(fields)));
  }

  private boolean hasFacets() {
    return facets != null && !facets.isEmpty();
  }

}
