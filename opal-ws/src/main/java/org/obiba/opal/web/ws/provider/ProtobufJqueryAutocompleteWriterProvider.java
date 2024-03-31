/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.ws.provider;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import org.jboss.resteasy.util.Types;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 *
 */
@Component
@Provider
@Produces({ "application/x-jquery-autocomplete+json" })
public class ProtobufJqueryAutocompleteWriterProvider extends AbstractProtobufProvider
    implements MessageBodyWriter<Object> {

  @Context
  private UriInfo uriInfo;

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type) || isWrapped(type, genericType, annotations, mediaType);
  }

  @Override
  public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  @SuppressWarnings({ "unchecked", "PMD.ExcessiveParameterList" })
  public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException, WebApplicationException {
    Class<Message> messageType = extractMessageType(type, genericType, annotations, mediaType);
    Descriptor descriptor = protobuf().descriptors().forMessage(messageType);
    String valueProperty = uriInfo.getQueryParameters().getFirst("value");
    String labelProperty = uriInfo.getQueryParameters().getFirst("label");
    FieldDescriptor valueFd = descriptor.findFieldByName(valueProperty);
    FieldDescriptor labelFd = descriptor.findFieldByName(labelProperty);

    try(OutputStreamWriter output = new OutputStreamWriter(entityStream, "UTF-8")) {
      AutocompletePrinter printer = new AutocompletePrinter(valueFd, labelFd);
      if(isWrapped(type, genericType, annotations, mediaType)) {
        printer.print((Iterable<Message>) t, output);
      } else {
        printer.print((Message) t, output);
      }
      output.flush();
    }
  }

  @Override
  protected boolean isWrapped(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if((Collection.class.isAssignableFrom(type) || type.isArray()) && genericType != null) {
      Class<?> baseType = Types.getCollectionBaseType(type, genericType);
      return baseType != null && Message.class.isAssignableFrom(baseType);
    }
    return false;
  }

  private static final class AutocompletePrinter {

    final FieldDescriptor valueFd;

    final FieldDescriptor labelFd;

    AutocompletePrinter(FieldDescriptor valueFd, FieldDescriptor labelFd) {
      this.valueFd = valueFd;
      this.labelFd = labelFd;
    }

    public void print(Iterable<Message> msgs, OutputStreamWriter writer) throws IOException {
      boolean first = true;
      writer.write('[');
      for(Message msg : msgs) {
        if(!first) writer.append(',');
        print(msg, writer);
        first = false;
      }
      writer.write(']');
    }

    public void print(Message msg, OutputStreamWriter writer) throws IOException {
      writer.write("{value:\"");
      writer.write(fieldAsString(msg, valueFd));
      writer.write('\"');
      if(labelFd != null) {
        writer.write(",label:\"");
        writer.write(fieldAsString(msg, labelFd));
        writer.write('\"');
      }
      writer.write("}");
    }

    private String fieldAsString(Message msg, FieldDescriptor fd) {
      if(msg.hasField(fd)) {
        Object value = msg.getField(fd);
        if(fd.getType() == FieldDescriptor.Type.STRING) {
          return (String) value;
        }
        return value.toString();
      }
      return "";
    }
  }

}
