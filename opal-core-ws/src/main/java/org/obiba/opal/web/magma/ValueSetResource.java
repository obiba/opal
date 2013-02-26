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

import java.util.HashSet;
import java.util.Locale;

import javax.annotation.Nullable;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.Attribute;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BinaryType;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.support.MimetypesFileExtensionsMap;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 *
 */
public class ValueSetResource extends AbstractValueTableResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ValueSetResource.class);

  @Nullable
  private VariableValueSource vvs;

  @Nullable
  private VariableEntity entity;

  public ValueSetResource(ValueTable valueTable, VariableEntity entity) {
    this(valueTable, null, entity);
  }

  public ValueSetResource(ValueTable valueTable, VariableValueSource vvs, VariableEntity entity) {
    super(valueTable, new HashSet<Locale>());
    this.vvs = vvs;
    this.entity = entity;
  }

  /**
   * Get a chunk of value sets, optionally filters the variables and/or the entities.
   *
   * @param select script for filtering the variables
   * @return
   */
  @GET
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  public Response getValueSet(@Context final UriInfo uriInfo, @QueryParam("select") String select,
      @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary) {
    if(vvs == null) {
      ValueSetsDto vs = getValueSetDto(uriInfo, filterVariables(select, 0, null), filterBinary);
      return TimestampedResponses.ok(getValueTable(), vs).build();
    } else {
      // ignore select parameter if value set is accessed by variable value source
      ValueSetsDto.ValueDto vs = getValueDto(uriInfo, filterBinary);
      return TimestampedResponses.ok(getValueTable(), vs).build();
    }
  }

  /**
   * Get a value, optionally providing the position (start at 0) of the value in the case of a value sequence.
   *
   * @param pos
   * @return
   */
  @GET
  @Path("/value")
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  public Response getValue(@QueryParam("pos") Integer pos) {
    if(pos != null && pos < 0) return Response.status(Status.BAD_REQUEST).build();
    if(vvs == null) return Response.status(Status.BAD_REQUEST).build();

    Variable variable = vvs.getVariable();

    if(!variable.isRepeatable() && pos != null) return Response.status(Status.BAD_REQUEST).build();

    return getValueAtPosition(pos);
  }

  //
  // private methods
  //

  /**
   * The position in a sequence of the value is its occurrence.
   *
   * @param pos the occurrence number (start at 0)
   * @return
   */
  private Response getValueAtPosition(Integer pos) {
    ResponseBuilder builder;
    try {
      Value value = extractValue(entity.getIdentifier());
      if(value.isNull() || (value.isSequence() && pos != null && pos > value.asSequence().getSize() - 1)) {
        builder = Response.status(Status.NOT_FOUND);
      } else {
        value = getValueAt(value, pos);
        if(value.isNull()) {
          builder = Response.status(Status.NOT_FOUND);
        } else {
          builder = getValueResponse(entity.getIdentifier(), value, pos);
        }
      }
    } catch(NoSuchValueSetException ex) {
      builder = Response.status(Status.NOT_FOUND);
    }
    return builder.build();
  }

  private ValueSetsDto getValueSetDto(final UriInfo uriInfo, final Iterable<Variable> variables,
      final boolean filterBinary) {
    final ValueSet valueSet = getValueTable().getValueSet(entity);

    // Do not add iterable directly otherwise the values will be fetched as many times it is iterated
    // (i.e. 2 times, see AbstractMessageLite.addAll()).
    Iterable<ValueSetsDto.ValueDto> valueDtoIterable = Iterables.transform(variables, new Function<Variable, ValueSetsDto.ValueDto>() {

      @Override
      public ValueSetsDto.ValueDto apply(Variable fromVariable) {
        String link = uriInfo.getPath() + "/variable/" + fromVariable.getName() + "/value";
        Value value = getValueTable().getVariableValueSource(fromVariable.getName()).getValue(valueSet);
        return Dtos.asDto(link, value, filterBinary).build();
      }
    });
    ImmutableList.Builder<ValueSetsDto.ValueDto> valueDtos = ImmutableList.builder();
    for (ValueSetsDto.ValueDto dto : valueDtoIterable) {
      valueDtos.add(dto);
    }

    ValueSetsDto.ValueSetDto.Builder vsBuilder = Dtos.asDto(valueSet)
        .addAllValues(valueDtos.build()).setTimestamps(Dtos.asDto(valueSet.getTimestamps()));

    return ValueSetsDto.newBuilder().setEntityType(getValueTable().getEntityType())
        .addAllVariables(Iterables.transform(variables, new Function<Variable, String>() {

          @Override
          public String apply(Variable from) {
            return from.getName();
          }

        })).addValueSets(vsBuilder.build()).build();
  }

  private ValueSetsDto.ValueDto getValueDto(final UriInfo uriInfo, final boolean filterBinary) {
    String link = uriInfo.getPath() + "/value";
    Value value = extractValue(entity.getIdentifier());
    return Dtos.asDto(link, value, filterBinary).build();
  }

  private Value extractValue(String identifier) {
    VariableEntity entity = new VariableEntityBean(getValueTable().getEntityType(), identifier);
    ValueSet valueSet = getValueTable().getValueSet(entity);
    return vvs.getValue(valueSet);
  }

  private Value getValueAt(Value value, Integer occurrence) {
    if(value.isSequence() && occurrence != null) return value.asSequence().get(occurrence);
    else return value;
  }

  private ResponseBuilder getValueResponse(String identifier, Value value, Integer pos) {
    Variable variable = vvs.getVariable();
    if(variable.getValueType().equals(BinaryType.get())) {
      return getBinaryValueResponse(identifier, value, pos);
    }
    return TimestampedResponses.ok(getValueTable(), value.toString())
        .type(value.isSequence() ? "text/csv" : MediaType.TEXT_PLAIN);
  }

  private ResponseBuilder getBinaryValueResponse(String identifier, Value value, Integer pos) {
    if(value.isSequence()) return Response.status(Status.BAD_REQUEST);

    Variable variable = vvs.getVariable();
    ResponseBuilder builder;

    // download as a file
    builder = TimestampedResponses.ok(getValueTable(), value.getValue()).type(getVariableMimeType(variable));
    builder.header("Content-Disposition", "attachment; filename=\"" + getFileName(variable, identifier, pos) + "\"");

    return builder;
  }

  private String getFileName(Variable variable, String identifier, Integer pos) {
    // first look in variables attributes
    String name = getFileNameFromAttributes(variable);
    StringBuilder builder = new StringBuilder();

    if(name == null) {
      name = variable.getName();
    }

    builder.append(name);
    int dot = name.lastIndexOf('.');
    if(dot != -1) {
      String id = identifier;
      if(pos != null) {
        id = id + "-" + (pos + 1);
      }
      builder.insert(dot, "-" + id);
    } else {
      builder.append("-").append(identifier);
      if(pos != null) {
        builder.append("-").append(pos + 1);
      }
      builder.append(".").append(getFileExtension(variable));
    }

    return builder.toString();
  }

  private String getFileExtension(Variable variable) {
    // first look in variables attributes
    String extension = getFileExtensionFromAttributes(variable);
    if(extension != null) return extension;

    return MimetypesFileExtensionsMap.get().getPreferedFileExtension(variable.getMimeType());
  }

  private String getVariableMimeType(Variable variable) {
    // first, get it from variable mime-type
    if(variable.getMimeType() != null && !variable.getMimeType().isEmpty()) {
      return variable.getMimeType();
    }

    // if file extension is defined, get the mime-type from it
    String name = getFileExtensionFromAttributes(variable);
    if(name != null) {
      name = "file." + name;
    } else {
      // if file name is defined, get the mime-type from it
      name = getFileNameFromAttributes(variable);
    }

    if(name != null) {
      return MimetypesFileExtensionsMap.get().getMimeType(name);
    }

    return MediaType.APPLICATION_OCTET_STREAM;
  }

  private String getFileNameFromAttributes(Variable variable) {
    for(Attribute attr : variable.getAttributes()) {
      if(attr.getName().equalsIgnoreCase("filename") || attr.getName().equalsIgnoreCase("file-name")) {
        String name = variable.getAttributeStringValue(attr.getName());
        if(name.length() > 0) {
          return name;
        }
      }
    }
    return null;
  }

  private String getFileExtensionFromAttributes(Variable variable) {
    for(Attribute attr : variable.getAttributes()) {
      if(attr.getName().equalsIgnoreCase("fileextension") || attr.getName().equalsIgnoreCase("file-extension")) {
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
