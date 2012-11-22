/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.search;

import org.obiba.opal.web.model.Search;

public class QueryTermDtoBuilder {

    private Search.QueryTermDto.Builder dto;


    public QueryTermDtoBuilder(String facet)
    {
        dto = Search.QueryTermDto.newBuilder().setFacet(facet);
    }

    public QueryTermDtoBuilder not(Boolean value)
    {
        dto.setNot(value);
        return this;
    }

    public QueryTermDtoBuilder categoricalVariableTermDto(String variable)
    {
        Search.VariableTermDto.Builder variableDto = Search.VariableTermDto.newBuilder();
        variableDto.setVariable(variable);
        variableDto.setExtension(Search.InTermDto.params, Search.InTermDto.newBuilder().build());

        dto.setExtension(Search.VariableTermDto.params, variableDto.build());

        return this;
    }


    public QueryTermDtoBuilder continuousVariableTermDto(String variable)
    {
        Search.VariableTermDto.Builder variableDto = Search.VariableTermDto.newBuilder();
        variableDto.setVariable(variable);
        variableDto.setExtension(Search.RangeTermDto.params, Search.RangeTermDto.newBuilder().build());

        dto.setExtension(Search.VariableTermDto.params, variableDto.build());

        return this;
    }

    public Search.QueryTermDto build()
    {
        return dto.build();
    }


}
