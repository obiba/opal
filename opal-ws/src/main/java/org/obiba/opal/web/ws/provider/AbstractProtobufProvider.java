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
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.plugins.providers.jaxb.IgnoredMediaTypes;
import org.jboss.resteasy.util.Types;
import org.obiba.opal.web.ws.SortDir;
import org.obiba.opal.web.ws.inject.RequestAttributesProvider;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;

public class AbstractProtobufProvider {

  @Autowired
  private final ProtobufProviderHelper helper = new ProtobufProviderHelper();

  @Autowired
  private RequestAttributesProvider requestAttributesProvider;

  protected ProtobufProviderHelper protobuf() {
    return helper;
  }

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

  /**
   * Sorts a {@code Iterable} of {@code Message} instances based on the following query parameters:
   * <ul>
   * <li>{@code sortField} : the name of the field to sort on</li>
   * <li>{@code sortDir} : the direction (ASC or DESC)</li>
   * </ul>
   * 
   * This method does nothing if the {@code sortField} parameter is missing.
   * 
   * @param messageType the type of {@code Message} to sort
   * @param msgs the {@code Iterable} to sort
   * @return sorted {@code Iterable} or the original instance untouched when the sort parameters are missing or invalid
   */
  protected Iterable<Message> sort(Class<Message> messageType, Iterable<Message> msgs) {
    MultivaluedMap<String, String> query = requestAttributesProvider.getUriInfo().getQueryParameters();

    if(query.containsKey("sortField") == false || Strings.isNullOrEmpty(query.getFirst("sortField"))) return msgs;

    String fieldName = query.getFirst("sortField");
    if(Strings.isNullOrEmpty(fieldName)) return msgs;
    SortDir sortDir = Strings.isNullOrEmpty(query.getFirst("sortDir")) ? SortDir.ASC : SortDir.valueOf(query.getFirst("sortDir"));

    return sortMessages(protobuf().descriptors().forMessage(messageType), msgs, fieldName, sortDir);
  }

  private Iterable<Message> sortMessages(Descriptor descriptor, Iterable<Message> msgs, final String field, SortDir sortDir) {
    Preconditions.checkNotNull(sortDir);
    Preconditions.checkNotNull(field);

    final FieldDescriptor sortField = descriptor.findFieldByName(field);
    // Can't sort on repeated fields
    if(sortField.isRepeated()) return msgs;
    // Can't sort on complex types
    switch(sortField.getJavaType()) {
    case MESSAGE:
      return msgs;
    }

    // Default ordering is natural order with null values last
    Ordering<Comparable<?>> ordering = Ordering.natural().nullsLast();
    if(sortDir == SortDir.DESC) ordering = ordering.reverse();
    return sortMessages(msgs, sortField, ordering);
  }

  private Iterable<Message> sortMessages(Iterable<Message> msgs, final FieldDescriptor field, final Ordering<Comparable<?>> ordering) {
    return ordering.onResultOf(new Function<Message, Comparable<?>>() {
      @Override
      public Comparable<?> apply(Message input) {
        Object value = input.getField(field);
        if(value == null) return null;
        // This can throw a ClassCastException, but we tested JavaType earlier, so it shouldn't
        return Comparable.class.cast(value);
      }
    }).sortedCopy(msgs);
  }
}
