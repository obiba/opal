/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Attribute;
import org.obiba.magma.AttributeAware;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BinaryType;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.support.MimetypesFileExtensionsMap;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class ValueSetResourceImpl extends AbstractValueTableResource implements ValueSetResource {

//  private static final Logger log = LoggerFactory.getLogger(ValueSetResource.class);

  @Nullable
  private VariableValueSource vvs;

  @NotNull
  private VariableEntity entity;

  @Override
  public void setEntity(@NotNull VariableEntity entity) {
    this.entity = entity;
  }

  @Override
  public void setVariableValueSource(@Nullable VariableValueSource vvs) {
    this.vvs = vvs;
  }

  @Override
  public Response getValueSet(UriInfo uriInfo, String select, Boolean filterBinary) {
    if(vvs == null) {
      ValueSetsDto vs = getValueSetDto(uriInfo, filterVariables(select, 0, null), filterBinary);
      return TimestampedResponses.ok(getValueTable(), vs).build();
    }
    // ignore select parameter if value set is accessed by variable value source
    ValueSetsDto.ValueDto vs = getValueDto(uriInfo, filterBinary);
    return TimestampedResponses.ok(getValueTable(), vs).build();
  }

  @Override
  public Response drop() {
    ValueTable table = getValueTable();
    if (table.isView()) throw new IllegalArgumentException("Cannot remove a value set from a view");
    ValueTableWriter.ValueSetWriter vsw = getDatasource().createWriter(table.getName(), table.getEntityType()).writeValueSet(entity);
    vsw.remove();
    vsw.close();
    return Response.ok().build();
  }

  @Override
  public Response getValue(Integer pos) {
    if(pos != null && pos < 0) return Response.status(Status.BAD_REQUEST).build();
    if(vvs == null) return Response.status(Status.BAD_REQUEST).build();

    Variable variable = vvs.getVariable();

    if(!variable.isRepeatable() && pos != null) return Response.status(Status.BAD_REQUEST).build();

    return getValueAtPosition(pos);
  }

  /**
   * The position in a sequence of the value is its occurrence.
   *
   * @param pos the occurrence number (start at 0)
   * @return
   */
  private Response getValueAtPosition(@Nullable Integer pos) {
    ResponseBuilder builder;
    try {
      Value value = extractValue(entity.getIdentifier());
      if(value == null || value.isNull() ||
          value.isSequence() && pos != null && pos > value.asSequence().getSize() - 1) {
        builder = Response.status(Status.NOT_FOUND);
      } else {
        value = getValueAt(value, pos);
        builder = value.isNull()
            ? Response.status(Status.NOT_FOUND)
            : getValueResponse(entity.getIdentifier(), value, pos);
      }
    } catch(NoSuchValueSetException ex) {
      builder = Response.status(Status.NOT_FOUND);
    }
    return builder.build();
  }

  private ValueSetsDto getValueSetDto(final UriInfo uriInfo, Iterable<Variable> variables, final boolean filterBinary) {
    final ValueSet valueSet = getValueTable().getValueSet(entity);

    // Do not add iterable directly otherwise the values will be fetched as many times it is iterated
    // (i.e. 2 times, see AbstractMessageLite.addAll()).
    Iterable<ValueSetsDto.ValueDto> valueDtoIterable = Iterables
        .transform(variables, new Function<Variable, ValueSetsDto.ValueDto>() {

          @Override
          public ValueSetsDto.ValueDto apply(Variable fromVariable) {
            String link = uriInfo.getPath() + "/variable/" + fromVariable.getName() + "/value";
            Value value = getValueTable().getVariableValueSource(fromVariable.getName()).getValue(valueSet);
            return Dtos.asDto(link, value, filterBinary).build();
          }
        });
    ImmutableList.Builder<ValueSetsDto.ValueDto> valueDtos = ImmutableList.builder();
    for(ValueSetsDto.ValueDto dto : valueDtoIterable) {
      valueDtos.add(dto);
    }

    ValueSetsDto.ValueSetDto.Builder vsBuilder = Dtos.asDto(valueSet).addAllValues(valueDtos.build())
        .setTimestamps(Dtos.asDto(valueSet.getTimestamps()));

    return ValueSetsDto.newBuilder().setEntityType(getValueTable().getEntityType())
        .addAllVariables(Iterables.transform(variables, new Function<Variable, String>() {

          @Override
          public String apply(Variable from) {
            return from.getName();
          }

        })).addValueSets(vsBuilder.build()).build();
  }

  private ValueSetsDto.ValueDto getValueDto(UriInfo uriInfo, boolean filterBinary) {
    Value value = extractValue(entity.getIdentifier());
    return Dtos.asDto(uriInfo.getPath() + "/value", value, filterBinary).build();
  }

  @Nullable
  private Value extractValue(String identifier) {
    ValueSet valueSet = getValueTable()
        .getValueSet(new VariableEntityBean(getValueTable().getEntityType(), identifier));
    return vvs == null ? null : vvs.getValue(valueSet);
  }

  private Value getValueAt(Value value, @Nullable Integer occurrence) {
    return value.isSequence() && occurrence != null ? value.asSequence().get(occurrence) : value;
  }

  private ResponseBuilder getValueResponse(String identifier, Value value, @Nullable Integer pos) {
    Variable variable = vvs.getVariable();
    if(variable.getValueType().equals(BinaryType.get())) {
      return getBinaryValueResponse(identifier, value, pos);
    }
    return TimestampedResponses.ok(getValueTable(), value.toString())
        .type(value.isSequence() ? "text/csv" : MediaType.TEXT_PLAIN);
  }

  private ResponseBuilder getBinaryValueResponse(String identifier, Value value, @Nullable Integer pos) {
    if(value.isSequence()) return Response.status(Status.BAD_REQUEST);

    Variable variable = vvs.getVariable();

    // download as a file
    ResponseBuilder builder = TimestampedResponses.ok(getValueTable(), value.isNull() ? null : value.getValue())
        .type(getVariableMimeType(variable));
    builder.header("Content-Disposition", "attachment; filename=\"" + getFileName(variable, identifier, pos) + "\"");
    return builder;
  }

  private String getFileName(Variable variable, String identifier, @Nullable Integer pos) {
    // first look in variables attributes
    String name = getFileNameFromAttributes(variable);
    if(name == null) {
      name = variable.getName();
    }

    StringBuilder builder = new StringBuilder(name);
    int dot = name.lastIndexOf('.');
    if(dot == -1) {
      builder.append("-").append(identifier);
      if(pos != null) {
        builder.append("-").append(pos + 1);
      }
      builder.append(".").append(getFileExtension(variable));
    } else {
      String id = identifier;
      if(pos != null) {
        id = id + "-" + (pos + 1);
      }
      builder.insert(dot, "-" + id);
    }

    return builder.toString();
  }

  private String getFileExtension(Variable variable) {
    // first look in variables attributes
    String extension = getFileExtensionFromAttributes(variable);
    return extension == null
        ? MimetypesFileExtensionsMap.get().getPreferedFileExtension(variable.getMimeType())
        : extension;

  }

  private String getVariableMimeType(Variable variable) {
    // first, get it from variable mime-type
    if(variable.getMimeType() != null && !variable.getMimeType().isEmpty()) {
      return variable.getMimeType();
    }

    // if file extension is defined, get the mime-type from it
    String name = getFileExtensionFromAttributes(variable);
    name = name == null ? getFileNameFromAttributes(variable) : "file." + name;
    return name == null ? MediaType.APPLICATION_OCTET_STREAM : MimetypesFileExtensionsMap.get().getMimeType(name);
  }

  @Nullable
  private String getFileNameFromAttributes(AttributeAware variable) {
    for(Attribute attr : variable.getAttributes()) {
      if("filename".equalsIgnoreCase(attr.getName()) || "file-name".equalsIgnoreCase(attr.getName())) {
        String name = variable.getAttributeStringValue(attr.getName());
        if(name.length() > 0) {
          return name;
        }
      }
    }
    return null;
  }

  @Nullable
  private String getFileExtensionFromAttributes(AttributeAware variable) {
    for(Attribute attr : variable.getAttributes()) {
      if("fileextension".equalsIgnoreCase(attr.getName()) || "file-extension".equalsIgnoreCase(attr.getName())) {
        String extension = variable.getAttributeStringValue(attr.getName());
        if(extension.startsWith(".")) {
          extension = extension.substring(1);
        }
        if(extension.length() > 0) {
          return extension;
        }
      }
    }
    return null;
  }

}
