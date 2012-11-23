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

import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryTermDtoBuilder {

  private static final Logger log = LoggerFactory.getLogger(QueryTermDtoBuilder.class);

  private final IndexManagerHelper indexManagerHelper;

  private Search.QueryTermDto.Builder dto;

  public QueryTermDtoBuilder(IndexManagerHelper indexManagerHelper, String facet) {
    this.indexManagerHelper = indexManagerHelper;
    dto = Search.QueryTermDto.newBuilder().setFacet(facet);
  }

  public QueryTermDtoBuilder not(Boolean value) {
    dto.setNot(value);
    return this;
  }

  public QueryTermDtoBuilder variableTermDto(String variable) {
    log.info("* Variable " + variable);

    switch(indexManagerHelper.getVariableNature(variable)) {
      case CATEGORICAL:
        return categoricalVariableTermDto(variable);

      case CONTINUOUS:
        return continuousVariableTermDto(variable);

      default:
        throw new UnsupportedOperationException("Variable nature not supported");
    }
  }

  private QueryTermDtoBuilder categoricalVariableTermDto(String variable) {
    log.info("categoricalVariableTermDto() - " + variable);

    Search.VariableTermDto.Builder variableDto = Search.VariableTermDto.newBuilder();
    variableDto.setVariable(variable);
    variableDto.setExtension(Search.InTermDto.params, Search.InTermDto.newBuilder().build());

    dto.setExtension(Search.VariableTermDto.params, variableDto.build());

    return this;
  }

  private QueryTermDtoBuilder continuousVariableTermDto(String variable) {
    log.info("continuousVariableTermDto() - " + variable);

    Search.VariableTermDto.Builder variableDto = Search.VariableTermDto.newBuilder();
    variableDto.setVariable(variable);
    variableDto.setExtension(Search.RangeTermDto.params, Search.RangeTermDto.newBuilder().build());

    dto.setExtension(Search.VariableTermDto.params, variableDto.build());

    return this;
  }

  public Search.QueryTermDto build() {
    return dto.build();
  }

}
