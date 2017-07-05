/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.google.common.base.Strings;
import org.obiba.magma.*;
import org.obiba.magma.support.VariableNature;
import org.obiba.opal.web.magma.math.BinarySummaryResource;
import org.obiba.opal.web.magma.math.CategoricalSummaryResource;
import org.obiba.opal.web.magma.math.ContinuousSummaryResource;
import org.obiba.opal.web.magma.math.DefaultSummaryResource;
import org.obiba.opal.web.magma.math.GeoSummaryResource;
import org.obiba.opal.web.magma.math.SummaryResource;
import org.obiba.opal.web.magma.math.TextSummaryResource;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    for(int i = 0; i < 4; i++) {
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
    if(getValueTable().isView()) throw new InvalidRequestException("Derived variable must be deleted by the view");

    // The variable must exist
    Variable v = getValueTable().getVariable(name);

    if (tableListeners != null && !tableListeners.isEmpty()) {
      for (ValueTableUpdateListener listener : tableListeners) {
        listener.onDelete(getValueTable(), v);
      }
    }

    try(ValueTableWriter tableWriter = getValueTable().getDatasource()
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
  public SummaryResource getSummary(Request request, String natureStr) {
    Variable variable = variableValueSource.getVariable();
    VariableNature nature = natureStr == null
        ? VariableNature.getNature(variable)
        : VariableNature.valueOf(natureStr.toUpperCase());

    SummaryResource resource;

    resource = nature == VariableNature.UNDETERMINED && "text".equals(variable.getValueType().getName())
        ? applicationContext.getBean(TextSummaryResource.class)
        : getSummaryResourceClass(nature);
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
      }
      else
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
    if(getValueTable().isView()) throw new InvalidRequestException("Derived variable must be updated by the view");

    // The variable must exist
    Variable variable = getValueTable().getVariable(name);

    if(!variable.getEntityType().equals(updatedVariable.getEntityType())) {
      throw new InvalidRequestException("Variable entity type must be the same as the one of the table");
    }

    if(!variable.getName().equals(updatedVariable.getName())) {
      throw new InvalidRequestException("Variable cannot be renamed");
    }

    try(ValueTableWriter tableWriter = getValueTable().getDatasource()
        .createWriter(getValueTable().getName(), getValueTable().getEntityType());
        ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      variableWriter.writeVariable(updatedVariable);
      return Response.ok().build();
    }
  }

  private SummaryResource getSummaryResourceClass(VariableNature nature) {
    switch(nature) {
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
