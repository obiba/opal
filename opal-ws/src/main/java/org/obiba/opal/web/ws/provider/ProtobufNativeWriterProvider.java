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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

import com.google.protobuf.Message;

@Component
@Provider
@Produces({ "application/x-protobuf" })
public class ProtobufNativeWriterProvider extends AbstractProtobufProvider implements MessageBodyWriter<Object> {

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
    if(isWrapped(type, genericType, annotations, mediaType)) {
      Class<Message> messageType = extractMessageType(type, genericType, annotations, mediaType);
      for(Message m : sort(messageType, (Iterable<Message>) t)) {
        m.writeDelimitedTo(entityStream);
      }
    } else {
      ((Message) t).writeTo(entityStream);
    }
  }

}
