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

import java.io.IOException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

/**
 *
 */
public class EsQueryBuilders {

  private EsQueryBuilders() {}

  public static class EsBoolTermsQueryBuilder {

    private final XContentBuilder builder;

    public EsBoolTermsQueryBuilder() throws IOException {
      builder = XContentFactory.jsonBuilder().startObject() //
          .startObject("query") //
          .startObject("bool") //
          .startArray("must");
    }

    public EsBoolTermsQueryBuilder addTerm(String name, String value) throws IOException {
      builder.startObject() //
          .startObject("term").field(name, value).endObject() //
          .endObject();
      return this;
    }

    public JSONObject build() throws IOException, JSONException {
      builder.endArray() // must
          .endObject() // bool
          .endObject(); //query
      return new JSONObject(builder.string());
    }

  }

}
