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
import javax.ws.rs.core.MediaType;
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
import org.obiba.opal.web.magma.support.DefaultPagingVectorSourceImpl;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.magma.support.MimetypesFileExtensionsMap;
import org.obiba.opal.web.magma.support.PagingVectorSource;
import org.obiba.opal.web.math.AbstractSummaryStatisticsResource;
import org.obiba.opal.web.math.SummaryStatisticsResourceFactory;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.VariableDto;

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
  public ValueSetsResource getValueSets() {
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

  private Value extractValue(String identifier) {
    Variable variable = vvs.getVariable();
    VariableEntity entity = new VariableEntityBean(valueTable.getEntityType(), identifier);
    ValueSet valueSet = valueTable.getValueSet(entity);
    return valueTable.getValue(variable, valueSet);
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
    return Response.ok(value.toString(), value.isSequence() ? "text/csv" : MediaType.TEXT_PLAIN);
  }

  private ResponseBuilder getBinaryValueResponse(String identifier, Value value) {
    if(value.isSequence()) return Response.status(Status.BAD_REQUEST);

    Variable variable = vvs.getVariable();
    ResponseBuilder builder;

    // download as a file
    if(variable.getMimeType() != null && !variable.getMimeType().isEmpty()) {
      builder = Response.ok(value.getValue(), variable.getMimeType());
    } else {
      // TODO use mime-util library to detect mime type from byte array content
      builder = Response.ok(value.getValue(), MediaType.APPLICATION_OCTET_STREAM);
    }

    builder.header("Content-Disposition", "attachment; filename=\"" + getFileName(variable, identifier) + "\"");

    return builder;
  }

  private String getFileName(Variable variable, String identifier) {
    // first look in variables attributes
    for(Attribute attr : variable.getAttributes()) {
      if(attr.getName().equalsIgnoreCase("filename") || attr.getName().equalsIgnoreCase("file-name")) {
        String name = variable.getAttributeStringValue(attr.getName());
        if(name.length() > 0) {
          int dot = name.lastIndexOf('.');
          if(dot != -1) {
            StringBuilder builder = new StringBuilder(name);
            return builder.insert(dot, "-" + identifier).toString();
          } else {
            return name + "-" + identifier + "." + getFileExtension(variable);
          }
        }
      }
    }

    return variable.getName() + "-" + identifier + "." + getFileExtension(variable);
  }

  private String getFileExtension(Variable variable) {
    // first look in variables attributes
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

    return MimetypesFileExtensionsMap.get().getPreferedFileExtension(variable.getMimeType());
  }

  @GET
  @POST
  @Path("/values")
  public Iterable<ValueDto> getValues(@QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("limit") @DefaultValue("10") Integer limit) {
    if(limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }
    return Iterables.transform(getPagingVectorSource().getValues(offset, limit), Dtos.valueAsDtoFunc);
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

}
