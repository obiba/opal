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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

@Component
@Provider
@Consumes({ "application/x-protobuf" })
public class ProtobufNativeReaderProvider extends AbstractProtobufProvider implements MessageBodyReader<Object> {

  // TODO: this should be injected. OPAL-1090
  private final ProtobufProviderHelper helper = new ProtobufProviderHelper();

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Message.class.isAssignableFrom(type) || isWrapped(type, genericType, annotations, mediaType);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
    Class<Message> messageType = extractMessageType(type, genericType, annotations, mediaType);
    final ExtensionRegistry extensionRegistry = helper.extensions().forMessage(messageType);
    final Builder builder = helper.builders().forMessage(messageType);
    if(isWrapped(type, genericType, annotations, mediaType)) {
      ArrayList<Message> msgs = new ArrayList<Message>();
      Builder b = builder.clone();
      while(b.mergeDelimitedFrom(entityStream, extensionRegistry)) {
        msgs.add(b.build());
        b = builder.clone();
      }
      return msgs;
    } else {
      return builder.mergeFrom(entityStream, extensionRegistry).build();
    }
  }
}
