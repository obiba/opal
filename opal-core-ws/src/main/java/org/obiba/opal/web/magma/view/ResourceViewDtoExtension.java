/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.view;

import org.obiba.magma.ValueView;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.spi.r.datasource.magma.ResourceView;
import org.obiba.opal.spi.resource.TabularResourceConnectorFactory;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Magma.ResourceViewDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;

/**
 * An implementation of {@Code ViewDtoExtension} for {@code View} instances that have a {@code ListClause}.
 */
@Component
public class ResourceViewDtoExtension implements ValueViewDtoExtension {

  @Autowired
  private TabularResourceConnectorFactory tabularResourceConnectorFactory;

  @Value("${org.obiba.opal.keys.entityType}")
  private String participantEntityType;

  @Override
  public boolean isExtensionOf(@NotNull ViewDto viewDto) {
    return viewDto.hasExtension(ResourceViewDto.view);
  }

  @Override
  public boolean isDtoOf(@NotNull ValueView view) {
    return view instanceof ResourceView;
  }

  @Override
  public ValueView fromDto(ViewDto viewDto) {
    ResourceViewDto resDto = viewDto.getExtension(ResourceViewDto.view);

    String resFullName = viewDto.getFrom(0);
    // hack: use table resolving pattern
    MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(resFullName);

    ResourceView view = new ResourceView();
    view.setName(viewDto.getName());
    view.setProject(resolver.getDatasourceName());
    view.setResource(resolver.getTableName());
    view.setEntityType(resDto.hasEntityType() ? resDto.getEntityType() : participantEntityType);
    if (resDto.hasProfile())
      view.setProfile(resDto.getProfile());
    if (resDto.hasIdColumn())
      view.setIdColumn(resDto.getIdColumn());
    if (resDto.hasAllColumns())
      view.setAllColumns(resDto.getAllColumns());
    view.setVariables(resDto.getVariablesList().stream()
        .map(Dtos::fromDto)
        .collect(Collectors.toList()));

    // set connector
    view.setConnector(tabularResourceConnectorFactory.newConnector(view.getProject(), view.getResource(), view.getProfile()));

    return view;
  }

  @Override
  public ViewDto asDto(ValueView view) {
    ViewDto.Builder viewDtoBuilder = ViewDto.newBuilder();
    viewDtoBuilder.setDatasourceName(view.getDatasource().getName());
    viewDtoBuilder.setName(view.getName());
    ResourceView resView = (ResourceView) view;
    viewDtoBuilder.addFrom(resView.getResourceFullName());

    ResourceViewDto.Builder resDtoBuilder = ResourceViewDto.newBuilder();
    view.getVariables().forEach(v -> resDtoBuilder.addVariables(Dtos.asDto(v)));
    if (resView.hasIdColumn())
      resDtoBuilder.setIdColumn(resView.getIdColumn());
    if (resView.hasProfile())
      resDtoBuilder.setProfile(resView.getProfile());
    resDtoBuilder.setAllColumns(resView.isAllColumns());
    resDtoBuilder.setEntityType(resView.getEntityType());

    viewDtoBuilder.setExtension(ResourceViewDto.view, resDtoBuilder.build());

    return viewDtoBuilder.build();
  }

  @Override
  public TableDto asTableDto(ViewDto viewDto, TableDto.Builder tableDtoBuilder) {
    ResourceViewDto listDto = viewDto.getExtension(ResourceViewDto.view);
    if (listDto.getVariablesCount() > 0) {
      tableDtoBuilder.setEntityType(listDto.getVariables(0).getEntityType());
    }
    tableDtoBuilder.addAllVariables(listDto.getVariablesList());
    return tableDtoBuilder.build();
  }

}