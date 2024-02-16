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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.obiba.magma.*;
import org.obiba.magma.support.VariableNature;
import org.obiba.opal.core.event.VariableDeletedEvent;
import org.obiba.opal.core.event.VariablesUpdatedEvent;
import org.obiba.opal.web.magma.math.*;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.core.*;
import java.util.List;

@Component("variableResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class VariableResourceImpl extends AbstractValueTableResource implements VariableResource {

  private String name;

  private VariableValueSource variableValueSource;

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setVariableValueSource(VariableValueSource variableValueSource) {
    this.variableValueSource = variableValueSource;
  }

  @Override
  public VariableDto get(UriInfo uriInfo) {
    UriBuilder uriBuilder = UriBuilder.fromPath("/");
    List<PathSegment> pathSegments = uriInfo.getPathSegments();
    for (int i = 0; i < 4; i++) {
      uriBuilder.segment(pathSegments.get(i).getPath());
    }
    String tableUri = uriBuilder.build().toString();
    Magma.LinkDto linkDto = Magma.LinkDto.newBuilder().setLink(tableUri).setRel(getValueTable().getName()).build();
    return Dtos.asDto(linkDto, variableValueSource.getVariable()).build();
  }

  @Override
  public Response updateVariable(VariableDto dto) {
    return updateVariable(Dtos.fromDto(dto));
  }

  @Override
  public Response deleteVariable() {
    if (getValueTable().isView()) throw new InvalidRequestException("Derived variable must be deleted by the view");

    // The variable must exist
    Variable v = getValueTable().getVariable(name);
    getEventBus().post(new VariableDeletedEvent(getValueTable(), v));

    try (ValueTableWriter tableWriter = getValueTable().getDatasource()
        .createWriter(getValueTable().getName(), getValueTable().getEntityType());
         ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      variableWriter.removeVariable(v);
      return Response.ok().build();
    }
  }

  @Override
  public Response updateVariableAttribute(String name, String namespace, String locale, String value) {
    Variable v = getValueTable().getVariable(this.name);
    return updateVariable(updateVariableAttribute(v, name, namespace, locale, value));
  }

  @Override
  public Response deleteVariableAttribute(String name, String namespace, String locale) {
    Variable v = getValueTable().getVariable(this.name);
    Variable updatedVariable = deleteVariableAttribute(v, name, namespace, locale);
    return updatedVariable == null ? Response.ok().build() : updateVariable(updatedVariable);
  }

  @Override
  public SummaryResource getSummary(UriInfo uriInfo, Request request, String natureStr) {
    Variable variable = variableValueSource.getVariable();

    // OPAL-2979 if user does not have access to individual values, summary should not provide details about top value frequencies
    String path = uriInfo.getPath();
    path = path.replaceAll("\\/variable\\/" + variable.getName() + "\\/summary$", "/valueSets");
    boolean viewValuesPermitted = SecurityUtils.getSubject().isPermitted("rest:" + path + ":GET");

    VariableNature nature = natureStr == null
        ? VariableNature.getNature(variable)
        : VariableNature.valueOf(natureStr.toUpperCase());

    // OPAL-2979 if user forces categorical nature without having the right to see values, go back to default summary
    if (!viewValuesPermitted && !variable.hasCategories() && nature == VariableNature.CATEGORICAL)
      nature = VariableNature.UNDETERMINED;

    SummaryResource resource;

    if (nature == VariableNature.UNDETERMINED && "text".equals(variable.getValueType().getName())) {
      resource = viewValuesPermitted ?
          applicationContext.getBean(TextSummaryResource.class) :
          applicationContext.getBean(DefaultSummaryResource.class);
    }
    else {
      resource = getSummaryResourceClass(nature);
    }
    resource.setValueTable(getValueTable());
    resource.setVariable(variable);
    resource.setVariableValueSource(variableValueSource);
    return resource;
  }

  //
  // Private methods
  //

  protected Variable updateVariableAttribute(Variable originalVariable, String name, String namespace, String locale, String value) {
    VariableBean.Builder builder = VariableBean.Builder.sameAs(originalVariable);
    builder.clearAttributes();
    Attribute.Builder attrBuilder = Attribute.Builder.newAttribute(name).withValue(value);
    if (!Strings.isNullOrEmpty(namespace)) attrBuilder.withNamespace(namespace);
    if (!Strings.isNullOrEmpty(locale)) attrBuilder.withLocale(locale);
    boolean found = false;
    for (Attribute attr : originalVariable.getAttributes()) {
      if (foundAttribute(attr, name, namespace, locale)) {
        found = true;
        builder.addAttribute(attrBuilder.build());
      } else
        builder.addAttribute(attr);
    }
    if (!found) builder.addAttribute(attrBuilder.build());
    return builder.build();
  }

  protected Variable deleteVariableAttribute(Variable originalVariable, String name, String namespace, String locale) {
    VariableBean.Builder builder = VariableBean.Builder.sameAs(originalVariable);
    builder.clearAttributes();
    boolean found = false;
    for (Attribute attr : originalVariable.getAttributes()) {
      if (!foundAttribute(attr, name, namespace, locale))
        builder.addAttribute(attr);
      else
        found = true;
    }
    return found ? builder.build() : null;
  }

  private boolean foundAttribute(Attribute attr, String name, String namespace, String locale) {
    if (!attr.getName().equals(name)) return false;
    if (!sameValue(attr.hasNamespace() ? attr.getNamespace() : "", namespace)) return false;
    if (!sameValue(attr.isLocalised() ? attr.getLocale().getLanguage() : "", locale)) return false;
    return true;
  }

  private boolean sameValue(String value1, String value2) {
    String nValue1 = Strings.isNullOrEmpty(value1) ? "" : value1;
    String nValue2 = Strings.isNullOrEmpty(value2) ? "" : value2;
    return nValue1.equals(nValue2);
  }

  private Response updateVariable(Variable updatedVariable) {
    if (getValueTable().isView()) throw new InvalidRequestException("Derived variable must be updated by the view");

    // The variable must exist
    Variable variable = getValueTable().getVariable(name);

    if (!variable.getEntityType().equals(updatedVariable.getEntityType())) {
      throw new InvalidRequestException("Variable entity type must be the same as the one of the table");
    }

    if (!variable.getName().equals(updatedVariable.getName())) {
      throw new InvalidRequestException("Variable cannot be renamed");
    }

    try (ValueTableWriter tableWriter = getValueTable().getDatasource()
        .createWriter(getValueTable().getName(), getValueTable().getEntityType());
         ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      variableWriter.writeVariable(updatedVariable);

      // inform about update
      List<Variable> variables = Lists.newArrayList(updatedVariable);
      getEventBus().post(new VariablesUpdatedEvent(getValueTable(), variables));
      return Response.ok().build();
    }
  }

  private SummaryResource getSummaryResourceClass(VariableNature nature) {
    switch (nature) {
      case CATEGORICAL:
        return applicationContext.getBean(CategoricalSummaryResource.class);
      case CONTINUOUS:
        return applicationContext.getBean(ContinuousSummaryResource.class);
      case BINARY:
        return applicationContext.getBean(BinarySummaryResource.class);
      case GEO:
        return applicationContext.getBean(GeoSummaryResource.class);
      case TEMPORAL:
      case UNDETERMINED:
      default:
        return applicationContext.getBean(DefaultSummaryResource.class);
    }
  }

  @Override
  public VariableValueSource getVariableValueSource() {
    return variableValueSource;
  }

  String getName() {
    return name;
  }
}
