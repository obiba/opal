/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewAwareDatasource;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.StaticDatasourceFactoryDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Static datasource is used as a transient datasource.
 */
@Component
public class StaticDatasourceFactoryDtoParser extends AbstractDatasourceFactoryDtoParser {

  private final ViewDtos viewDtos;

  @Autowired
  public StaticDatasourceFactoryDtoParser(ViewDtos viewDtos) {
    this.viewDtos = viewDtos;
  }

  @NotNull
  @Override
  protected DatasourceFactory internalParse(DatasourceFactoryDto dto, DatasourceEncryptionStrategy encryptionStrategy) {
    return new StaticDatasourceFactory(dto);
  }

  @Override
  public boolean canParse(DatasourceFactoryDto dto) {
    return dto.hasExtension(StaticDatasourceFactoryDto.params);
  }

  private final class StaticDatasourceFactory extends AbstractDatasourceFactory {
    private final DatasourceFactoryDto dto;

    private StaticDatasourceFactory(DatasourceFactoryDto dto) {
      this.dto = dto;
      if(dto.getName() != null) {
        setName(dto.getName());
      }
    }

    @NotNull
    @Override
    protected Datasource internalCreate() {
      StaticDatasourceFactoryDto staticDto = dto.getExtension(StaticDatasourceFactoryDto.params);
      StaticDatasource ds = new StaticDatasource(getName());
      for(TableDto tableDto : staticDto.getTablesList()) {
        addValueTable(ds, tableDto);
      }

      Set<View> views = new LinkedHashSet<>();
      for(ViewDto viewDto : staticDto.getViewsList()) {
        if(viewDto.getFromCount() == 0) {
          // cannot make a view from it, so make it a table
          addValueTable(ds, viewDtos.asTableDto(viewDto));
        } else {
          views.add(viewDtos.fromDto(viewDto));
        }
      }

      return new ViewAwareDatasource(ds, views);
    }

    private void addValueTable(StaticDatasource ds, TableDto tableDto) {
      StaticValueTable table = new StaticValueTable(ds, tableDto.getName(), new HashSet<String>(),
          tableDto.getEntityType());
      for(Magma.VariableDto variableDto : tableDto.getVariablesList()) {
        table.addVariable(Dtos.fromDto(variableDto));
      }
      ds.addValueTable(table);
    }
  }
}
