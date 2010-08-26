/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.ws.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jaxb.IgnoredMediaTypes;
import org.jboss.resteasy.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 *
 */
@Component
@Provider
@Produces( { "application/x-jquery-autocomplete+json" })
public class ProtobufJqueryAutocompleteWriterProvider implements MessageBodyWriter<Object> {

  private static final Logger log = LoggerFactory.getLogger(ProtobufJsonReaderProvider.class);

  private final DescriptorFactory descriptorFactory = new DescriptorFactory();

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
  @SuppressWarnings( { "unchecked", "PMD.ExcessiveParameterList" })
  public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    Class<Message> messageType = extractMessageType(type, genericType, annotations, mediaType);
    final Descriptor descriptor = descriptorFactory.forMessage(messageType);
    String valueProperty = uriInfo.getQueryParameters().getFirst("value");
    String labelProperty = uriInfo.getQueryParameters().getFirst("label");
    FieldDescriptor valueFd = descriptor.findFieldByName(valueProperty);
    FieldDescriptor labelFd = descriptor.findFieldByName(labelProperty);

    AutocompletePrinter printer = new AutocompletePrinter(valueFd, labelFd);

    OutputStreamWriter output = new OutputStreamWriter(entityStream, "UTF-8");
    if(isWrapped(type, genericType, annotations, mediaType)) {
      printer.print((Collection<Message>) t, output);
    } else {
      printer.print((Message) t, output);
    }
    output.flush();
  }

  protected boolean isWrapped(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if((Collection.class.isAssignableFrom(type) || type.isArray()) && genericType != null) {
      Class<?> baseType = Types.getCollectionBaseType(type, genericType);
      if(baseType == null) return false;
      return Message.class.isAssignableFrom(baseType) && !IgnoredMediaTypes.ignored(baseType, annotations, mediaType);
    }
    return false;
  }

  private Class<Message> extractMessageType(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if(isWrapped(type, genericType, annotations, mediaType)) {
      return Types.getCollectionBaseType(type, genericType);
    } else {
      return (Class<Message>) type;
    }
  }

  private static final class AutocompletePrinter {

    final FieldDescriptor valueFd;

    final FieldDescriptor labelFd;

    AutocompletePrinter(FieldDescriptor valueFd, FieldDescriptor labelFd) {
      this.valueFd = valueFd;
      this.labelFd = labelFd;
    }

    public void print(Collection<Message> msgs, OutputStreamWriter writer) throws IOException {
      boolean first = true;
      writer.write('[');
      for(Message msg : msgs) {
        if(first == false) writer.append(',');
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

  private static final class DescriptorFactory {

    private Map<Class<Message>, Method> methodCache = new HashMap<Class<Message>, Method>();

    Descriptor forMessage(final Class<Message> messageType) {
      if(messageType == null) throw new IllegalArgumentException("messageType cannot be null");

      try {
        return (Descriptor) extractMethod(messageType).invoke(null);
      } catch(WebApplicationException e) {
        throw e;
      } catch(RuntimeException e) {
        log.error("Error invoking 'getDescriptor' method for type " + messageType.getName(), e);
        throw new WebApplicationException(500);
      } catch(IllegalAccessException e) {
        log.error("Error invoking 'getDescriptor' method for type " + messageType.getName(), e);
        throw new WebApplicationException(500);
      } catch(InvocationTargetException e) {
        log.error("Error invoking 'getDescriptor' method for type " + messageType.getName(), e);
        throw new WebApplicationException(500);
      }
    }

    synchronized private Method extractMethod(final Class<Message> messageType) {

      if(methodCache.containsKey(messageType) == false) {
        try {
          methodCache.put(messageType, messageType.getMethod("getDescriptor"));
        } catch(SecurityException e) {
          log.error("Error getting 'getDescriptor' method from type " + messageType.getName(), e);
          throw new WebApplicationException(500);
        } catch(NoSuchMethodException e) {
          throw new IllegalStateException("The Message type " + messageType.getName() + " does not define a 'getDescriptor' static method.");
        }
      }
      return methodCache.get(messageType);
    }

  }
}
