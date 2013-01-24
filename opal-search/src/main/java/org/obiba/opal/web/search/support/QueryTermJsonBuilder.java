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

import com.google.common.base.Joiner;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class QueryTermJsonBuilder {

  private static final Logger log = LoggerFactory.getLogger(QueryTermJsonBuilder.class);

  private List<String> fieldNames = new ArrayList<String>();
  private String termFieldName;
  private String termFieldValue;
  private int size = 9999;
  private JSONObject termFilters;


  public QueryTermJsonBuilder addFieldName(String name) {
    fieldNames.add(name);
    return this;
  }

  public QueryTermJsonBuilder setTermFieldName(String name) {
    termFieldName = name;
    return this;
  }

  public QueryTermJsonBuilder setTermFieldValue(String value) {
    termFieldValue = value;
    return this;
  }

  public QueryTermJsonBuilder setSize(int size) {
    this.size = size;
    return this;
  }

  public QueryTermJsonBuilder setTermFilters(JSONObject jsonFilters) {
    termFilters = jsonFilters;
    return this;
  }

  public JSONObject build() throws JSONException {
    JSONObject jsonTerm = new JSONObject().put("term", new JSONObject().put(termFieldName, termFieldValue));
    JSONObject jsonQuery = new JSONObject().put("query", jsonTerm);

    jsonQuery.put("size", Integer.toString(size));

    if (fieldNames.isEmpty()) {
      jsonQuery.put("fields", new JSONArray());
    }
    else {
      jsonQuery.put("fields", Joiner.on(",").join(fieldNames));
    }

    jsonQuery.put("filter", termFilters);

    return jsonQuery;
  }


  public static class QueryTermsFiltersBuilder {
    private List<String> filterValues = new ArrayList<String>();
    private String fieldName;


    public QueryTermsFiltersBuilder setFieldName(String name) {
      fieldName = name;
      return this;
    }

    public QueryTermsFiltersBuilder addFilterValue(String value) {
      filterValues.add(value);
      return this;
    }

    public QueryTermsFiltersBuilder addFilterValues(List<String> values) {
      filterValues = values;
      return this;
    }

    public JSONObject build() throws JSONException {
      JSONObject jsonTerms = new JSONObject();
      jsonTerms.put(fieldName, new JSONArray(filterValues));

      List<JSONObject> filtersTerms = new ArrayList<JSONObject>();
      filtersTerms.add(new JSONObject().put("terms", jsonTerms));

      JSONObject jsonFiltersTerms = new JSONObject().put("filters", filtersTerms);

      return new JSONObject().put("and", jsonFiltersTerms);
    }

  }


}
