/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search.support;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class used to build simple DTO queries used to extract a variable facet (terms, statistical)
 */
public class QueryTermDtoBuilder {

  private static final Logger log = LoggerFactory.getLogger(QueryTermDtoBuilder.class);

  private final Search.QueryTermDto.Builder dto;

  public QueryTermDtoBuilder(String facet) {
    dto = Search.QueryTermDto.newBuilder().setFacet(facet);
  }

  public QueryTermDtoBuilder global(Boolean value) {
    dto.setGlobal(value);
    return this;
  }

  /**
   * Given a variable name and depending on its nature, returns a DTO query
   *
   * @param variable
   * @param type optional field aggregation type
   * @return
   */
  public QueryTermDtoBuilder variableTermDto(@NotNull String variable, @Nullable String type) {
    log.info("* Variable {}", variable);
    Search.VariableTermDto.Builder variableDto = Search.VariableTermDto.newBuilder();
    variableDto.setVariable(variable);
    if(type != null) {
      variableDto.setType(Search.VariableTermDto.AggType.valueOf(type.toUpperCase()));
    }
    dto.setExtension(Search.VariableTermDto.field, variableDto.build());

    return this;
  }

  public Search.QueryTermDto build() {
    return dto.build();
  }
}