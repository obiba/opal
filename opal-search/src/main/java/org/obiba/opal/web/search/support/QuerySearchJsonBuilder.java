/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
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

import javax.annotation.Nonnull;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Strings;

public class QuerySearchJsonBuilder {

  private static final Collection<String> defaultQueryFields = new ArrayList<String>();

  private final static int DEFAULT_FROM = 0;

  private final static int DEFAULT_SIZE = 10;

  static {
    defaultQueryFields.add("name");
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

  public QuerySearchJsonBuilder setFields(@Nonnull Collection<String> value) {
    fields = value;
    return this;
  }

  public QuerySearchJsonBuilder setSortField(@Nonnull String value) {
    sortField = value;
    return this;
  }

  public QuerySearchJsonBuilder setSortDir(@Nonnull String value) {
    sortDir = value.toLowerCase(); // elastic search accepts only lower case
    return this;
  }

  public QuerySearchJsonBuilder setQuery(@Nonnull String value) {
    if(Strings.isNullOrEmpty(value)) {
      throw new IllegalArgumentException();
    }

    query = value;
    return this;
  }

  public QuerySearchJsonBuilder setFilterTypes(@Nonnull Collection<String> value) {
    filterTypes = value;
    return this;
  }

  public JSONObject build() throws JSONException {
    JSONObject jsonQuery = new JSONObject();
    jsonQuery.accumulate("query", new JSONObject().put("query_string", buildQueryStringJson()));
    jsonQuery.put("sort", buildSortJson());
    if (fields != null && fields.size() > 0) jsonQuery.put("fields", new JSONArray(fields));
    if (filterTypes != null && filterTypes.size() > 0) jsonQuery.put("filter", buildFilter());
    jsonQuery.put("from", from);
    jsonQuery.put("size", size);

    return jsonQuery;
  }

  //
  // Private members
  //

  private JSONObject buildQueryStringJson() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("fields", new JSONArray(defaultQueryFields));
    json.put("query", query);

    return json;
  }

  private JSONObject buildSortJson() throws JSONException {
    if (Strings.isNullOrEmpty(sortField)) {
      return new JSONObject();
    }

    return new JSONObject().put(sortField, new JSONObject().put("order", sortDir));
  }

  private JSONObject buildFilter() throws JSONException {
    return new JSONObject().put("bool", new JSONObject()
        .put("must", new JSONObject().put("terms", new JSONObject().put("_type", new JSONArray(filterTypes)))));
  }

}
