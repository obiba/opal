/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.opal.web.model.Search;

/**
 * Converts a DTO query to an elastic search JSON query
 */
public class QueryTermConverter {

  private String fieldPrefix;

  /**
   * @param fieldPrefix - the field prefix is in the form of 'datasource.table' and is needed to fully qualify a
   * variable.
   */
  public QueryTermConverter(String fieldPrefix) {
    assert (fieldPrefix != null);

    this.fieldPrefix = fieldPrefix;
  }

  /**
   * Converts a DTO query to an elastic search JSON query
   *
   * @param dto
   * @return
   * @throws JSONException
   */
  public JSONObject convert(Search.QueryTermDto dto) throws JSONException {
    assert (dto != null);

    Search.VariableTermDto variableDto = dto.getExtension(Search.VariableTermDto.params);

    JSONObject json = new JSONObject("{\"query\":{\"match_all\":{}}, \"size\":0}");

    JSONObject facetsJSON = new JSONObject();

    if(variableDto.hasExtension(Search.InTermDto.params)) {
      // Categorical variable
      facetsJSON.put(dto.getFacet(), new JSONObject().put("terms", convertField(variableDto)));
    } else if(variableDto.hasExtension(Search.RangeTermDto.params)) {
      // Continuous variable
      facetsJSON.put(dto.getFacet(), new JSONObject().put("statistical", convertField(variableDto)));
    }

    json.put("facets", facetsJSON);

    return json;
  }

  private JSONObject convertField(Search.VariableTermDto variableDto) throws JSONException {
    JSONObject termsJSON = new JSONObject();
    termsJSON.put("field", fieldPrefix + ":" + variableDto.getVariable());
    // NOTE does not seem to be necessary
    // termsJSON.put("all_fields", true);
    return termsJSON;
  }

}
