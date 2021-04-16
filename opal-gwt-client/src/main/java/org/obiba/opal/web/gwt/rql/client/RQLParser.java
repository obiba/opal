/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.rql.client;

public class RQLParser {

  private final String query;

  private final RQLQuery rqlQuery;

  public RQLParser(String query) {
    this.query = query;
    this.rqlQuery = parseQuery(query);
  }

  public static RQLQuery parse(String query) {
    return new RQLParser(query).getRQLQuery();
  }

  public String getQuery() {
    return query;
  }

  public RQLQuery getRQLQuery() {
    return rqlQuery;
  }

  public String stringify() {
    return rqlQuery.stringify();
  }

  /**
   * Parse the RQL query into a RQLParser object.
   *
   * @param query
   * @return
   */
  private static native RQLQuery parseQuery(String query) /*-{
    var parser = new $wnd.RqlParser();
    // do not over-interpret tokens: everything is a string
    parser.converters['default'] = parser.converters.string;
    parser.stringToValue = function (str) {
      var converter = parser.converters.string;
      return converter(str);
    };
    encodedQuery = query.replace(/ /g, '%20')
      .replace(/\[/g, '%5B').replace(/\]/g, '%5D')
      .replace(/{/g, '%7B').replace(/}/g, '%7D');
    return parser.parse(encodedQuery);
  }-*/;

}
