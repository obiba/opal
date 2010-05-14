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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jaxb.IgnoredMediaTypes;
import org.jboss.resteasy.util.Types;
import org.obiba.opal.web.model.json.JsonIoUtil;
import org.springframework.stereotype.Component;

import com.google.protobuf.JsonFormat;
import com.google.protobuf.Message;

/**
 *
 */
@Component
@Provider
@Produces("application/json")
public class ProtobufJsonProvider implements MessageBodyWriter<Object> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type) || isWrapped(type, genericType, annotations, mediaType);
  }

  @Override
  public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    OutputStreamWriter output = new OutputStreamWriter(entityStream, "UTF-8");
    if(isWrapped(type, genericType, annotations, mediaType)) {
      // JsonFormat does not provide a printList method
      JsonIoUtil.printCollection((Collection<Message>) t, output);
    } else {
      JsonFormat.print((Message) t, output);
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

}
