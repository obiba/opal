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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.obiba.magma.Attribute;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.BinaryType;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.support.DefaultPagingVectorSourceImpl;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.magma.support.MimetypesFileExtensionsMap;
import org.obiba.opal.web.magma.support.PagingVectorSource;
import org.obiba.opal.web.math.AbstractSummaryStatisticsResource;
import org.obiba.opal.web.math.SummaryStatisticsResourceFactory;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.VariableDto;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class VariableResource {

  private final ValueTable valueTable;

  private final VariableValueSource vvs;

  private PagingVectorSource pagingVectorSource;

  public VariableResource(ValueTable valueTable, VariableValueSource vvs) {
    this.valueTable = valueTable;
    this.vvs = vvs;
  }

  @GET
  public VariableDto get() {
    return Dtos.asDto(vvs.getVariable()).build();
  }

  @Path("/valueSets")
  public ValueSetsResource getValueSets(@Context Request request) {
    return new ValueSetsResource(valueTable, vvs);
  }

  @GET
  @Path("/value/{identifier}")
  public Response getValue(@PathParam("identifier") String identifier, @QueryParam("pos") Integer pos) {
    if(pos != null && pos < 0) return Response.status(Status.BAD_REQUEST).build();

    Variable variable = vvs.getVariable();

    if(!variable.isRepeatable() && pos != null) return Response.status(Status.BAD_REQUEST).build();

    try {
      Value value = extractValue(identifier);

      ResponseBuilder builder;

      if(value.isNull() || (value.isSequence() && pos != null && pos >= value.asSequence().getSize())) {
        builder = Response.status(Status.NOT_FOUND);
      } else {
        value = getValueAt(value, pos);
        if(value.isNull()) {
          builder = Response.status(Status.NOT_FOUND);
        } else {
          builder = getValueResponse(identifier, value);
        }
      }

      return builder.build();
    } catch(NoSuchValueSetException ex) {
      return Response.status(Status.NOT_FOUND).build();
    }

  }

  @GET
  @POST
  @Path("/values")
  public Iterable<ValueDto> getValues(@QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("limit") @DefaultValue("10") Integer limit) {
    if(limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }
    return Iterables.transform(getPagingVectorSource().getValues(offset, limit), new Function<Value, ValueDto>() {

      @Override
      public ValueDto apply(Value from) {
        return Dtos.asDto("", from).build();
      }

    });
  }

  @Path("/summary")
  public AbstractSummaryStatisticsResource getSummary(@QueryParam("nature") String nature) {
    return new SummaryStatisticsResourceFactory().getResource(this.valueTable, this.vvs, nature);
  }

  VariableValueSource getVariableValueSource() {
    return vvs;
  }

  PagingVectorSource getPagingVectorSource() {
    if(pagingVectorSource == null) {
      pagingVectorSource = new DefaultPagingVectorSourceImpl(valueTable, vvs);
    }
    return pagingVectorSource;
  }

  //
  // private methods
  //

  private Value extractValue(String identifier) {
    VariableEntity entity = new VariableEntityBean(valueTable.getEntityType(), identifier);
    ValueSet valueSet = valueTable.getValueSet(entity);
    return vvs.getValue(valueSet);
  }

  private Value getValueAt(Value value, Integer pos) {
    if(value.isSequence() && pos != null) return value.asSequence().get(pos);
    else
      return value;
  }

  private ResponseBuilder getValueResponse(String identifier, Value value) {
    Variable variable = vvs.getVariable();
    if(variable.getValueType().equals(BinaryType.get())) {
      return getBinaryValueResponse(identifier, value);
    }
    return TimestampedResponses.ok(valueTable, value.toString()).type(value.isSequence() ? "text/csv" : MediaType.TEXT_PLAIN);
  }

  private ResponseBuilder getBinaryValueResponse(String identifier, Value value) {
    if(value.isSequence()) return Response.status(Status.BAD_REQUEST);

    Variable variable = vvs.getVariable();
    ResponseBuilder builder;

    // download as a file
    builder = TimestampedResponses.ok(valueTable, value.getValue()).type(getVariableMimeType(variable));
    builder.header("Content-Disposition", "attachment; filename=\"" + getFileName(variable, identifier) + "\"");

    return builder;
  }

  private String getVariableMimeType(Variable variable) {
    // first, get it from variable mime-type
    if(variable.getMimeType() != null && !variable.getMimeType().isEmpty()) {
      return variable.getMimeType();
    }

    // if file extension is defined, get the mime-type from it
    String name = getFileExtensionFromAttributes(variable);
    if(name != null) {
      name = "patate." + name;
    } else {
      // if file name is defined, get the mime-type from it
      name = getFileNameFromAttributes(variable);
    }

    if(name != null) {
      return MimetypesFileExtensionsMap.get().getMimeType(name);
    }

    return MediaType.APPLICATION_OCTET_STREAM;
  }

  private String getFileName(Variable variable, String identifier) {
    // first look in variables attributes
    String name = getFileNameFromAttributes(variable);
    if(name != null) {
      int dot = name.lastIndexOf('.');
      if(dot != -1) {
        StringBuilder builder = new StringBuilder(name);
        return builder.insert(dot, "-" + identifier).toString();
      } else {
        return name + "-" + identifier + "." + getFileExtension(variable);
      }
    }

    return variable.getName() + "-" + identifier + "." + getFileExtension(variable);
  }

  private String getFileExtension(Variable variable) {
    // first look in variables attributes
    String extension = getFileExtensionFromAttributes(variable);
    if(extension != null) return extension;

    return MimetypesFileExtensionsMap.get().getPreferedFileExtension(variable.getMimeType());
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
