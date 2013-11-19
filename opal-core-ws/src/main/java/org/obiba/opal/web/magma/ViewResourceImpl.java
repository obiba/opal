/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.ValueTable;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ViewManager;
import org.obiba.opal.web.magma.view.ViewDtos;
import org.obiba.opal.web.model.Magma.ViewDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.umd.cs.findbugs.annotations.Nullable;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class ViewResourceImpl extends AbstractValueTableResource implements ViewResource {

  private ViewManager viewManager;

  private ViewDtos viewDtos;

  @Autowired
  public void setViewDtos(ViewDtos viewDtos) {
    this.viewDtos = viewDtos;
  }

  @Autowired
  public void setViewManager(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  @Override
  public ViewDto getView() {
    return viewDtos.asDto(asView());
  }

  @Override
  public Response updateView(ViewDto viewDto, @Nullable String comment) {
    if(!viewDto.hasName()) return Response.status(Status.BAD_REQUEST).build();

    viewManager.addView(getDatasource().getName(), viewDtos.fromDto(viewDto), comment);
    if(!viewDto.getName().equals(getValueTable().getName())) {
      viewManager.removeView(getDatasource().getName(), getValueTable().getName());
    }

    return Response.ok().build();
  }

  @Override
  public VariablesViewResource getVariables() {
    VariablesViewResource resource = applicationContext.getBean(VariablesViewResource.class);
    resource.setLocales(getLocales());
    resource.setValueTable(getValueTable());
    return resource;
  }

  @Override
  public Response removeView() {
    viewManager.removeView(getDatasource().getName(), getValueTable().getName());
    return Response.ok().build();
  }

  @Override
  public Response downloadViewDefinition() {
    return Response.ok(asView(), "application/xml")
        .header("Content-Disposition", "attachment; filename=\"" + getValueTable().getName() + ".xml\"").build();
  }

  @Override
  @Bean
  @Scope("request")
  public TableResource getFrom() {
    TableResource resource = applicationContext.getBean("tableResource", TableResource.class);
    resource.setValueTable(asView().getWrappedValueTable());
    resource.setLocales(getLocales());
    return resource;
  }

  @Override
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  @Override
  public VariableViewResource getVariable(String name) {
    VariableViewResource resource = applicationContext.getBean(VariableViewResource.class);
    resource.setName(name);
    resource.setValueTable(getValueTable());
    resource.setLocales(getLocales());
    return resource;
  }

  private View asView() {
    ValueTable table = getValueTable();
    if(table.isView()) return (View) table;
    throw new InvalidRequestException("Not a view");
  }

}
