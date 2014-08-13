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

import org.obiba.magma.ValueTable;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Search;

public class ItemResultDtoStrategy {

  private final ValueTable valueTable;

  public ItemResultDtoStrategy(ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  public void process(Search.ItemResultDto.Builder dtoItemResultBuilder) {
    String variableName = MagmaEngineVariableResolver.valueOf(dtoItemResultBuilder.getIdentifier()).getVariableName();
    Search.VariableItemDto.Builder dtoVariableItemBuilder = Search.VariableItemDto.newBuilder();
    Magma.LinkDto parentLink = Magma.LinkDto.newBuilder().setRel(valueTable.getName())
        .setLink("/datasource/" + valueTable.getDatasource().getName() + "/table/" + valueTable.getName()).build();
    dtoVariableItemBuilder.setVariable(Dtos.asDto(parentLink, valueTable.getVariable(variableName))
        .setLink(parentLink.getLink() + "/variable/" + variableName));
    dtoItemResultBuilder.setExtension(Search.VariableItemDto.item, dtoVariableItemBuilder.build());
  }

}
