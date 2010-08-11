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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jaxb.IgnoredMediaTypes;
import org.jboss.resteasy.util.Types;
import org.obiba.opal.web.ws.util.JsonIoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.JsonFormat;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 *
 */
@Component
@Provider
@Consumes( { "application/x-protobuf+json", "application/json" })
public class ProtobufJsonReaderProvider implements MessageBodyReader<Object> {

  private static final Logger log = LoggerFactory.getLogger(ProtobufJsonReaderProvider.class);

  private final BuilderFactory builderFactory = new BuilderFactory();

  private final ExtensionRegistryFactory extensionRegistryFactory = new ExtensionRegistryFactory();

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type) || isWrapped(type, genericType, annotations, mediaType);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
    Class<Message> messageType = extractMessageType(type, genericType, annotations, mediaType);

    final ExtensionRegistry extensionRegistry = extensionRegistryFactory.forMessage(messageType);
    final Builder builder = builderFactory.forMessage(messageType);

    InputStreamReader input = new InputStreamReader(entityStream, "UTF-8");
    if(isWrapped(type, genericType, annotations, mediaType)) {
      // JsonFormat does not provide a mergeCollection method
      return JsonIoUtil.mergeCollection(input, extensionRegistry, builder);
    } else {
      JsonFormat.merge(input, extensionRegistry, builder);
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

  private static final class ExtensionRegistryFactory {

    private Map<Class<?>, ExtensionRegistry> registryCache = new HashMap<Class<?>, ExtensionRegistry>();

    private Map<Class<?>, Method> methodCache = new HashMap<Class<?>, Method>();

    ExtensionRegistry forMessage(final Class<Message> messageType) {
      if(messageType == null) throw new IllegalArgumentException("messageType cannot be null");

      Class<?> enclosingType = messageType.getEnclosingClass();
      if(registryCache.containsKey(enclosingType) == false) {
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        invokeStaticMethod(extractStaticMethod("registerAllExtensions", methodCache, messageType.getEnclosingClass()), registry);
        registryCache.put(enclosingType, registry);
      }
      return registryCache.get(enclosingType);
    }
  }

  private static final class BuilderFactory {

    private Map<Class<?>, Method> methodCache = new HashMap<Class<?>, Method>();

    Builder forMessage(final Class<Message> messageType) {
      if(messageType == null) throw new IllegalArgumentException("messageType cannot be null");
      return (Builder) invokeStaticMethod(extractStaticMethod("newBuilder", methodCache, messageType), null);
    }

  }

  private static Object invokeStaticMethod(Method method, Object argument) {
    if(method == null) throw new IllegalArgumentException("method cannot be null");

    try {
      return method.invoke(argument);
    } catch(WebApplicationException e) {
      throw e;
    } catch(RuntimeException e) {
      log.error("Error invoking '" + method.getName() + "' method for type " + method.getDeclaringClass().getName(), e);
      throw new WebApplicationException(500);
    } catch(IllegalAccessException e) {
      log.error("Error invoking '" + method.getName() + "' method for type " + method.getDeclaringClass().getName(), e);
      throw new WebApplicationException(500);
    } catch(InvocationTargetException e) {
      log.error("Error invoking '" + method.getName() + "' method for type " + method.getDeclaringClass().getName(), e);
      throw new WebApplicationException(500);
    }
  }

  private static Method extractStaticMethod(final String methodName, final Map<Class<?>, Method> methodCache, final Class<?> type) {
    if(methodName == null) throw new IllegalArgumentException("methodName cannot be null");
    if(methodCache == null) throw new IllegalArgumentException("methodCache cannot be null");
    if(type == null) throw new IllegalArgumentException("type cannot be null");

    if(methodCache.containsKey(type) == false) {
      try {
        methodCache.put(type, type.getMethod(methodName));
      } catch(SecurityException e) {
        log.error("Error getting '" + methodName + "' method from type " + type.getName(), e);
        throw new WebApplicationException(500);
      } catch(NoSuchMethodException e) {
        throw new IllegalStateException("The type " + type.getName() + " does not define a '" + methodName + "' static method.");
      }
    }
    return methodCache.get(type);
  }

}
