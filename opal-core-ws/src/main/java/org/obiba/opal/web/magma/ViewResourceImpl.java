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

import org.obiba.magma.*;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.MagmaSecurityExtension;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.core.event.ValueTableDeletedEvent;
import org.obiba.opal.core.event.ValueTableRenamedEvent;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.spi.r.datasource.magma.ResourceView;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class ViewResourceImpl extends TableResourceImpl implements ViewResource {

  private final static Authorizer authorizer = new ShiroAuthorizer();

  private ViewManager viewManager;

  private ViewDtos viewDtos;

  private SubjectProfileService subjectProfileService;

  @Autowired
  public void setViewDtos(ViewDtos viewDtos) {
    this.viewDtos = viewDtos;
  }

  @Autowired
  public void setViewManager(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  @Autowired
  public void setSubjectProfileService(SubjectProfileService subjectProfileService) {
    this.subjectProfileService = subjectProfileService;
  }

  @Override
  public ViewDto getView() {
    return viewDtos.asDto(asValueView());
  }

  @Override
  public Response updateView(ViewDto viewDto, @Nullable String comment) {
    if (!viewDto.hasName())
      return Response.status(Status.BAD_REQUEST).build();
    if (isResourceView() && !viewDto.hasExtension(Magma.ResourceViewDto.view))
      return Response.status(Status.BAD_REQUEST).build();

    if (!checkValuesPermissions(viewDto))
      return Response.status(Status.FORBIDDEN).build();

    ValueTable table = getValueTable();
    if (!viewDto.getName().equals(table.getName())) {
      getEventBus().post(new ValueTableRenamedEvent(table, viewDto.getName()));
      viewManager.removeView(getDatasource().getName(), getValueTable().getName());
      subjectProfileService.deleteBookmarks("/datasource/" + getDatasource().getName() + "/table/" + getValueTable().getName());
    }
    viewManager.addView(getDatasource().getName(), viewDtos.fromDto(viewDto), comment, null);

    return Response.ok().build();
  }

  @Override
  public VariablesViewResource getVariables() {
    VariablesViewResource resource = applicationContext.getBean("variablesViewResource", VariablesViewResource.class);
    resource.setLocales(getLocales());
    resource.setValueTable(getValueTable());
    return resource;
  }

  @Override
  public Response removeView() {
    try {
      getEventBus().post(new ValueTableDeletedEvent(getValueTable()));
      viewManager.removeView(getDatasource().getName(), getValueTable().getName());
      subjectProfileService.deleteBookmarks("/datasource/" + getDatasource().getName() + "/table/" + getValueTable().getName());
    } catch (NoSuchValueTableException e) {
      // ignore
    }
    return Response.ok().build();
  }

  @Override
  public Response initView() {
    viewManager.initView(getDatasource().getName(), getValueTable().getName());
    return ValueTableStatus.READY.equals(getValueTable().getStatus()) ? Response.ok().build() : Response.serverError().build();
  }

  @Override
  public Response downloadViewDefinition() {
    return Response.ok(asValueView(), "application/xml")
        .header("Content-Disposition", "attachment; filename=\"" + getValueTable().getName() + ".xml\"").build();
  }

  @Override
  @Bean
  @Scope("request")
  public TableResource getFrom() {
    TableResource resource = applicationContext.getBean("tableResource", TableResource.class);
    resource.setValueTable(asTableView().getWrappedValueTable());
    resource.setLocales(getLocales());
    return resource;
  }

  @Override
  public VariableViewResource getVariable(String name) {
    VariableViewResource resource = applicationContext.getBean(VariableViewResource.class);
    resource.setName(name);
    resource.setValueTable(getValueTable());
    resource.setLocales(getLocales());
    resource.setVariableValueSource(getValueTable().getVariableValueSource(name));
    return resource;
  }

  private boolean checkValuesPermissions(ViewDto viewDto) {
    if (!MagmaEngine.get().hasExtension(MagmaSecurityExtension.class)) return true;

    Authorizer authorizer = MagmaEngine.get().getExtension(MagmaSecurityExtension.class).getAuthorizer();

    ValueTable table = getValueTable();
    if (!authorizer.isPermitted(
        "rest:/datasource/" + table.getDatasource().getName() + "/table/" + table.getName() + "/valueSet:GET"))
      return true;

    return viewDto.hasExtension(Magma.ResourceViewDto.view) ? checkResourceValuesPermissions(viewDto) : checkTableValuesPermissions(viewDto);
  }

  private boolean checkTableValuesPermissions(ViewDto viewDto) {
    Authorizer authorizer = MagmaEngine.get().getExtension(MagmaSecurityExtension.class).getAuthorizer();
    // user can see the values of the view, so make sure user is also permitted to see the referred tables values
    for (String tableName : viewDto.getFromList()) {
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      ValueTable fromTable = resolver.resolveTable();
      if (!authorizer.isPermitted(
          "rest:/datasource/" + fromTable.getDatasource().getName() + "/table/" + fromTable.getName() + "/valueSet:GET")) {
        return false;
      }
    }
    return true;
  }

  private boolean checkResourceValuesPermissions(ViewDto viewDto) {
    // user can see the values of the view, so make sure user is also permitted to edit the resource ref
    for (String tableName : viewDto.getFromList()) {
      // reuse project's table naming for the resource...
      MagmaEngineTableResolver resolver = MagmaEngineTableResolver.valueOf(tableName);
      if (!authorizer.isPermitted(
          "rest:/project/" + resolver.getDatasourceName() + "/resource/" + resolver.getTableName() + ":PUT")) {
        return false;
      }
    }
    return true;
  }

  private ValueView asValueView() {
    ValueTable table = getValueTable();
    if (table.isView()) return (ValueView) table;
    throw new InvalidRequestException("Not a value view");
  }

  private View asTableView() {
    ValueTable table = getValueTable();
    if (isTableView()) return (View) table;
    throw new InvalidRequestException("Not a table view");
  }

  private boolean isTableView() {
    ValueTable table = getValueTable();
    return table.isView() && table instanceof View;
  }

  private ResourceView asResourceView() {
    ValueTable table = getValueTable();
    if (isResourceView()) return (ResourceView) table;
    throw new InvalidRequestException("Not a resource view");
  }

  private boolean isResourceView() {
    ValueTable table = getValueTable();
    return table.isView() && table instanceof ResourceView;
  }

}
