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

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.opal.core.ValueTableUpdateListener;
import org.obiba.opal.core.event.VariableDeletedEvent;
import org.obiba.opal.core.event.VariablesUpdatedEvent;
import org.obiba.opal.web.model.Magma.LinkDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.core.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Component("variablesResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class VariablesResourceImpl extends AbstractValueTableResource implements VariablesResource {

  @Override
  public Iterable<VariableDto> getVariables(Request request, UriInfo uriInfo, String script, Integer offset,
                                            @Nullable Integer limit) {
    if (offset < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "offset", String.valueOf(limit));
    }
    if (limit != null && limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }

    final UriBuilder uriBuilder = tableUriBuilder(uriInfo);
    String tableUri = uriBuilder.build().toString();
    uriBuilder.path(TableResource.class, "getVariable");

    LinkDto.Builder tableLinkBuilder = LinkDto.newBuilder().setLink(tableUri).setRel(getValueTable().getName());

    Iterable<Variable> variables = filterVariables(script, offset, limit);
    Iterable<VariableDto> entity = Iterables
        .transform(variables, Functions.compose(input -> input.setLink(uriBuilder.build(input.getName()).toString()).build(),
            Dtos.asDtoFunc(tableLinkBuilder.build())));

    return entity;
  }

  @Override
  public Response getExcelDictionary(Request request) throws MagmaRuntimeException, IOException {
    String destinationName = getValueTable().getDatasource().getName() + "." + getValueTable().getName() +
        "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    Datasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);

    destinationDatasource.initialise();
    try {
      DatasourceCopier.Builder.newCopier().dontCopyValues().build().copy(getValueTable(), destinationDatasource);
    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }

    return Response.ok().entity(excelOutput.toByteArray()).type("application/vnd.ms-excel")
        .header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  @Override
  public Response setVariableOrder(List<String> variables) {
    List<VariableDto> orderedVariables = Lists.newArrayListWithExpectedSize(variables != null ? variables.size() : 0);
    List<Variable> currentOrderVariables = orderVariables(Lists.newArrayList(getValueTable().getVariables()));
    int i = 1;
    if (variables != null && !variables.isEmpty()) {
      for (String name : variables) {
        if (getValueTable().hasVariable(name)) {
          orderedVariables.add(Dtos.asDto(getValueTable().getVariable(name)).setIndex(i++).build());
        }
      }
    }
    for (Variable v : currentOrderVariables) {
      if (variables == null || !variables.contains(v.getName()))
        orderedVariables.add(Dtos.asDto(v).setIndex(i++).build());
    }

    if (!orderedVariables.isEmpty()) addOrUpdateTableVariables(orderedVariables);
    return Response.ok().build();
  }

  @Override
  public Response updateAttribute(String namespace, String name, List<String> locales, List<String> values, String action, List<String> variableNames) {
    if ("delete".equals(action)) {
      if (Strings.isNullOrEmpty(name)) return Response.status(BAD_REQUEST).build();
      removeAttributes(namespace, name, locales, values, variableNames);
      return Response.ok().build();
    }
    if (Strings.isNullOrEmpty(name) || locales.contains("")) return Response.status(BAD_REQUEST).build();
    if (locales.isEmpty()) {
      String valueStr = values.isEmpty() ? null : Joiner.on("|").join(values);
      if (Strings.isNullOrEmpty(valueStr)) {
        removeAttribute(namespace, name, null, valueStr, variableNames);
        return Response.ok().build();
      }
      return setAttribute(namespace, name, null, valueStr, variableNames);
    }
    if (locales.size() != values.size())
      return Response.status(BAD_REQUEST).build();

    Map<String,String> localizedValues = Maps.newHashMap();
    for (int i=0; i<locales.size(); i++) {
      String locale = locales.get(i);
      String value = values.get(i);
      // check locales are all different
      if (localizedValues.containsKey(locale)) return Response.status(BAD_REQUEST).build();
      localizedValues.put(locale, value);
    }
    return setAttributes(namespace, name, localizedValues, variableNames);
  }

  @Override
  public Response deleteAttribute(String namespace, String name, String localeStr, String value) {
    if (Strings.isNullOrEmpty(name)) return Response.status(BAD_REQUEST).build();
    removeAttribute(namespace, name, localeStr, value, null);
    return Response.ok().build();
  }

  @Override
  public Response addOrUpdateVariables(List<VariableDto> variables, @Nullable String comment) {

    // @TODO Check if table can be modified and respond with "IllegalTableModification"
    // (it seems like this cannot be done with the current Magma implementation).

    if (getValueTable().isView()) {
      return Response.status(BAD_REQUEST).entity(getErrorMessage(BAD_REQUEST, "CannotWriteToView")).build();
    }
    addOrUpdateTableVariables(variables);

    return Response.ok().build();
  }

  private void addOrUpdateTableVariables(List<VariableDto> variables) {
    addOrUpdateTableVariables(variables.stream().map(Dtos::fromDto).collect(Collectors.toList()));
  }

  void addOrUpdateTableVariables(Iterable<Variable> variables) {
    try (ValueTableWriter tableWriter = getValueTable().getDatasource()
        .createWriter(getValueTable().getName(), getValueTable().getEntityType());
         VariableWriter variableWriter = tableWriter.writeVariables()) {
      for (Variable variable : variables) {
        variableWriter.writeVariable(variable);
      }
    }
    getEventBus().post(new VariablesUpdatedEvent(getValueTable(), variables));
  }

  @Override
  public Response deleteVariables(List<String> variables) {
    if (getValueTable().isView()) throw new InvalidRequestException("Derived variable must be deleted by the view");

    try (ValueTableWriter tableWriter = getValueTable().getDatasource()
        .createWriter(getValueTable().getName(), getValueTable().getEntityType());
         ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      for (String name : variables) {
        // The variable must exist
        Variable v = getValueTable().getVariable(name);
        getEventBus().post(new VariableDeletedEvent(getValueTable(), v));
        variableWriter.removeVariable(v);
      }
      return Response.ok().build();
    }
  }

  @Override
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  //
  // private methods
  //

  private Response setAttributes(final String namespace, final String name, final Map<String,String> localizedValues, final List<String> variableNames) {
    ValueTable table = getValueTable();
    Iterable<Variable> variables = getVariables(table, variableNames);
    List<Variable> updatedVariables = Lists.newArrayList();
    List<Attribute> attributes = localizedValues.keySet().stream().map(localeStr -> {
      Locale locale = Strings.isNullOrEmpty(localeStr) ? null : Locale.forLanguageTag(localeStr);
      String value = localizedValues.get(localeStr);
      return Attribute.Builder.newAttribute(name).withNamespace(namespace).withValue(locale, value).build();
    }).collect(Collectors.toList());

    for (Variable variable : variables) {
      // copy variable without the attribute of interest
      Variable.Builder updatedVariableBuilder = Variable.Builder.sameAs(variable).clearAttributes();
      if (variable.hasAttributes()) {
        // filter out same attributes
        variable.getAttributes().stream()
            .filter(attr -> attributes.stream().noneMatch(attribute -> isSameAttribute(attr, attribute)))
            .forEach(updatedVariableBuilder::addAttribute);
      }
      // add new/updated valid attributes
      attributes.stream()
          .filter(attribute -> !attribute.getValue().isNull())
          .forEach(updatedVariableBuilder::addAttribute);
      // update variable
      updatedVariables.add(updatedVariableBuilder.build());
    }
    addOrUpdateTableVariables(updatedVariables);
    return Response.ok().build();
  }

  private Response setAttribute(String namespace, String name, String localeStr, String value, List<String> variableNames) {
    ValueTable table = getValueTable();
    Iterable<Variable> variables = getVariables(table, variableNames);
    Locale locale = Strings.isNullOrEmpty(localeStr) ? null : Locale.forLanguageTag(localeStr);
    final Attribute attribute = Attribute.Builder.newAttribute(name).withNamespace(namespace).withValue(locale, value).build();
    List<Variable> updatedVariables = Lists.newArrayList();

    for (Variable variable : variables) {
      // copy variable without the attribute of interest
      Variable.Builder updatedVariableBuilder = Variable.Builder.sameAs(variable).clearAttributes();
      if (variable.hasAttributes())
        variable.getAttributes().stream()
          .filter(attr -> !isSameAttribute(attr, attribute))
          .forEach(updatedVariableBuilder::addAttribute);
      // empty value means no attribute
      if (!Strings.isNullOrEmpty(value)) updatedVariableBuilder.addAttribute(attribute);
      // update variable
      updatedVariables.add(updatedVariableBuilder.build());
    }
    addOrUpdateTableVariables(updatedVariables);
    return Response.ok().build();
  }


  private void removeAttributes(String namespace, String name, List<String> locales, List<String> values, List<String> variableNames) {
    if (values.isEmpty()) {
      if (locales.isEmpty()) {
        removeAttribute(namespace, name, null,null, variableNames);
      } else {
        for (String locale : locales) {
          removeAttribute(namespace, name, locale, null, variableNames);
        }
      }
    } else {
      for (String value : values) {
        if (locales.isEmpty()) {
          removeAttribute(namespace, name, null, value, variableNames);
        } else {
          for (String locale : locales) {
            removeAttribute(namespace, name, locale, value, variableNames);
          }
        }
      }
    }
  }

  private void removeAttribute(String namespace, String name, String localeStr, String value, List<String> variableNames) {
    ValueTable table = getValueTable();
    Iterable<Variable> variables = getVariables(table, variableNames);
    Locale locale = Strings.isNullOrEmpty(localeStr) ? null : Locale.forLanguageTag(localeStr);
    Attribute.Builder builder = Attribute.Builder.newAttribute(name).withNamespace(namespace).withLocale(locale);
    if (!Strings.isNullOrEmpty(value)) builder.withValue(value);
    final Attribute attribute = builder.build();
    List<Variable> updatedVariables = Lists.newArrayList();
    boolean removeAnyValues = Strings.isNullOrEmpty(localeStr) && Strings.isNullOrEmpty(value);

    for (Variable variable : variables) {
      if (variable.hasAttributes()) {
        // copy variable without the attribute of interest
        Variable.Builder updatedVariableBuilder = Variable.Builder.sameAs(variable).clearAttributes();
        variable.getAttributes().stream()
            .filter(attr -> removeAnyValues ? !isSameAttributeAnyValue(attr, attribute) :
                attribute.getValue().isNull() ? !isSameAttribute(attr, attribute) : !isSameAttributeWithValue(attr, attribute))
            .forEach(updatedVariableBuilder::addAttribute);
        Variable updatedVariable = updatedVariableBuilder.build();
        // update variable only if attributes have changed
        if (variable.getAttributes().size() != (updatedVariable.hasAttributes() ? updatedVariable.getAttributes().size() : 0))
          updatedVariables.add(updatedVariableBuilder.build());
      }
    }
    addOrUpdateTableVariables(updatedVariables);
  }

  /**
   * Get the {@link Variable}s from their name or all of them if name list is empty.
   *
   * @param table
   * @param variableNames
   * @return
   */
  private Iterable<Variable> getVariables(ValueTable table, List<String> variableNames) {
    if (variableNames == null || variableNames.isEmpty()) return table.getVariables();
    return variableNames.stream().filter(table::hasVariable).map(table::getVariable).collect(Collectors.toList());
  }

  private boolean isSameAttributeAnyValue(Attribute source, Attribute target) {
    if (!source.getName().equals(target.getName())) return false;
    if (source.hasNamespace() && !source.getNamespace().equals(target.getNamespace())) return false;
    if (!source.hasNamespace() && target.hasNamespace()) return false;
    return true;
  }

  private boolean isSameAttribute(Attribute source, Attribute target) {
    if (!source.getName().equals(target.getName())) return false;
    if (source.hasNamespace() && !source.getNamespace().equals(target.getNamespace())) return false;
    if (!source.hasNamespace() && target.hasNamespace()) return false;
    if (source.isLocalised() && (!target.isLocalised() || !source.getLocale().equals(target.getLocale()))) return false;
    if (!source.isLocalised() && target.isLocalised()) return false;
    return true;
  }

  private boolean isSameAttributeWithValue(Attribute source, Attribute target) {
    if (!isSameAttribute(source, target)) return false;
    if (target.getValue().isNull()) return source.getValue().isNull();
    if (source.getValue().isNull()) return target.getValue().isNull();
    return source.getValue().equals(target.getValue());
  }

  ClientErrorDto getErrorMessage(Response.StatusType responseStatus, String errorStatus) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus).build();
  }

  private static UriBuilder tableUriBuilder(UriInfo uriInfo) {
    List<PathSegment> segments = Lists.newArrayList(uriInfo.getPathSegments());
    segments.remove(segments.size() - 1);
    UriBuilder ub = UriBuilder.fromPath("/");
    for (PathSegment segment : segments) {
      ub.segment(segment.getPath());
    }
    return ub;
  }

}
