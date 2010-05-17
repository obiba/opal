/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.rest.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jaxb.IgnoredMediaTypes;
import org.jboss.resteasy.util.Types;
import org.obiba.opal.web.model.json.JsonIoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.JsonFormat;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 *
 */
@Component
@Provider
// TODO: should also provide "application/x-protobuf" for native protobuf encoding
@Produces("application/x-protobuf+json")
public class ProtobufJsonReaderProvider implements MessageBodyReader<Object> {

  private static final Logger log = LoggerFactory.getLogger(ProtobufJsonReaderProvider.class);

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type) || isWrapped(type, genericType, annotations, mediaType);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
    Class<Message> messageType = extractMessageType(type, genericType, annotations, mediaType);

    final Builder builder;
    try {
      Method method = messageType.getMethod("newBuilder");
      // newBuilder() is static
      builder = (Builder) method.invoke(null);
    } catch(SecurityException e) {
      log.error("Error getting 'newBuilder' method from type " + type.getName(), e);
      throw new WebApplicationException(500);
    } catch(NoSuchMethodException e) {
      throw new IllegalStateException("The Message type " + type.getName() + " does not define a 'newBuilder' static method.");
    } catch(IllegalArgumentException e) {
      log.error("Error invoking 'newBuilder' method for type " + type.getName(), e);
      throw new WebApplicationException(500);
    } catch(IllegalAccessException e) {
      log.error("Error invoking 'newBuilder' method for type " + type.getName(), e);
      throw new WebApplicationException(500);
    } catch(InvocationTargetException e) {
      log.error("Error invoking 'newBuilder' method for type " + type.getName(), e);
      throw new WebApplicationException(500);
    }

    InputStreamReader input = new InputStreamReader(entityStream, "UTF-8");
    if(isWrapped(type, genericType, annotations, mediaType)) {
      // JsonFormat does not provide a mergeCollection method
      return JsonIoUtil.mergeCollection(builder, input);
    } else {
      JsonFormat.merge(input, builder);
      return builder.build();
    }
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

}
