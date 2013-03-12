/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.provider;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jaxb.IgnoredMediaTypes;
import org.jboss.resteasy.util.Types;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Variable;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 *
 */
@Component
@Provider
@Consumes(MediaType.APPLICATION_XML)
public class XStreamVariableReader implements MessageBodyReader<Object> {

  @Override
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Variable.class.isAssignableFrom(type) || isWrapped(type, genericType, annotations, mediaType);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    ObjectInputStream ois = MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory()
        .createXStream().createObjectInputStream(entityStream);
    List<Variable> list = Lists.newArrayList();
    try {
      while(true) {
        list.add((Variable) ois.readObject());
      }
    } catch(EOFException e) {
      // We reached the end of the ois.
    } catch(ClassNotFoundException e) {
      throw new MagmaRuntimeException(e);
    }
    return isWrapped(type, genericType, annotations, mediaType) ? list : (list.size() > 0 ? list.get(0) : null);
  }

  protected boolean isWrapped(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if((Collection.class.isAssignableFrom(type) || type.isArray()) && genericType != null) {
      Class<?> baseType = Types.getCollectionBaseType(type, genericType);
      if(baseType == null) return false;
      return Variable.class.isAssignableFrom(baseType) && !IgnoredMediaTypes.ignored(baseType, annotations, mediaType);
    }
    return false;
  }
}
