/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.search.finder.*;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.GET;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class VariableEntityTablesResourceImpl implements AbstractTablesResource, VariableEntityTablesResource {

  private static final Logger log = LoggerFactory.getLogger(VariableEntityTablesResourceImpl.class);

  private VariableEntityBean variableEntity;

  @Override
  public void setVariableEntity(VariableEntityBean variableEntity) {
    this.variableEntity = variableEntity;
  }

  @Override
  @GET
  @NoAuthorization
  public List<Magma.TableDto> getTables() {
    // maybe should return 404 if list is empty?
    return getTables(0);
  }

  @Override
  public List<Magma.TableDto> getTables(int limit) {

    FinderResult<List<Magma.TableDto>> results = new FinderResult<>(new ArrayList<Magma.TableDto>());

    new VariableEntityTablesFinder() //
        .withLimit(limit) //
        .find(new VariableEntityTablesQuery(variableEntity), results);

    List<Magma.TableDto> tables = results.getValue();

    Collections.sort(tables, Comparator.comparing(Magma.TableDto::getLink));

    return tables;
  }

  public static class VariableEntityTablesQuery extends AbstractFinderQuery {

    private final VariableEntity entity;

    public VariableEntityTablesQuery(VariableEntity entity) {
      this.entity = entity;
    }

    public VariableEntity getEntity() {
      return entity;
    }
  }

  public static class EntityTablesFinder
      extends AccessFilterTablesFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    @Override
    protected boolean isTableSearchable(ValueTable valueTable, VariableEntityTablesQuery query) {
      return valueTable.getEntityType().equals(query.getEntity().getType()) && areEntitiesReadable(valueTable);
    }

    private boolean areEntitiesReadable(ValueTable valueTable) {
      return SecurityUtils.getSubject().isPermitted("rest:/datasource/" + valueTable.getDatasource().getName() +
          "/table/" + valueTable.getName() + "/entities:GET");
    }
  }

  private static class EntityTablesMagmaFinder
      extends AbstractMagmaFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    @Override
    public void executeQuery(VariableEntityTablesQuery query, FinderResult<List<Magma.TableDto>> result) {

      for(ValueTable valueTable : query.getTableFilter()) {

        if(valueTable.hasValueSet(query.getEntity())) {
          Magma.TableDto tableDto = Dtos.asDto(valueTable, false).build();
          result.getValue().add(tableDto);

          if(getLimit() > 0 && result.getValue().size() == getLimit()) {
            break;
          }
        }
      }
    }
  }

  private class VariableEntityTablesFinder
      extends AbstractFinder<VariableEntityTablesQuery, FinderResult<List<Magma.TableDto>>> {

    @Override
    public void find(VariableEntityTablesQuery query, FinderResult<List<Magma.TableDto>> result) {
      nextFinder(new EntityTablesFinder()) //
          .nextFinder(new EntityTablesMagmaFinder().withLimit(getLimit()));
      next(query, result);
    }
  }

}
