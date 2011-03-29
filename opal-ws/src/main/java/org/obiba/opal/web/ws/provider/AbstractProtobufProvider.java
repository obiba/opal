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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.jaxb.IgnoredMediaTypes;
import org.jboss.resteasy.util.Types;

import com.google.protobuf.Message;

public class AbstractProtobufProvider {

  @SuppressWarnings("unchecked")
  protected Class<Message> extractMessageType(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if(isWrapped(type, genericType, annotations, mediaType)) {
      return Types.getCollectionBaseType(type, genericType);
    } else {
      return (Class<Message>) type;
    }
  }

  protected boolean isWrapped(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    if((Iterable.class.isAssignableFrom(type) || type.isArray()) && genericType != null) {
      Class<?> baseType = Types.getCollectionBaseType(type, genericType);
      if(baseType == null) return false;
      return Message.class.isAssignableFrom(baseType) && !IgnoredMediaTypes.ignored(baseType, annotations, mediaType);
    }
    return false;
  }
}
