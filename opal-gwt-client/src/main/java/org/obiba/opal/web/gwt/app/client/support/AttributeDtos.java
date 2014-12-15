/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.support;

import java.util.Collection;

import javax.annotation.Nullable;

import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 *
 */
public class AttributeDtos {

  public static final String OPAL_NAMESPACE = "opal";

  public static final String MAELSTROM_NAMESPACE = "maelstrom";

  public static final String LABEL_ATTRIBUTE = "label";

  public static final String SCRIPT_ATTRIBUTE = "script";

  public static final String DERIVED_FROM_ATTRIBUTE = "derivedFrom";

  public static final String DESCRIPTION_ATTRIBUTE = "description";

  public static final String COMMENT_ATTRIBUTE = "comment";

  public static final String STATUS_ATTRIBUTE = "status";

  private AttributeDtos() {
  }

  public static AttributeDto findAttribute(Collection<AttributeDto> attributes, String namespace, String name,
      String value) {
    for(AttributeDto attribute : attributes) {
      if(Objects.equal(namespace, attribute.getNamespace()) && Objects.equal(name, attribute.getName()) &&
          Objects.equal(value, attribute.getValue())) {
        return attribute;
      }
    }
    return null;
  }

  public static AttributeDto create(@Nullable String namespace, String name, String value, @Nullable String locale) {
    AttributeDto attribute = AttributeDto.create();
    if(!Strings.isNullOrEmpty(namespace)) attribute.setNamespace(namespace);
    attribute.setName(name);
    attribute.setValue(value);
    if(!Strings.isNullOrEmpty(locale)) attribute.setLocale(locale);
    return attribute;
  }

  public static AttributeDto create(String labelValue, @Nullable String locale) {
    return create(null, LABEL_ATTRIBUTE, labelValue, locale);
  }

}
